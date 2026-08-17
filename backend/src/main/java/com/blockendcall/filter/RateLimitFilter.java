package com.blockendcall.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MS = 60_000L;

    private static final int MAX_TRACKED_IPS = 10_000;

    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        boolean rateLimitApplies = uri.startsWith("/api/v1/numbers/check")
                || uri.equals("/api/v1/numbers/check-batch");

        if (rateLimitApplies) {
            String ip = req.getRemoteAddr();
            long now = Instant.now().toEpochMilli();

            evictIfNecessary(now);

            requestLog.compute(ip, (k, deque) -> {
                if (deque == null) deque = new ArrayDeque<>();
                while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
                    deque.pollFirst();
                }
                deque.addLast(now);
                return deque;
            });

            if (requestLog.get(ip).size() > MAX_REQUESTS) {
                res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.getWriter().write("{\"success\":false,\"message\":\"Too many requests, please wait\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private void evictIfNecessary(long now) {
        if (requestLog.size() >= MAX_TRACKED_IPS) {
            requestLog.entrySet().removeIf(e -> {
                Deque<Long> deque = e.getValue();
                return deque.isEmpty() || (now - deque.peekLast() > WINDOW_MS);
            });
        }
    }
}

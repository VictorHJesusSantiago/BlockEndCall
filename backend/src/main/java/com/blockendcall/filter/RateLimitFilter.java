package com.blockendcall.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ConcurrentHashMap<String, Deque<Long>> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        boolean isRateLimited = uri.startsWith("/api/v1/numbers/check") || uri.equals("/api/v1/numbers/check-batch");

        if (isRateLimited) {
            String ip = getClientIp(req);
            long now = Instant.now().toEpochMilli();

            requests.compute(ip, (k, deque) -> {
                if (deque == null) deque = new ArrayDeque<>();
                while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) deque.pollFirst();
                deque.addLast(now);
                return deque;
            });

            if (requests.get(ip).size() > MAX_REQUESTS) {
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"message\":\"Too many requests, please wait\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}

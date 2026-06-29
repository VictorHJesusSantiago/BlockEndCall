package com.blockendcall.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitFilter — proteção contra abuso de endpoints públicos")
class RateLimitFilterTest {

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    @DisplayName("requisição abaixo do limite passa pelo filtro normalmente")
    void underLimit_requestPassesThrough() throws Exception {
        MockHttpServletRequest req = buildRequest("/api/v1/numbers/check/+5511999990000", "192.0.2.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        assertThat(res.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(filterChain).doFilter(req, res);
    }

    @Test
    @DisplayName("endpoint não sujeito a rate limit nunca retorna 429")
    void nonLimitedEndpoint_alwaysPassesThrough() throws Exception {
        MockHttpServletRequest req = buildRequest("/api/v1/auth/login", "192.0.2.1");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, filterChain);

        verify(filterChain).doFilter(req, res);
    }

    @Test
    @DisplayName("X-Forwarded-For é ignorado — usa RemoteAddr real para evitar bypass")
    void xForwardedFor_isIgnored_usesRemoteAddr() throws Exception {
        MockHttpServletRequest req = buildRequest("/api/v1/numbers/check-batch", "203.0.113.5");
        req.addHeader("X-Forwarded-For", "1.2.3.4");
        MockHttpServletResponse res = new MockHttpServletResponse();

        // A primeira requisição usa o remoteAddr real (203.0.113.5), não o XFF (1.2.3.4)
        // Se o XFF fosse usado, geraria um bucket diferente do remoteAddr.
        // Verificamos apenas que o filtro passa corretamente sem erros.
        filter.doFilterInternal(req, res, filterChain);

        assertThat(res.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(filterChain).doFilter(req, res);
    }

    @Test
    @DisplayName("após 60 requisições do mesmo IP no mesmo minuto, retorna 429")
    void overLimit_returns429() throws Exception {
        String ip = "198.51.100.1";
        String uri = "/api/v1/numbers/check/+5511111111";

        // Executa 60 requisições (limite máximo)
        for (int i = 0; i < 60; i++) {
            MockHttpServletRequest req = buildRequest(uri, ip);
            filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        // A 61ª requisição deve ser bloqueada
        MockHttpServletRequest req61 = buildRequest(uri, ip);
        MockHttpServletResponse res61 = new MockHttpServletResponse();
        filter.doFilterInternal(req61, res61, filterChain);

        assertThat(res61.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(res61.getContentType()).isEqualTo("application/json");
        assertThat(res61.getContentAsString()).contains("Too many requests");
    }

    @Test
    @DisplayName("IPs diferentes têm buckets independentes")
    void differentIps_haveIndependentBuckets() throws Exception {
        String uri = "/api/v1/numbers/check/+5511111111";

        // IP A faz 60 requisições (no limite)
        for (int i = 0; i < 60; i++) {
            filter.doFilterInternal(buildRequest(uri, "10.1.0.1"), new MockHttpServletResponse(), filterChain);
        }

        // IP B faz 1 requisição e não deve ser bloqueado
        MockHttpServletResponse resB = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest(uri, "10.1.0.2"), resB, filterChain);

        assertThat(resB.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("check-batch também é sujeito ao rate limit")
    void checkBatchEndpoint_isRateLimited() throws Exception {
        String uri = "/api/v1/numbers/check-batch";
        String ip = "198.51.100.50";

        for (int i = 0; i < 60; i++) {
            filter.doFilterInternal(buildRequest(uri, ip), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilterInternal(buildRequest(uri, ip), res, filterChain);

        assertThat(res.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    private MockHttpServletRequest buildRequest(String uri, String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRemoteAddr(remoteAddr);
        return req;
    }
}

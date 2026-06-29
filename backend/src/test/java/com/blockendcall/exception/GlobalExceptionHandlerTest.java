package com.blockendcall.exception;

import com.blockendcall.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler — mapeamento de exceções para códigos HTTP")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("ResourceNotFoundException → 404 Not Found com success=false")
    void resourceNotFound_returns404() throws Exception {
        mockMvc.perform(get("/fake/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("recurso não encontrado"));
    }

    @Test
    @DisplayName("DuplicateReportException → 409 Conflict com success=false")
    void duplicateReport_returns409() throws Exception {
        mockMvc.perform(get("/fake/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("BadCredentialsException → 401 Unauthorized com mensagem genérica")
    void badCredentials_returns401() throws Exception {
        mockMvc.perform(get("/fake/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("JwtException → 401 Unauthorized com mensagem de token inválido")
    void jwtException_returns401() throws Exception {
        mockMvc.perform(get("/fake/jwt-error"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    @DisplayName("AccessDeniedException → 403 Forbidden")
    void accessDenied_returns403() throws Exception {
        mockMvc.perform(get("/fake/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @DisplayName("UnsupportedOperationException → 503 Service Unavailable")
    void unsupportedOperation_returns503() throws Exception {
        mockMvc.perform(get("/fake/not-implemented"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("This feature is not yet available"));
    }

    @Test
    @DisplayName("IllegalArgumentException → 400 Bad Request com a mensagem da exceção")
    void illegalArgument_returns400WithMessage() throws Exception {
        mockMvc.perform(get("/fake/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("argumento inválido"));
    }

    @Test
    @DisplayName("Exception genérica → 500 Internal Server Error com mensagem segura")
    void genericException_returns500WithSafeMessage() throws Exception {
        mockMvc.perform(get("/fake/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    // Controlador falso que deliberadamente lança exceções para testar o handler
    @RestController
    static class FakeController {

        @GetMapping("/fake/not-found")
        public void notFound() { throw new ResourceNotFoundException("recurso não encontrado"); }

        @GetMapping("/fake/duplicate")
        public void duplicate() { throw new DuplicateReportException("já reportado"); }

        @GetMapping("/fake/bad-credentials")
        public void badCredentials() { throw new BadCredentialsException("wrong"); }

        @GetMapping("/fake/jwt-error")
        public void jwtError() { throw new io.jsonwebtoken.MalformedJwtException("bad jwt"); }

        @GetMapping("/fake/access-denied")
        public void accessDenied() { throw new AccessDeniedException("forbidden"); }

        @GetMapping("/fake/not-implemented")
        public void notImplemented() { throw new UnsupportedOperationException("not yet"); }

        @GetMapping("/fake/illegal-arg")
        public void illegalArg() { throw new IllegalArgumentException("argumento inválido"); }

        @GetMapping("/fake/generic-error")
        public void genericError() throws Exception { throw new Exception("boom"); }
    }
}

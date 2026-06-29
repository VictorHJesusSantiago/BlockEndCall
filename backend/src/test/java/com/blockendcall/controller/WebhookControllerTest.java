package com.blockendcall.controller;

import com.blockendcall.dto.response.WebhookResponse;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
@DisplayName("WebhookController — gerenciamento de webhooks (requer ADMIN)")
class WebhookControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean WebhookService webhookService;
    @MockBean UserDetailsService userDetailsService;

    private static final String BASE = "/api/v1/webhooks";

    private WebhookResponse stubWebhook(Long id) {
        return new WebhookResponse(id, "https://hook.example.com", true, LocalDateTime.now());
    }

    // ─── POST / ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST / retorna 401 sem autenticação")
    void register_noAuth_returns401() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://hook.example.com\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST / retorna 403 com role USER")
    void register_userRole_returns403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://hook.example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST / retorna 201 Created com role ADMIN e URL válida")
    void register_adminRole_returns201() throws Exception {
        when(webhookService.register(anyString(), any())).thenReturn(stubWebhook(1L));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://hook.example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.url").value("https://hook.example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST / retorna 400 quando URL está em branco")
    void register_blankUrl_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST / retorna 400 quando URL usa HTTP em vez de HTTPS")
    void register_httpUrl_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://example.com/hook\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST / retorna 400 com mensagem quando SSRF é detectado pelo service")
    void register_ssrfRejected_returns400() throws Exception {
        when(webhookService.register(anyString(), any()))
                .thenThrow(new IllegalArgumentException("Webhook URL must use HTTPS"));

        // Bypass de validação de @Pattern enviando URL interna (mas com HTTPS)
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://internal.corp/hook\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Webhook URL must use HTTPS"));
    }

    // ─── GET / ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET / retorna 401 sem autenticação")
    void list_noAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET / retorna 403 com role USER")
    void list_userRole_returns403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET / retorna 200 com lista de webhooks para ADMIN")
    void list_adminRole_returns200() throws Exception {
        when(webhookService.listAll()).thenReturn(List.of(
                stubWebhook(1L), stubWebhook(2L)));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    // ─── DELETE /{id} ──────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /{id} retorna 401 sem autenticação")
    void deactivate_noAuth_returns401() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /{id} retorna 403 com role USER")
    void deactivate_userRole_returns403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /{id} retorna 200 quando ADMIN desativa webhook existente")
    void deactivate_adminRole_returns200() throws Exception {
        doNothing().when(webhookService).deactivate(1L);

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook deactivated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /{id} retorna 404 quando webhook não existe")
    void deactivate_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Webhook not found: 99"))
                .when(webhookService).deactivate(99L);

        mockMvc.perform(delete(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

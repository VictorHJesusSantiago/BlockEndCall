package com.blockendcall.controller;

import com.blockendcall.dto.response.BlockedCallLogResponse;
import com.blockendcall.service.CallLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(CallLogController.class)
@DisplayName("CallLogController — log de chamadas bloqueadas pelo servidor")
class CallLogControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CallLogService callLogService;
    @MockBean UserDetailsService userDetailsService;

    private static final String BASE = "/api/v1/users/me/call-log";

    // ─── POST / ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST / retorna 401 sem autenticação")
    void logBlockedCall_noAuth_returns401() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+5511999990000\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "victor@example.com")
    @DisplayName("POST / retorna 200 quando autenticado e payload válido")
    void logBlockedCall_authenticated_returns200() throws Exception {
        doNothing().when(callLogService).logBlockedCall(any(), anyString());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+5511999990000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "victor@example.com")
    @DisplayName("POST / retorna 400 quando phoneNumber está ausente")
    void logBlockedCall_missingPhoneNumber_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET / ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET / retorna 401 sem autenticação")
    void getMyCallLog_noAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "victor@example.com")
    @DisplayName("GET / retorna 200 com página de logs quando autenticado")
    void getMyCallLog_authenticated_returns200() throws Exception {
        BlockedCallLogResponse log = BlockedCallLogResponse.builder()
                .id(1L).phoneNumber("+5511999990000")
                .blockResult("REJECTED")
                .blockedAt(LocalDateTime.now())
                .build();

        when(callLogService.getMyCallLog(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].phoneNumber").value("+5511999990000"));
    }

    // ─── GET /count ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /count retorna 401 sem autenticação")
    void getCount_noAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "victor@example.com")
    @DisplayName("GET /count retorna contagem total de chamadas bloqueadas")
    void getCount_authenticated_returnsCount() throws Exception {
        when(callLogService.getMyBlockedCallCount(anyString())).thenReturn(42L);

        mockMvc.perform(get(BASE + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }
}

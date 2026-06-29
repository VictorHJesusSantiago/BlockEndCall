package com.blockendcall.controller;

import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.NumberCheckResponse;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.service.BlockedNumberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockedNumberController.class)
@DisplayName("BlockedNumberController — CRUD de números bloqueados")
class BlockedNumberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BlockedNumberService blockedNumberService;
    @MockBean UserDetailsService userDetailsService;

    private static final String BASE = "/api/v1/numbers";

    // ─── GET /check/{phoneNumber} — endpoint público ───────────────────────────

    @Test
    @DisplayName("GET /check/{number} retorna 200 sem autenticação (endpoint público)")
    void checkNumber_noAuth_returns200() throws Exception {
        NumberCheckResponse response = NumberCheckResponse.builder()
                .phoneNumber("+5511999990000").blocked(false).build();
        when(blockedNumberService.checkNumber(anyString())).thenReturn(response);

        mockMvc.perform(get(BASE + "/check/+5511999990000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.blocked").value(false));
    }

    @Test
    @DisplayName("GET /check/{number} retorna blocked=true para número confirmado")
    void checkNumber_confirmedNumber_returnsBlocked() throws Exception {
        NumberCheckResponse response = NumberCheckResponse.builder()
                .phoneNumber("+5511999990000").blocked(true)
                .confirmed(true).riskLevel("HIGH").spamScore(80)
                .build();
        when(blockedNumberService.checkNumber(anyString())).thenReturn(response);

        mockMvc.perform(get(BASE + "/check/+5511999990000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.blocked").value(true))
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.data.spamScore").value(80));
    }

    // ─── POST /check-batch — endpoint público ────────────────────────────────

    @Test
    @DisplayName("POST /check-batch retorna 200 sem autenticação para lista válida")
    void checkBatch_noAuth_returns200() throws Exception {
        when(blockedNumberService.checkBatch(any())).thenReturn(List.of(
                NumberCheckResponse.builder().phoneNumber("+5511111111111").blocked(false).build()
        ));

        mockMvc.perform(post(BASE + "/check-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumbers\":[\"+5511111111111\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].phoneNumber").value("+5511111111111"));
    }

    @Test
    @DisplayName("POST /check-batch retorna 400 quando lista excede 20 números")
    void checkBatch_over20Numbers_returns400() throws Exception {
        List<String> tooMany = List.of(
                "1","2","3","4","5","6","7","8","9","10",
                "11","12","13","14","15","16","17","18","19","20","21");
        String json = "{\"phoneNumbers\":" + objectMapper.writeValueAsString(tooMany) + "}";

        mockMvc.perform(post(BASE + "/check-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ─── GET / — endpoint protegido (auth requerida) ─────────────────────────

    @Test
    @DisplayName("GET / retorna 401 sem autenticação")
    void listNumbers_noAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET / retorna 200 com autenticação válida")
    void listNumbers_authenticated_returns200() throws Exception {
        when(blockedNumberService.listConfirmedNumbers(any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─── POST /report ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /report retorna 401 sem autenticação")
    void reportNumber_noAuth_returns401() throws Exception {
        mockMvc.perform(post(BASE + "/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+5511999990000\",\"category\":\"SCAM\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /report retorna 201 Created para report válido")
    void reportNumber_authenticated_returns201() throws Exception {
        BlockedNumberResponse created = BlockedNumberResponse.builder()
                .id(1L).phoneNumber("+5511999990000")
                .confirmed(false).reportCount(1)
                .build();

        when(blockedNumberService.reportNumber(any(), anyString())).thenReturn(created);

        mockMvc.perform(post(BASE + "/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+5511999990000\",\"category\":\"SCAM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.phoneNumber").value("+5511999990000"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /report retorna 409 quando usuário já reportou o número")
    void reportNumber_duplicate_returns409() throws Exception {
        when(blockedNumberService.reportNumber(any(), anyString()))
                .thenThrow(new DuplicateReportException("Você já reportou este número"));

        mockMvc.perform(post(BASE + "/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"+5511999990000\",\"category\":\"SCAM\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /{id}/false-positive ────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /{id}/false-positive retorna 200 quando marcação é nova")
    void reportFalsePositive_authenticated_returns200() throws Exception {
        doNothing().when(blockedNumberService).reportFalsePositive(any(), anyString(), any());

        mockMvc.perform(post(BASE + "/1/false-positive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Não é spam\"}"))
                .andExpect(status().isOk());
    }

    // ─── PATCH /{id}/whitelist — requer ROLE_ADMIN ────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/whitelist retorna 401 sem autenticação")
    void whitelist_noAuth_returns401() throws Exception {
        mockMvc.perform(patch(BASE + "/1/whitelist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /{id}/whitelist retorna 403 com role USER")
    void whitelist_userRole_returns403() throws Exception {
        mockMvc.perform(patch(BASE + "/1/whitelist"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/whitelist retorna 200 com role ADMIN")
    void whitelist_adminRole_returns200() throws Exception {
        doNothing().when(blockedNumberService).adminWhitelist(1L);

        mockMvc.perform(patch(BASE + "/1/whitelist"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /{id}/whitelist retorna 404 quando número não existe")
    void whitelist_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Number not found: 99"))
                .when(blockedNumberService).adminWhitelist(99L);

        mockMvc.perform(patch(BASE + "/99/whitelist"))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /{id} — requer ROLE_ADMIN ────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /{id} retorna 403 com role USER")
    void deleteNumber_userRole_returns403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /{id} retorna 200 com role ADMIN")
    void deleteNumber_adminRole_returns200() throws Exception {
        doNothing().when(blockedNumberService).deleteNumber(1L);

        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isOk());
    }

    // ─── GET /autocomplete — endpoint público ────────────────────────────────

    @Test
    @DisplayName("GET /autocomplete retorna 200 sem autenticação")
    void autocomplete_noAuth_returns200() throws Exception {
        when(blockedNumberService.autocomplete(anyString())).thenReturn(List.of("+5511"));

        mockMvc.perform(get(BASE + "/autocomplete").param("q", "+5511"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("+5511"));
    }

    // ─── GET /{id} ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /{id} retorna 404 quando ID não existe")
    void getById_notFound_returns404() throws Exception {
        when(blockedNumberService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Number not found: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

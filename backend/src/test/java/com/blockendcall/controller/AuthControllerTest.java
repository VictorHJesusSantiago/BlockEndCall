package com.blockendcall.controller;

import com.blockendcall.dto.response.AuthResponse;
import com.blockendcall.enums.UserRole;
import com.blockendcall.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController — endpoints de autenticação e registro")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AuthService authService;
    @MockBean UserDetailsService userDetailsService;

    private static final String BASE = "/api/v1/auth";

    private AuthResponse stubResponse() {
        return AuthResponse.builder()
                .token("jwt-token").type("Bearer")
                .userId(1L).name("Victor")
                .email("victor@example.com").role("USER")
                .build();
    }

    // ─── POST /register ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /register retorna 201 Created com token quando dados são válidos")
    void register_validRequest_returns201WithToken() throws Exception {
        when(authService.register(any())).thenReturn(stubResponse());

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Victor\",\"email\":\"victor@example.com\",\"password\":\"Senha@123\",\"phone\":\"+5511999990000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("POST /register retorna 400 quando email está em branco")
    void register_blankEmail_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Victor\",\"email\":\"\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /register retorna 400 quando email é inválido")
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Victor\",\"email\":\"nao-e-email\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register retorna 400 quando email já está cadastrado (IllegalArgument do service)")
    void register_duplicateEmail_returns400() throws Exception {
        when(authService.register(any()))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Victor\",\"email\":\"victor@example.com\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    // ─── POST /login ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login retorna 200 com token quando credenciais são válidas")
    void login_validCredentials_returns200WithToken() throws Exception {
        when(authService.login(any())).thenReturn(stubResponse());

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"victor@example.com\",\"password\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /login retorna 401 quando credenciais são inválidas")
    void login_wrongCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"victor@example.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ─── POST /verify-email ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /verify-email retorna 503 (funcionalidade não implementada)")
    void verifyEmail_returns503() throws Exception {
        doThrow(new UnsupportedOperationException("not yet"))
                .when(authService).verifyEmail(any());

        mockMvc.perform(post(BASE + "/verify-email").param("token", "tok123"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /forgot-password ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /forgot-password retorna 503 (funcionalidade não implementada)")
    void forgotPassword_returns503() throws Exception {
        doThrow(new UnsupportedOperationException("not yet"))
                .when(authService).sendPasswordResetEmail(any());

        mockMvc.perform(post(BASE + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"victor@example.com\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("POST /forgot-password retorna 400 quando email é inválido")
    void forgotPassword_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── POST /reset-password ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /reset-password retorna 503 (funcionalidade não implementada)")
    void resetPassword_returns503() throws Exception {
        doThrow(new UnsupportedOperationException("not yet"))
                .when(authService).resetPassword(any(), any());

        mockMvc.perform(post(BASE + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"newPassword\":\"NovaSenha123\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("POST /reset-password retorna 400 quando nova senha tem menos de 8 caracteres")
    void resetPassword_shortPassword_returns400() throws Exception {
        mockMvc.perform(post(BASE + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"tok\",\"newPassword\":\"curta\"}"))
                .andExpect(status().isBadRequest());
    }
}

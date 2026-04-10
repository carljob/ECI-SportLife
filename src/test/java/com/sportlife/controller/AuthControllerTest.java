package com.sportlife.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlife.core.models.User;
import com.sportlife.core.services.AuthService;
import com.sportlife.handlers.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de integración para AuthController usando MockMvc.
 * @WebMvcTest carga solo la capa web (sin BD ni MongoDB).
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  AuthService authService;

    // ── Records internos para construir el JSON del request ───────────
    private record LoginBody(String email, String password) {}
    private record RegisterBody(String fullName, String email, String password) {}

    // ── Tests de login ────────────────────────────────────────────────

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn("token-demo");

        String body = objectMapper.writeValueAsString(new LoginBody("user@sportlife.com", "pass123"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-demo"));
    }

    @Test
    void login_shouldReturn400WhenEmailInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(new LoginBody("not-an-email", "pass123"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400WhenCredentialsInvalid() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenThrow(new BusinessException("Credenciales invalidas"));

        String body = objectMapper.writeValueAsString(new LoginBody("user@sportlife.com", "wrong"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
            .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }

    // ── Tests de registro ─────────────────────────────────────────────

    @Test
    void register_shouldReturn200WithUserIdAndToken() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
            .thenReturn(User.builder().id(1L).email("new@sportlife.com").build());
        when(authService.login(anyString(), anyString())).thenReturn("token-register");

        String body = objectMapper.writeValueAsString(
            new RegisterBody("Nuevo Usuario", "new@sportlife.com", "abcd12"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.token").value("token-register"));
    }

    @Test
    void register_shouldReturn400WhenEmailAlreadyExists() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
            .thenThrow(new BusinessException("El email ya esta registrado"));

        String body = objectMapper.writeValueAsString(
            new RegisterBody("Usuario", "exists@sportlife.com", "pass123"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("El email ya esta registrado"));
    }

    @Test
    void register_shouldReturn400WhenFieldsMissing() throws Exception {
        String body = "{\"email\":\"solo@email.com\"}"; // sin fullName ni password

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}

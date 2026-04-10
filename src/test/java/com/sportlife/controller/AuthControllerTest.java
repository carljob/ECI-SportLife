package com.sportlife.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlife.core.models.User;
import com.sportlife.core.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn("token-demo");

        String body = objectMapper.writeValueAsString(new LoginBody("user@sportlife.com", "1234"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-demo"));
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        when(authService.register(anyString(), anyString(), anyString()))
            .thenReturn(User.builder().id(1L).email("new@sportlife.com").build());
        when(authService.login(anyString(), anyString())).thenReturn("token-register");

        String body = objectMapper.writeValueAsString(new RegisterBody("Nuevo", "new@sportlife.com", "abcd"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.token").value("token-register"));
    }

    private record LoginBody(String email, String password) {}

    private record RegisterBody(String fullName, String email, String password) {}
}


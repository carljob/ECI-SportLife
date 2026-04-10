package com.sportlife.controller;

import com.sportlife.core.models.User;
import com.sportlife.core.services.AuthService;
import com.sportlife.dtos.request.LoginRequest;
import com.sportlife.dtos.request.RegisterUserRequest;
import com.sportlife.dtos.response.AuthResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Requisito del enunciado: registro de usuario.
     * Crea la cuenta y retorna un token inicial para continuar el flujo.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        User user = authService.register(request.getFullName(), request.getEmail(), request.getPassword());
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(AuthResponse.builder().userId(user.getId()).token(token).build());
    }

    /**
     * Requisito del enunciado: autenticación/login.
     * Valida credenciales y emite token para endpoints protegidos.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
}


package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.request.LoginRequest;
import com.minerva.api.request.PasswordRequest;
import com.minerva.api.request.PasswordValidationRequest;
import com.minerva.api.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println("========== LOGIN CONTROLLER ==========");
        System.out.println("Username recibido: " + request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/validar-password")
    public ResponseEntity<Void> validarPassword(
            Authentication authentication,
            @RequestBody PasswordValidationRequest request) {

        authService.validarPassword(
                authentication.getName(),
                request.getCurrentPassword());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            Authentication authentication,
            @RequestBody PasswordRequest request) {
        authService.cambiarPassword(
                authentication.getName(),
                request);

        return ResponseEntity.noContent().build();
    }
}

// @PostMapping(value = "register")
// public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest
// request){
// return ResponseEntity.ok(authService.register(request));
// }

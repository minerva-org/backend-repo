package com.minerva.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.User.UserRepository;
import com.minerva.api.controller.AuthResponse;
import com.minerva.api.jwt.JwtService;
import com.minerva.api.model.Persona;
import com.minerva.api.request.LoginRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginAcceptsEmailAsCredentialAlias() {
        User user = new User();
        user.setUsername("bootstrap");
        user.setPassword("encoded-password");

        Persona persona = new Persona();
        persona.setEmail("bootstrap@chapala.edu.mx");
        persona.setRol(Roles.DEV);
        user.setPersona(persona);

        Authentication authentication = new UsernamePasswordAuthenticationToken("bootstrap", "Admin123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByUsername("bootstrap@chapala.edu.mx")).thenReturn(Optional.empty());
        when(userRepository.findByPersonaEmailIgnoreCase("bootstrap@chapala.edu.mx")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("bootstrap")).thenReturn(Optional.of(user));
        when(jwtService.getToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(LoginRequest.builder()
            .username("bootstrap@chapala.edu.mx")
            .password("Admin123!")
            .build());

        assertEquals("jwt-token", response.getToken());
        verify(userRepository).findByPersonaEmailIgnoreCase("bootstrap@chapala.edu.mx");
    }
}

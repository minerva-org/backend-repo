package com.minerva.api.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.request.PasswordRequest;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.User.UserRepository;
import com.minerva.api.controller.AuthResponse;
import com.minerva.api.excepciones.CuentaDeshabilitadaException;
import com.minerva.api.jwt.JwtService;
import com.minerva.api.request.LoginRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final PasswordEncoder passwordEncoder;

        public AuthResponse login(LoginRequest request) {
                String identifier = request.getUsername() == null ? "" : request.getUsername().trim();
                String password = request.getPassword() == null ? "" : request.getPassword();

                String username = resolveUsername(identifier)
                                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

                if (!user.isEnabled()) {
                        throw new CuentaDeshabilitadaException(
                                        "CUENTA_DESHABILITADA",
                                        formatDisabledMessage(user.getPersona().getRol()));
                }

                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

                String role = user.getAuthorities().stream()
                                .findFirst()
                                .map(authority -> authority.getAuthority())
                                .orElse(null);

                String token = jwtService.getToken(user);
                return AuthResponse.builder()
                                .token(token)
                                .role(role)
                                .personaId(user.getPersona().getId())
                                .build();
        }

        private Optional<String> resolveUsername(String identifier) {
                if (identifier == null || identifier.isBlank()) {
                        return Optional.empty();
                }

                return userRepository.findByUsername(identifier)
                                .map(User::getUsername)
                                .or(() -> userRepository.findByPersonaEmailIgnoreCase(identifier)
                                                .map(User::getUsername));
        }

        private String formatDisabledMessage(Roles rol) {
                if (rol == null) {
                        return "Cuenta deshabilitada";
                }

                String nombreRol = rol.name().toLowerCase().replace('_', ' ');
                nombreRol = Character.toUpperCase(nombreRol.charAt(0)) + nombreRol.substring(1);
                return nombreRol + " deshabilitado";
        }

        public void validarPassword(String username, String currentPassword) {
                var user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Usuario no encontrado"));

                if (currentPassword == null ||
                                !passwordEncoder.matches(currentPassword, user.getPassword())) {
                        throw new BadCredentialsException(
                                        "La contraseña actual es incorrecta.");
                }
        }

        public void cambiarPassword(String username, PasswordRequest request) {

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
                if (request.getCurrentPassword() == null
                                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La contraseña actual es incorrecta");
                }
                if (request.getNewPassword() == null ||
                                request.getNewPassword().isBlank()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La nueva contraseña no puede estar vacía");
                }
                if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La nueva contraseña debe ser diferente a la actual");
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);
        }

        // public AuthResponse register(RegisterRequest request){
        // User user = User.builder()
        // .username(request.username)
        // .password(passwordEncoder.encode(request.password))
        // .role(Roles.ALUMNO)
        // .build();

        // userRepository.save(user);

        // return AuthResponse.builder()
        // .token(jwtService.getToken(user))
        // .build();
        // }

}

package com.minerva.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.minerva.api.User.User;
import com.minerva.api.User.UserRepository;
import com.minerva.api.controller.AuthResponse;
import com.minerva.api.jwt.JwtService;
import com.minerva.api.request.LoginRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRespository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        String username = resolveUsername(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRespository.findByUsername(username).orElseThrow();

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

    private java.util.Optional<String> resolveUsername(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return java.util.Optional.empty();
        }

        return userRespository.findByUsername(identifier)
                .map(User::getUsername)
                .or(() -> userRespository.findByPersonaEmailIgnoreCase(identifier)
                        .map(User::getUsername));
    }

    // public AuthResponse register(RegisterRequest request){
    // User user = User.builder()
    // .username(request.username)
    // .password(passwordEncoder.encode(request.password))
    // .role(Roles.ALUMNO)
    // .build();

    // userRespository.save(user);

    // return AuthResponse.builder()
    // .token(jwtService.getToken(user))
    // .build();
    // }

}

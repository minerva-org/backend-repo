package com.minerva.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.minerva.api.User.Roles;
import com.minerva.api.User.User;
import com.minerva.api.User.UserRepository;
import com.minerva.api.controller.AuthResponse;
import com.minerva.api.jwt.JwtService;
import com.minerva.api.request.LoginRequest;
import com.minerva.api.request.RegisterRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AuthService {

    private final UserRepository userRespository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        UserDetails user = userRespository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.getToken(user);
        return AuthResponse.builder()
            .token(token)
            .build();
    }

    // public  AuthResponse register(RegisterRequest request){
    //     User user = User.builder()
    //     .username(request.username)
    //     .password(passwordEncoder.encode(request.password))
    //     .role(Roles.ALUMNO)
    //     .build();

    //     userRespository.save(user);

    //     return AuthResponse.builder()
    //     .token(jwtService.getToken(user))
    //     .build();
    // }

}


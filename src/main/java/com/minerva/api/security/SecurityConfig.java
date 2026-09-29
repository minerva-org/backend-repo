package com.minerva.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;


import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.minerva.api.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor 
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf ->
                csrf
                .disable())
            .authorizeHttpRequests(authRequest -> authRequest
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/planteles/**").hasAnyAuthority("ADMIN", "COORDINADOR", "DEV")
                .requestMatchers("api/universidades/**").hasAnyAuthority("DEV")
                .requestMatchers("api/materias/**").hasAnyAuthority("DEV", "DOCENTE", "COORDINADOR")
                .requestMatchers("api/unidades/**").hasAnyAuthority("DEV", "DOCENTE", "COORDINADOR")
                .requestMatchers("api/temas/**").hasAnyAuthority("DEV", "DOCENTE", "COORDINADOR")
                .requestMatchers("api/conceptos/**").hasAnyAuthority("DEV", "DOCENTE", "COORDINADOR")
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager ->
                sessionManager
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
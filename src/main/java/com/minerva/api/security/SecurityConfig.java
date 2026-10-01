package com.minerva.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authRequest -> authRequest
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers("/api/planteles/**")
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "DIRECTOR_PLANTEL", "ROLE_DIRECTOR_PLANTEL"
                    )

                .requestMatchers("/api/personas/**")
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "DIRECTOR_PLANTEL", "ROLE_DIRECTOR_PLANTEL",
                        "COORDINADOR", "ROLE_COORDINADOR"
                    )

                .requestMatchers(
                        "/api/materias/**",
                        "/api/unidades/**",
                        "/api/temas/**",
                        "/api/conceptos/**",
                        "/api/preguntas/**",
                        "/api/opciones/**"
                    )
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "COORDINADOR", "ROLE_COORDINADOR",
                        "DOCENTE", "ROLE_DOCENTE"
                    )

                .requestMatchers("/api/grupos/**")
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "COORDINADOR", "ROLE_COORDINADOR",
                        "DOCENTE", "ROLE_DOCENTE"
                    )

                .requestMatchers("/api/quiz-x-pregunta/**")
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "COORDINADOR", "ROLE_COORDINADOR",
                        "DOCENTE", "ROLE_DOCENTE"
                    )
                .requestMatchers("/api/quizzes/**")
                    .hasAnyAuthority(
                        "DEV", "ROLE_DEV",
                        "DIRECTOR_GENERAL", "ROLE_DIRECTOR_GENERAL",
                        "COORDINADOR", "ROLE_COORDINADOR",
                        "DOCENTE", "ROLE_DOCENTE",
                        "ALUMNO", "ROLE_ALUMNO"
                    )

                .requestMatchers("/api/**").hasAnyAuthority("DEV", "ROLE_DEV")
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
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

    // @Value("${service.security.secure-key-username}")
    // private String SECURE_KEY_USERNAME;

    // @Value("${service.security.secure-key-password}")
    // private String SECURE_KEY_PASSWORD;

    // @Value("${service.security.secure-key-username-2}")
    // private String SECURE_KEY_USERNAME_2;

    // @Value("${service.security.secure-key-password-2}")
    // private String SECURE_KEY_PASSWORD_2;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf ->
                csrf
                .disable())
            .authorizeHttpRequests(authRequest -> authRequest
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager ->
                sessionManager
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }

    // @Bean
    // public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
    //     UserDetails admin = User.builder()
    //             .username(SECURE_KEY_USERNAME)
    //             .password(passwordEncoder.encode(SECURE_KEY_PASSWORD))
    //             .roles("ADMIN")
    //             .build();

    //     UserDetails dev = User.builder()
    //             .username(SECURE_KEY_USERNAME_2)
    //             .password(passwordEncoder.encode(SECURE_KEY_PASSWORD_2))
    //             .roles("DEV")
    //             .build();

    //     return new InMemoryUserDetailsManager(admin, dev);
    // }

}
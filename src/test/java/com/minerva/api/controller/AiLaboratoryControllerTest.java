package com.minerva.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.minerva.api.jwt.JwtAuthenticationFilter;
import com.minerva.api.jwt.JwtService;
import com.minerva.api.security.SecurityConfig;
import com.minerva.api.service.AiLaboratoryClient;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = AiLaboratoryControllerTest.TestConfig.class)
class AiLaboratoryControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private AiLaboratoryClient client;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(client);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void deniesAnonymousAndOtherRolesWithoutCallingAiService() throws Exception {
        mvc.perform(get("/api/ai/laboratory/documents/prompts")).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/ai/laboratory/documents/prompts").with(user("teacher").roles("DOCENTE")))
                .andExpect(status().isForbidden());
        mvc.perform(multipart("/api/ai/laboratory/documents/extract")
                .file(new MockMultipartFile("file", new byte[]{1}))
                .with(user("director").roles("DIRECTOR_GENERAL")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(client);
    }

    @Test
    void allowsBothDevAuthorityFormatsAndKeepsDiagnosticStatus() throws Exception {
        when(client.prompts()).thenReturn(ResponseEntity.ok(new ObjectMapper().readTree("{\"model\":\"qwen3:8b\"}")));
        for (String authority : new String[]{"DEV", "ROLE_DEV"}) {
            mvc.perform(get("/api/ai/laboratory/documents/prompts")
                    .with(user("dev").authorities(new SimpleGrantedAuthority(authority))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.model").value("qwen3:8b"));
        }
        when(client.extract(any(), any())).thenReturn(ResponseEntity.status(422)
                .body(new ObjectMapper().readTree("{\"rawOutput\":\"bad\",\"validationErrors\":[\"invalid\"]}")));
        mvc.perform(multipart("/api/ai/laboratory/documents/extract")
                .file(new MockMultipartFile("file", "plan.pdf", "application/pdf", new byte[]{1}))
                .file(new MockMultipartFile("request", "", "application/json",
                        "{\"sourceType\":\"STUDY_PLAN\"}".getBytes()))
                .with(user("dev").roles("DEV")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.rawOutput").value("bad"));
    }

    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, AiLaboratoryController.class})
    static class TestConfig {
        @Bean AiLaboratoryClient client() { return mock(AiLaboratoryClient.class); }
        @Bean AuthenticationProvider provider() { return mock(AuthenticationProvider.class); }
        @Bean JwtAuthenticationFilter jwtFilter() {
            return new JwtAuthenticationFilter(mock(JwtService.class), mock(UserDetailsService.class));
        }
    }
}

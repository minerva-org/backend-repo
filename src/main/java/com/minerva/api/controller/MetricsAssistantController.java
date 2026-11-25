package com.minerva.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.User.User;
import com.minerva.api.dto.MetricsAssistantRequestDTO;
import com.minerva.api.dto.MetricsAssistantResponseDTO;
import com.minerva.api.service.MetricsAssistantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/metrics-assistant")
@RequiredArgsConstructor
public class MetricsAssistantController {

    private final MetricsAssistantService metricsAssistantService;

    @PostMapping("/chat")
    public ResponseEntity<MetricsAssistantResponseDTO> chat(
            @Valid @RequestBody MetricsAssistantRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(metricsAssistantService.consult(request, user));
    }
}

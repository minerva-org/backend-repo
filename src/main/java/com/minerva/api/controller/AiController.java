package com.minerva.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minerva.api.service.AiServiceClient;
import com.minerva.api.service.AiServiceClient.HealthResponse;
import com.minerva.api.service.AiServiceClient.AiServiceUnavailableException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceClient aiServiceClient;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        try {
            HealthResponse status = aiServiceClient.health();
            if (!status.isUp()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
            }
            return ResponseEntity.ok(status);
        } catch (AiServiceUnavailableException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResponse("DOWN", "unknown"));
        }
    }
}

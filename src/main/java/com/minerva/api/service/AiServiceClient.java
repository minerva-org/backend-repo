package com.minerva.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(
            @Value("${app.ai.service.base-url}") String baseUrl,
            @Value("${app.ai.service.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-AI-Service-Key", apiKey)
                .build();
    }

    public HealthResponse health() {
        try {
            HealthResponse response = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(HealthResponse.class);
            if (response == null) {
                throw new AiServiceUnavailableException("AI-Service returned an empty health response.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new AiServiceUnavailableException("Could not reach AI-Service.", exception);
        }
    }

    public record HealthResponse(String status, String model) {
        public boolean isUp() {
            return "UP".equals(status);
        }
    }

    public static class AiServiceUnavailableException extends RuntimeException {
        public AiServiceUnavailableException(String message) {
            super(message);
        }

        public AiServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

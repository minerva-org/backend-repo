package com.minerva.api.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiServiceClient {

    private static final Logger LOGGER =
            Logger.getLogger(AiServiceClient.class.getName());

    private final String baseUrl;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiServiceClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.service.base-url}") String baseUrl,
            @Value("${app.ai.service.api-key}") String apiKey) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build());
        factory.setReadTimeout(Duration.ofSeconds(300));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-AI-Service-Key", apiKey)
                .requestFactory(factory)
                .build();
    }

    public JsonNode createTask(JsonNode request) {
        try {
            JsonNode response = restClient.post()
                    .uri("/v1/tareas")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.isObject()) {
                throw new AiServiceUnavailableException(
                        "AI-Service returned an invalid task response.");
            }
            return response;
        } catch (RestClientException exception) {
            logRequestFailure("POST", "/v1/tareas", exception);
            throw new AiServiceUnavailableException(
                    "Could not create AI task.", exception);
        }
    }

    public JsonNode getTask(String tareaId) {
        try {
            JsonNode response = restClient.get()
                    .uri("/v1/tareas/{id}", tareaId)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.isObject()) {
                throw new AiServiceUnavailableException(
                        "AI-Service returned an invalid task status.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new AiServiceUnavailableException(
                    "Could not read AI task.", exception);
        }
    }

    public void deleteTask(String tareaId) {
        try {
            restClient.delete()
                    .uri("/v1/tareas/{id}", tareaId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new AiServiceUnavailableException(
                    "Could not delete AI task.", exception);
        }
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

    private void logRequestFailure(
            String method,
            String path,
            RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            LOGGER.log(Level.SEVERE,
                    "AI_SERVICE_RESPUESTA_ERROR: metodo={0}, url={1}{2}, status={3}, mensaje={4}, cuerpo={5}",
                    new Object[]{method, baseUrl, path,
                            responseException.getStatusCode().value(),
                            responseException.getMessage(),
                            responseException.getResponseBodyAsString()});
            return;
        }

        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        LOGGER.log(Level.SEVERE,
                "AI_SERVICE_CONEXION_ERROR: metodo={0}, url={1}{2}, tipo={3}, mensaje={4}",
                new Object[]{method, baseUrl, path,
                        cause.getClass().getSimpleName(),
                        cause.getMessage()});
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

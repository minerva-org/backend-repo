package com.minerva.api.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiLaboratoryClient {

    private static final Set<Integer> FORWARDED_STATUSES = Set.of(200, 400, 413, 415, 422, 503);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiLaboratoryClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.service.base-url}") String baseUrl,
            @Value("${app.ai.service.api-key}") String apiKey) {
        this.objectMapper = objectMapper;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(240));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-AI-Service-Key", apiKey)
                .requestFactory(factory)
                .build();
    }

    public ResponseEntity<JsonNode> prompts() {
        return forward(restClient.get().uri("/laboratory/documents/prompts"));
    }

    public ResponseEntity<JsonNode> extract(MultipartFile file, JsonNode request) {
        var parts = new LinkedMultiValueMap<String, Object>();
        var jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        parts.add("file", file.getResource());
        parts.add("request", new HttpEntity<>(request, jsonHeaders));
        return forward(restClient.post().uri("/laboratory/documents/extract")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(parts));
    }

    private ResponseEntity<JsonNode> forward(RestClient.RequestHeadersSpec<?> request) {
        try {
            return request.exchange((sent, received) -> {
                if (!FORWARDED_STATUSES.contains(received.getStatusCode().value())) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                            "AI-Service rechazo la solicitud del laboratorio.");
                }
                JsonNode body = objectMapper.readTree(received.getBody());
                if (body == null || !body.isObject()) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                            "AI-Service devolvio una respuesta no valida.");
                }
                return ResponseEntity.status(received.getStatusCode())
                        .cacheControl(CacheControl.noStore())
                        .contentType(MediaType.APPLICATION_JSON).body(body);
            });
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar a AI-Service o se agoto el tiempo de espera.", exception);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI-Service no devolvio JSON valido.", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Fallo la comunicacion con AI-Service.", exception);
        }
    }
}

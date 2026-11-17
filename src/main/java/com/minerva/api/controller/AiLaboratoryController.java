package com.minerva.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.service.AiLaboratoryClient;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/ai/laboratory/documents")
@RequiredArgsConstructor
public class AiLaboratoryController {

    private final AiLaboratoryClient client;

    @GetMapping("/prompts")
    public ResponseEntity<JsonNode> prompts() {
        return client.prompts();
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonNode> extract(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") JsonNode request) {
        return client.extract(file, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleServiceError(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("mensaje", exception.getReason()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadTooLarge() {
        return ResponseEntity.status(HttpStatusCode.valueOf(413))
                .body(Map.of("mensaje", "El archivo supera el limite de carga permitido (10 MiB por PDF)."));
    }
}

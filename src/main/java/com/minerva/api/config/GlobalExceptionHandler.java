package com.minerva.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.minerva.api.excepciones.CuentaDeshabilitadaException;
import com.minerva.api.service.AiServiceClient.AiServiceUnavailableException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionHandler.class.getName());

    @ExceptionHandler(AiServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleAiServiceUnavailable(
            AiServiceUnavailableException ex) {
        LOGGER.log(Level.SEVERE, "AI_SERVICE_NO_DISPONIBLE: " + ex.getMessage(), ex);
        Map<String, String> body = new HashMap<>();
        body.put("mensaje",
                "El servicio de IA no está disponible en este momento. Intenta de nuevo más tarde.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler (EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CuentaDeshabilitadaException.class)
    public ResponseEntity<Map<String, String>> handleCuentaDeshabilitada(CuentaDeshabilitadaException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("codigo", ex.getCodigo());
        body.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensaje", ex.getReason() != null
                ? ex.getReason()
                : "La solicitud no pudo ser procesada.");
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        if (message != null && message.contains("uk_inscripcion_usuario_curso")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Conflict",
                            "message", "Ya existe una solicitud de este usuario al curso."
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Bad Request",
                        "message", "Violación de integridad en los datos."
                ));
    }
}
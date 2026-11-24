package com.minerva.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetricsAssistantRequestDTO(
        @NotBlank String scope,
        String entityId,
        @NotBlank @Size(max = 4_000) String prompt,
        @Valid @Size(max = 10) List<MessageDTO> messages) {

    public record MessageDTO(
            @NotBlank String role,
            @NotBlank @Size(max = 8_000) String content) {
    }
}

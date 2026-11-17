package com.minerva.api.dto;

import java.time.Instant;

public record DocumentoDTO(String id, String originalFilename, String contentType,
                        long sizeBytes, String checksumSha256, String uploadedBy,
                        Instant createdAt) {}
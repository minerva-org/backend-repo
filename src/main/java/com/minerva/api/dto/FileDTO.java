package com.minerva.api.dto;

import java.util.UUID;

public record FileDTO(UUID fileId, String fileName, String contentType, long size) {
}
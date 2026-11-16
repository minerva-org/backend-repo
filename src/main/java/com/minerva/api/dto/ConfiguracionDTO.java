package com.minerva.api.dto;

import java.time.Instant;

public record ConfiguracionDTO (String id, String valor, String descripcion, Instant fechaModificacion){}

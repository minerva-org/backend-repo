package com.minerva.api.dto;

import java.time.Instant;

import lombok.Builder;

@Builder 
public record IntentoResponse(String intentoId, String quizId, Instant fechaFinalizacion,
                                boolean finalizado, PreguntaAlumnoDTO preguntaActual) {}
package com.minerva.api.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizDTO {
    private String id;
    private String nombre;
    private Instant fechaCreacion;
    private Instant fechaInicio;
    private Instant fechaFinalizacion;
    private String grupoId;
}
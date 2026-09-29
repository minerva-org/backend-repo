package com.minerva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizXPreguntaDTO {
    private String id;
    private String quizId;
    private String preguntaId;
    private String conceptoId;
}
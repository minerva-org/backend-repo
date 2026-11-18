package com.minerva.api.dto;

public record ResumenQuizDTO(String quizId, String tipoQuiz, int numRespondieron, double promedio, int numAprobados) {}
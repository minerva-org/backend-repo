package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasTendenciaDTO(
        String quizId,
        String nombreQuiz,
        java.time.Instant fechaFinalizacion,
        double porcentajeAprobacion,
        long intentosFinalizados) {

}

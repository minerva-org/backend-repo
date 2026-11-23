
package com.minerva.api.dto;

public record EstadisticasQuizDTO(
                String quizId,
                String nombreQuiz,
                double promedioGeneral,
                double porcentajeAprobacion,
                double porcentajeReprobacion,
                double indiceFocoLimpio,
                long alumnosEvaluados,
                long intentosFinalizados,
                long incidenciasFoco,
                long intentosAprobados,
                long intentosReprobados) {
}

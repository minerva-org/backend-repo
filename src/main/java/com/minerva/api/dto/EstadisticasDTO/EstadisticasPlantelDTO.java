package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasPlantelDTO(
        Long plantelId,
        String nombrePlantel,
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
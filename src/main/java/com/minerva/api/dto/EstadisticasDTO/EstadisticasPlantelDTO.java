package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasPlantelDTO(
        Long plantelId,
        String nombrePlantel,
        double porcentajeAprobacion,
        long alumnosEvaluados,
        long intentosFinalizados,
        long incidenciasFoco) {
}
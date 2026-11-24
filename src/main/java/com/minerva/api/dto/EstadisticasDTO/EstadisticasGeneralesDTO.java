package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasGeneralesDTO(
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

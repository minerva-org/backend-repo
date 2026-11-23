package com.minerva.api.dto;

public record EstadisticasGrupoDTO(
        String grupoId,
        String nombreGrupo,
        String claveGrupo,
        String semestre,
        double promedioGeneral,
        double porcentajeAprobacion,
        double porcentajeReprobacion,
        double indiceFocoLimpio,
        long alumnosEvaluados,
        long quizzesAplicados,
        long intentosFinalizados,
        long incidenciasFoco,
        long intentosAprobados,
        long intentosReprobados) {
}
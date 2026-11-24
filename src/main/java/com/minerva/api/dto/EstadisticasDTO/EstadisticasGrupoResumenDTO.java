package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasGrupoResumenDTO(
        String grupoId,
        String nombreGrupo,
        String claveGrupo,
        String semestre,
        String docenteId,
        String nombreDocente,
        String apellidoPaterno,
        String apellidoMaterno,
        String materiaId,
        String nombreMateria,
        double porcentajeAprobacion,
        long alumnosEvaluados,
        long intentosFinalizados,
        long incidenciasFoco) {
}
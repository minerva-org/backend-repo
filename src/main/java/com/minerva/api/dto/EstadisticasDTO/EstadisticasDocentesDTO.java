package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasDocentesDTO(
        String docenteId,
        String nombreDocente,
        String apellidoPaterno,
        String apellidoMaterno,
        long quizzesAplicados,
        long intentosFinalizados) {
}

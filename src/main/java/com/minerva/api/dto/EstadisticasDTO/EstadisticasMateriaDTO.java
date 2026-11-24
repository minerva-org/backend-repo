package com.minerva.api.dto.EstadisticasDTO;

public record EstadisticasMateriaDTO(
                String materiaId,
                String nombreMateria,
                double porcentajeAprobacion,
                long intentosFinalizados,
                long intentosAprobados,
                long incidenciasFoco) {
}

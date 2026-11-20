package com.minerva.api.dto;

import java.util.List;

public record AlumnoDesgloseDTO(String alumnoId, String alumnoNombre,
                                String grupoId, String grupoClave, String grupoNombre,
                                String intentoId, boolean finalizado,
                                long correctas, long total, double calificacion,
                                List<RespuestaAlumnoDTO> respuestas) {} 
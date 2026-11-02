package com.minerva.api.dto;

import java.util.List;

public record PreguntaAlumnoDTO(String id, String descripcion, int numero, int total,
                                List<OpcionAlumnoDTO> opciones) {}

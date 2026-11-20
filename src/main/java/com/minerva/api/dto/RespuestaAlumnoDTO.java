package com.minerva.api.dto;

public record RespuestaAlumnoDTO(long numero, String preguntaId, String pregunta,
                                String conceptoId, String conceptoDescripcion,
                                String opcionElegida, String opcionCorrecta,
                                String resultado, int cambiosPantalla) {}
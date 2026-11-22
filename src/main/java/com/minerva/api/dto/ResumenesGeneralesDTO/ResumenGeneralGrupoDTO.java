package com.minerva.api.dto.ResumenesGeneralesDTO;

public record ResumenGeneralGrupoDTO (String quizId, String grupoId, String grupoClave, String grupoNombre,
                                long numPreguntas, long numIntentos, long numRespondieron,
                                long numEnCurso, long numAprobados, long numReprobados,
                                double promedio, double calificacionMaxima,
                                double calificacionMinima, double duracionPromedioSegundos) {}

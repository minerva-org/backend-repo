package com.minerva.api.dto.ResumenesGeneralesDTO;

public record ResumenGeneralPlantelDTO (String quizId, String grupoId, String grupoClave, String grupoNombre,
                                long numPreguntas, long numIntentos, long numRespondieron,
                                long numEnCurso, long numAprobados, long numReprobados,
                                double promedio, double calificacionMaxima,
                                double calificacionMinima, double duracionPromedioSegundos,
                                String idDocente, String docenteNombre) {}
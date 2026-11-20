CREATE OR REPLACE VIEW v_quiz_resumen_general AS
WITH preguntas AS (
    SELECT id_quiz, COUNT(*) AS num_preguntas
    FROM quiz_x_pregunta
    GROUP BY id_quiz
),
calificaciones AS (
    SELECT i.id AS id_intento, i.id_quiz, i.fecha_inicio, i.fecha_envio,
           COALESCE(100.0 * COUNT(d.id) FILTER (WHERE d.es_correcto)
                    / NULLIF(p.num_preguntas, 0), 0) AS calificacion
    FROM intentos i
    LEFT JOIN detalles_intentos d ON d.id_intento = i.id
    LEFT JOIN preguntas p ON p.id_quiz = i.id_quiz
    GROUP BY i.id, i.id_quiz, i.fecha_inicio, i.fecha_envio, p.num_preguntas
)
SELECT q.id AS id_quiz,
        COALESCE(p.num_preguntas, 0)                                              AS num_preguntas,
        COUNT(c.id_intento)                                                       AS num_intentos,
        COUNT(c.id_intento) FILTER (WHERE c.fecha_envio IS NOT NULL)              AS num_respondieron,
        COUNT(c.id_intento) FILTER (WHERE c.fecha_envio IS NULL)                  AS num_en_curso,
        COUNT(c.id_intento) FILTER (WHERE c.fecha_envio IS NOT NULL
                                    AND c.calificacion >= 60)                   AS num_aprobados,
        COUNT(c.id_intento) FILTER (WHERE c.fecha_envio IS NOT NULL
                                    AND c.calificacion < 60)                    AS num_reprobados,
        COALESCE(ROUND((AVG(c.calificacion) FILTER (WHERE c.fecha_envio IS NOT NULL))::numeric, 2), 0)::double precision
                                                                                AS promedio,
        COALESCE(MAX(c.calificacion) FILTER (WHERE c.fecha_envio IS NOT NULL), 0)::double precision
                                                                                AS calificacion_maxima,
        COALESCE(MIN(c.calificacion) FILTER (WHERE c.fecha_envio IS NOT NULL), 0)::double precision
                                                                                AS calificacion_minima,
        COALESCE(ROUND((AVG(EXTRACT(EPOCH FROM (c.fecha_envio - c.fecha_inicio)))
                    FILTER (WHERE c.fecha_envio IS NOT NULL))::numeric, 0), 0)::double precision
                                                                                AS duracion_promedio_segundos,
        q.id_grupo                                                                AS id_grupo,
        g.clave_grupo                                                             AS grupo_clave,
        g.nombre                                                                  AS grupo_nombre
FROM quiz q
JOIN grupo g ON g.id = q.id_grupo                                                                 
LEFT JOIN preguntas p ON p.id_quiz = q.id
LEFT JOIN calificaciones c ON c.id_quiz = q.id
GROUP BY q.id, q.id_grupo, g.clave_grupo, g.nombre, p.num_preguntas;
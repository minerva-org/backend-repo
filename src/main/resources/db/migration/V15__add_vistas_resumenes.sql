--vista para recopilar los siguientes datos
-- id 
-- id_quiz
-- id_intento
-- id_alumno
-- alumno_nombre
-- finalizado
-- numero
-- id_concepto
-- descripcion_concepto
-- id_pregunta
-- pregunta
-- id_opcion_elegida
-- opcion_elegida
-- opcion_correcta
-- resultado
-- cambios_pantalla
-- id_grupo
-- grupo_clave
-- grupo_nombre 
CREATE
OR REPLACE VIEW v_quiz_alumno_pregunta AS
SELECT
    d.id AS id,
    i.id_quiz,
    i.id AS id_intento,
    i.id_alumno,
    TRIM(
        CONCAT_WS (
            ' ',
            per.nombre,
            per.apellido_paterno,
            per.apellido_materno
        )
    ) AS alumno_nombre,
    (i.fecha_envio IS NOT NULL) AS finalizado,
    qp.numero,
    c.id AS id_concepto,
    c.detalles AS descripcion_concepto,
    d.id_pregunta,
    p.descripcion AS pregunta,
    d.id_opcion AS id_opcion_elegida,
    sel.descripcion AS opcion_elegida,
    (
        SELECT
            o.descripcion
        FROM
            opciones o
        WHERE
            o.id_pregunta = d.id_pregunta
            AND o.es_correcta
        LIMIT
            1
    ) AS opcion_correcta,
    CASE
        WHEN d.id_opcion IS NULL THEN 'EN_BLANCO'
        WHEN d.es_correcto THEN 'CORRECTA'
        ELSE 'INCORRECTA'
    END AS resultado,
    COALESCE(d.cambios_pantalla, 0) AS cambios_pantalla,
    q.id_grupo AS id_grupo,
    g.clave_grupo AS grupo_clave,
    g.nombre AS grupo_nombre
FROM
    detalles_intentos d
    JOIN intentos i ON i.id = d.id_intento
    JOIN quiz q ON q.id = i.id_quiz
    JOIN grupo g ON g.id = q.id_grupo
    JOIN personas per ON per.id = i.id_alumno
    JOIN pregunta p ON p.id = d.id_pregunta
    JOIN concepto c ON c.id = p.id_concepto
    JOIN (
        SELECT
            id_quiz,
            id_pregunta,
            ROW_NUMBER() OVER (
                PARTITION BY
                    id_quiz
                ORDER BY
                    id
            ) AS numero
        FROM
            quiz_x_pregunta
    ) qp ON qp.id_quiz = i.id_quiz
    AND qp.id_pregunta = d.id_pregunta
    LEFT JOIN opciones sel ON sel.id = d.id_opcion;
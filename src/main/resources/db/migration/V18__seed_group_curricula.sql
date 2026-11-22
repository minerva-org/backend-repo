-- Inicializa el curriculum independiente de cada grupo existente.
-- No reemplaza curriculos que ya hayan sido creados o modificados.

INSERT INTO unidad_grupo (
    id, nombre, id_materia, id_grupo, id_unidad_origen, activo
)
SELECT
    'UG-' || g.id || '-' || u.id,
    u.detalles,
    g.id_materia,
    g.id,
    u.id,
    TRUE
FROM grupo g
JOIN unidad u ON u.id_materia = g.id_materia
WHERE u.activo = TRUE
ON CONFLICT (id) DO NOTHING;

INSERT INTO tema_grupo (
    id, clave, nombre, id_unidad_grupo, id_tema_origen, activo
)
SELECT
    'TG-' || ug.id || '-' || t.id,
    NULL,
    t.detalles,
    ug.id,
    t.id,
    TRUE
FROM unidad_grupo ug
JOIN unidad u ON u.id = ug.id_unidad_origen
JOIN tema t ON t.id_unidad = u.id
WHERE ug.activo = TRUE
  AND t.activo = TRUE
ON CONFLICT (id) DO NOTHING;

INSERT INTO concepto_grupo (
    id, nombre, id_tema_grupo, id_concepto_origen, activo
)
SELECT
    'CG-' || tg.id || '-' || c.id,
    c.detalles,
    tg.id,
    c.id,
    TRUE
FROM tema_grupo tg
JOIN tema t ON t.id = tg.id_tema_origen
JOIN concepto c ON c.id_tema = t.id
WHERE tg.activo = TRUE
  AND c.activo = TRUE
ON CONFLICT (id) DO NOTHING;

-- Materias funcionales con árbol completo para pruebas por plantel.
INSERT INTO plan_estudio (detalles)
VALUES ('Ingeniería de Software')
ON CONFLICT (detalles) DO NOTHING;

INSERT INTO materia (id, detalles, prefijo, id_plan_estudio, activo)
VALUES ('MAT-SW', 'Ingeniería de Software', 'SWE', 'Ingeniería de Software', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO unidad (id, detalles, id_materia, activo)
VALUES
    ('UNI-SW-1', 'Proceso de software', 'MAT-SW', TRUE),
    ('UNI-SW-2', 'Análisis y diseño', 'MAT-SW', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tema (id, detalles, id_unidad, activo)
VALUES
    ('TEM-SW-1', 'Metodologías ágiles', 'UNI-SW-1', TRUE),
    ('TEM-SW-2', 'Requisitos', 'UNI-SW-2', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO concepto (id, detalles, id_tema, activo)
VALUES
    ('CON-SW-1', 'Scrum', 'TEM-SW-1', TRUE),
    ('CON-SW-2', 'Historias de usuario', 'TEM-SW-2', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO materia (id, detalles, prefijo, id_plan_estudio, activo)
VALUES ('MAT-DB', 'Bases de Datos Avanzadas', 'DBA', 'Ingeniería de Software', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO unidad (id, detalles, id_materia, activo)
VALUES
    ('UNI-DB-1', 'Modelado', 'MAT-DB', TRUE),
    ('UNI-DB-2', 'Optimización', 'MAT-DB', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tema (id, detalles, id_unidad, activo)
VALUES
    ('TEM-DB-1', 'Modelo relacional', 'UNI-DB-1', TRUE),
    ('TEM-DB-2', 'Índices', 'UNI-DB-2', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO concepto (id, detalles, id_tema, activo)
VALUES
    ('CON-DB-1', 'Normalización', 'TEM-DB-1', TRUE),
    ('CON-DB-2', 'Planes de ejecución', 'TEM-DB-2', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Un grupo por plantel para las materias nuevas.
INSERT INTO grupo (
    id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo
)
SELECT
    'GRP-SW-' || p.id,
    'SW-' || p.id,
    'Ingeniería de Software ' || p.id,
    '1',
    dp.id,
    'MAT-SW',
    p.id,
    TRUE
FROM planteles p
CROSS JOIN LATERAL (
    SELECT dp.id
    FROM personas dp
    WHERE dp.id_plantel = p.id
      AND dp.rol = 'DOCENTE'
      AND dp.activo = TRUE
    ORDER BY dp.id
    LIMIT 1
) dp
ON CONFLICT (id) DO NOTHING;

INSERT INTO grupo (
    id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo
)
SELECT
    'GRP-DB-' || p.id,
    'DB-' || p.id,
    'Bases de Datos ' || p.id,
    '2',
    dp.id,
    'MAT-DB',
    p.id,
    TRUE
FROM planteles p
CROSS JOIN LATERAL (
    SELECT dp.id
    FROM personas dp
    WHERE dp.id_plantel = p.id
      AND dp.rol = 'DOCENTE'
      AND dp.activo = TRUE
    ORDER BY dp.id
    LIMIT 1
) dp
ON CONFLICT (id) DO NOTHING;

-- Inicializa el árbol independiente de los grupos nuevos.
INSERT INTO unidad_grupo (
    id, nombre, id_materia, id_grupo, id_unidad_origen, activo
)
SELECT
    'UG-' || g.id || '-' || u.id,
    u.detalles,
    g.id_materia,
    g.id,
    u.id,
    TRUE
FROM grupo g
JOIN unidad u ON u.id_materia = g.id_materia
WHERE g.id LIKE 'GRP-SW-%'
   OR g.id LIKE 'GRP-DB-%'
ON CONFLICT (id) DO NOTHING;

INSERT INTO tema_grupo (
    id, clave, nombre, id_unidad_grupo, id_tema_origen, activo
)
SELECT
    'TG-' || ug.id || '-' || t.id,
    NULL,
    t.detalles,
    ug.id,
    t.id,
    TRUE
FROM unidad_grupo ug
JOIN unidad u ON u.id = ug.id_unidad_origen
JOIN tema t ON t.id_unidad = u.id
WHERE ug.id LIKE 'UG-GRP-SW-%'
   OR ug.id LIKE 'UG-GRP-DB-%'
ON CONFLICT (id) DO NOTHING;

INSERT INTO concepto_grupo (
    id, nombre, id_tema_grupo, id_concepto_origen, activo
)
SELECT
    'CG-' || tg.id || '-' || c.id,
    c.detalles,
    tg.id,
    c.id,
    TRUE
FROM tema_grupo tg
JOIN tema t ON t.id = tg.id_tema_origen
JOIN concepto c ON c.id_tema = t.id
WHERE tg.id LIKE 'TG-UG-GRP-SW-%'
   OR tg.id LIKE 'TG-UG-GRP-DB-%'
ON CONFLICT (id) DO NOTHING;

-- Seed definitivo reproducible para visualizacion.
-- Incluye su propio arbol curricular para que no dependa de datos historicos de V21.

INSERT INTO plan_estudio (detalles)
VALUES ('Ingenieria en Sistemas Computacionales')
ON CONFLICT (detalles) DO NOTHING;

INSERT INTO materia (id, detalles, prefijo, id_plan_estudio, activo)
VALUES
    ('ISC-MAT-SOFT', 'Ingenieria de Software', 'ISW', 'Ingenieria en Sistemas Computacionales', TRUE),
    ('ISC-MAT-BD', 'Bases de Datos', 'BDD', 'Ingenieria en Sistemas Computacionales', TRUE),
    ('ISC-MAT-EDD', 'Estructuras de Datos', 'EDD', 'Ingenieria en Sistemas Computacionales', TRUE)
ON CONFLICT (id) DO UPDATE SET
    detalles = EXCLUDED.detalles,
    prefijo = EXCLUDED.prefijo,
    id_plan_estudio = EXCLUDED.id_plan_estudio,
    activo = EXCLUDED.activo;

INSERT INTO unidad (id, detalles, id_materia, activo)
VALUES
    ('ISC-UNI-SW1', 'Proceso de desarrollo de software', 'ISC-MAT-SOFT', TRUE),
    ('ISC-UNI-SW2', 'Requisitos y modelado', 'ISC-MAT-SOFT', TRUE),
    ('ISC-UNI-BD1', 'Diseno relacional', 'ISC-MAT-BD', TRUE),
    ('ISC-UNI-BD2', 'Consultas y optimizacion', 'ISC-MAT-BD', TRUE),
    ('ISC-UNI-EDD1', 'Arreglos y estructuras lineales', 'ISC-MAT-EDD', TRUE)
ON CONFLICT (id) DO UPDATE SET
    detalles = EXCLUDED.detalles,
    id_materia = EXCLUDED.id_materia,
    activo = EXCLUDED.activo;

INSERT INTO tema (id, detalles, id_unidad, activo)
VALUES
    ('ISC-TEM-SCRUM', 'Metodologias agiles', 'ISC-UNI-SW1', TRUE),
    ('ISC-TEM-HIST', 'Historias de usuario', 'ISC-UNI-SW2', TRUE),
    ('ISC-TEM-NORM', 'Normalizacion de bases de datos', 'ISC-UNI-BD1', TRUE),
    ('ISC-TEM-IND', 'Indices y planes de ejecucion', 'ISC-UNI-BD2', TRUE),
    ('ISC-TEM-LIST', 'Listas y pilas', 'ISC-UNI-EDD1', TRUE)
ON CONFLICT (id) DO UPDATE SET
    detalles = EXCLUDED.detalles,
    id_unidad = EXCLUDED.id_unidad,
    activo = EXCLUDED.activo;

INSERT INTO concepto (id, detalles, id_tema, activo)
VALUES
    ('ISC-CON-SPRINT', 'Sprint y Product Backlog', 'ISC-TEM-SCRUM', TRUE),
    ('ISC-CON-HISTORIA', 'Criterios de aceptacion', 'ISC-TEM-HIST', TRUE),
    ('ISC-CON-NORMAL', 'Dependencias funcionales', 'ISC-TEM-NORM', TRUE),
    ('ISC-CON-INDICE', 'Plan de ejecucion y indices', 'ISC-TEM-IND', TRUE),
    ('ISC-CON-LISTA', 'Operaciones de listas enlazadas', 'ISC-TEM-LIST', TRUE)
ON CONFLICT (id) DO UPDATE SET
    detalles = EXCLUDED.detalles,
    id_tema = EXCLUDED.id_tema,
    activo = EXCLUDED.activo;

-- Arbol independiente que consume el canvas curricular de cada grupo.
INSERT INTO unidad_grupo (
    id, nombre, id_materia, id_grupo, id_unidad_origen, activo
)
SELECT
    'ISC-UG-' || g.id || '-' || u.id,
    u.detalles,
    g.id_materia,
    g.id,
    u.id,
    TRUE
FROM grupo g
JOIN unidad u ON u.id_materia = g.id_materia
WHERE g.id LIKE 'ISC-GRP-%'
ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    id_materia = EXCLUDED.id_materia,
    id_grupo = EXCLUDED.id_grupo,
    id_unidad_origen = EXCLUDED.id_unidad_origen,
    activo = EXCLUDED.activo;

INSERT INTO tema_grupo (
    id, clave, nombre, id_unidad_grupo, id_tema_origen, activo
)
SELECT
    'ISC-TG-' || ug.id || '-' || t.id,
    CASE t.id
        WHEN 'ISC-TEM-SCRUM' THEN 'SCRUM'
        WHEN 'ISC-TEM-HIST' THEN 'HISTORIA'
        WHEN 'ISC-TEM-NORM' THEN 'NORMAL'
        WHEN 'ISC-TEM-IND' THEN 'OPT'
        ELSE 'EDD'
    END,
    t.detalles,
    ug.id,
    t.id,
    TRUE
FROM unidad_grupo ug
JOIN unidad u ON u.id = ug.id_unidad_origen
JOIN tema t ON t.id_unidad = u.id
WHERE ug.id LIKE 'ISC-UG-%'
ON CONFLICT (id) DO UPDATE SET
    clave = EXCLUDED.clave,
    nombre = EXCLUDED.nombre,
    id_unidad_grupo = EXCLUDED.id_unidad_grupo,
    id_tema_origen = EXCLUDED.id_tema_origen,
    activo = EXCLUDED.activo;

INSERT INTO concepto_grupo (
    id, nombre, id_tema_grupo, id_concepto_origen, activo
)
SELECT
    'ISC-CG-' || tg.id || '-' || c.id,
    c.detalles,
    tg.id,
    c.id,
    TRUE
FROM tema_grupo tg
JOIN tema t ON t.id = tg.id_tema_origen
JOIN concepto c ON c.id_tema = t.id
WHERE tg.id LIKE 'ISC-TG-%'
ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    id_tema_grupo = EXCLUDED.id_tema_grupo,
    id_concepto_origen = EXCLUDED.id_concepto_origen,
    activo = EXCLUDED.activo;

INSERT INTO contenido_grupo (id, descripcion, id_concepto_grupo)
SELECT
    'ISC-CONT-' || cg.id,
    CASE cg.id_concepto_origen
        WHEN 'ISC-CON-SPRINT' THEN
            'Scrum organiza el trabajo en Sprints con incrementos revisables. El Product Backlog contiene necesidades priorizadas por el Product Owner.'
        WHEN 'ISC-CON-HISTORIA' THEN
            'Una historia de usuario describe una necesidad desde el rol del usuario y usa criterios de aceptacion verificables para definir su finalizacion.'
        WHEN 'ISC-CON-NORMAL' THEN
            'La normalizacion reduce redundancia y anomalias. La tercera forma normal elimina dependencias transitivas entre atributos no clave.'
        WHEN 'ISC-CON-INDICE' THEN
            'Un indice mejora accesos selectivos, pero aumenta escrituras y almacenamiento. El plan de ejecucion permite evaluar recorridos y costos.'
        ELSE
            'Una lista enlazada representa elementos mediante nodos y referencias. Las pilas siguen el principio LIFO y tienen operaciones push y pop.'
    END,
    cg.id
FROM concepto_grupo cg
WHERE cg.id LIKE 'ISC-CG-%'
ON CONFLICT (id) DO UPDATE SET
    descripcion = EXCLUDED.descripcion,
    id_concepto_grupo = EXCLUDED.id_concepto_grupo;

-- Treinta alumnos realistas de Ingenieria en Sistemas Computacionales.
WITH datos(n, nombre, paterno, materno) AS (
    VALUES
        (1, 'Valeria', 'Sanchez', 'Mendoza'), (2, 'Mateo', 'Ramirez', 'Torres'),
        (3, 'Camila', 'Lopez', 'Gomez'), (4, 'Sebastian', 'Hernandez', 'Vargas'),
        (5, 'Mariana', 'Martinez', 'Silva'), (6, 'Leonardo', 'Garcia', 'Reyes'),
        (7, 'Regina', 'Perez', 'Flores'), (8, 'Emiliano', 'Gonzalez', 'Cruz'),
        (9, 'Sofia', 'Rodriguez', 'Navarro'), (10, 'Daniel', 'Morales', 'Castillo'),
        (11, 'Fernanda', 'Vazquez', 'Rojas'), (12, 'Santiago', 'Jimenez', 'Paredes'),
        (13, 'Ximena', 'Ruiz', 'Ortega'), (14, 'Diego', 'Diaz', 'Contreras'),
        (15, 'Paula', 'Castro', 'Mendez'), (16, 'Javier', 'Ortiz', 'Salazar'),
        (17, 'Natalia', 'Gutierrez', 'Campos'), (18, 'Andres', 'Rojas', 'Peña'),
        (19, 'Renata', 'Chavez', 'Luna'), (20, 'Carlos', 'Acosta', 'Nunez'),
        (21, 'Daniela', 'Molina', 'Ibarra'), (22, 'Hugo', 'Santos', 'Beltran'),
        (23, 'Lucia', 'Cortes', 'Estrada'), (24, 'Bruno', 'Herrera', 'Galindo'),
        (25, 'Emma', 'Aguilar', 'Soto'), (26, 'Ian', 'Delgado', 'Miranda'),
        (27, 'Victoria', 'Pacheco', 'Rivas'), (28, 'Gael', 'Bravo', 'Fuentes'),
        (29, 'Elena', 'Macias', 'Cabrera'), (30, 'Tomas', 'Vega', 'Aguirre')
)
INSERT INTO personas (
    id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo
)
SELECT
    'ISC-ALU-' || LPAD(n::text, 3, '0'),
    nombre,
    paterno,
    materno,
    lower(nombre) || '.' || lower(replace(paterno, ' ', '.')) || '@minerva.local',
    'ALUMNO',
    CASE WHEN n <= 15 THEN 1 ELSE 2 END,
    TRUE
FROM datos
ON CONFLICT (id) DO UPDATE SET
    nombre = EXCLUDED.nombre,
    apellido_paterno = EXCLUDED.apellido_paterno,
    apellido_materno = EXCLUDED.apellido_materno,
    email = EXCLUDED.email,
    rol = EXCLUDED.rol,
    id_plantel = EXCLUDED.id_plantel,
    activo = EXCLUDED.activo;

INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
SELECT
    'isc.alumno.' || LPAD(n::text, 3, '0'),
    '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa',
    FALSE,
    'ISC-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 30) AS n
ON CONFLICT (username) DO NOTHING;

INSERT INTO alumnos (id, tutor, numero_tutor, fecha_ingreso, promedio_general)
SELECT
    'ISC-ALU-' || LPAD(n::text, 3, '0'),
    'Tutor familiar ' || LPAD(n::text, 3, '0'),
    '33' || LPAD((10000000 + n)::text, 8, '0'),
    NOW() - ((n % 18) + 180) * INTERVAL '1 day',
    ROUND((68 + (n * 7 % 28))::numeric, 1)::real
FROM generate_series(1, 30) AS n
ON CONFLICT (id) DO UPDATE SET
    promedio_general = EXCLUDED.promedio_general,
    fecha_ingreso = EXCLUDED.fecha_ingreso,
    tutor = EXCLUDED.tutor,
    numero_tutor = EXCLUDED.numero_tutor;

INSERT INTO grupo (id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo)
VALUES
    ('ISC-GRP-A', 'ISC-4A', 'Sistemas 4A', '4', 'DOC-001', 'ISC-MAT-SOFT', 1, TRUE),
    ('ISC-GRP-B', 'ISC-5B', 'Sistemas 5B', '5', 'DOC-001', 'ISC-MAT-BD', 1, TRUE),
    ('ISC-GRP-C', 'ISC-6A', 'Sistemas 6A', '6', 'DOC-002', 'ISC-MAT-EDD', 2, TRUE)
ON CONFLICT (id) DO UPDATE SET
    clave_grupo = EXCLUDED.clave_grupo,
    nombre = EXCLUDED.nombre,
    semestre = EXCLUDED.semestre,
    id_docente = EXCLUDED.id_docente,
    id_materia = EXCLUDED.id_materia,
    id_plantel = EXCLUDED.id_plantel,
    activo = EXCLUDED.activo;

-- Cada grupo tiene 20 alumnos. Se comparte exactamente un bloque de 10 entre pares.
INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'ISC-GRP-A', 'ISC-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 20) AS n
ON CONFLICT DO NOTHING;

INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'ISC-GRP-B', 'ISC-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(11, 30) AS n
ON CONFLICT DO NOTHING;

INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'ISC-GRP-C', 'ISC-ALU-' || LPAD(n::text, 3, '0')
FROM (SELECT n FROM generate_series(1, 10) AS n UNION ALL SELECT n FROM generate_series(21, 30) AS n) d(n)
ON CONFLICT DO NOTHING;

-- Veinte preguntas curriculares de Sistemas.
WITH preguntas(n, texto, concepto_id) AS (
    VALUES
        (1, '¿Que documento prioriza el trabajo que puede ejecutar el equipo?', 'ISC-CON-SPRINT'),
        (2, '¿Que representa un Sprint en Scrum?', 'ISC-CON-SPRINT'),
        (3, '¿Quien prioriza el Product Backlog?', 'ISC-CON-SPRINT'),
        (4, '¿Que finalidad tiene una Daily Scrum?', 'ISC-CON-SPRINT'),
        (5, '¿Que resultado busca una retrospectiva?', 'ISC-CON-SPRINT'),
        (6, '¿Que describe una historia de usuario?', 'ISC-CON-HISTORIA'),
        (7, '¿Que contiene un criterio de aceptacion?', 'ISC-CON-HISTORIA'),
        (8, '¿Cuando se considera terminada una historia de usuario?', 'ISC-CON-HISTORIA'),
        (9, '¿Que permite dividir una historia demasiado grande?', 'ISC-CON-HISTORIA'),
        (10, '¿Que formato facilita escribir una historia de usuario?', 'ISC-CON-HISTORIA'),
        (11, '¿Que busca la segunda forma normal?', 'ISC-CON-NORMAL'),
        (12, '¿Que elimina una dependencia parcialmente funcional?', 'ISC-CON-NORMAL'),
        (13, '¿Para que sirve una clave primaria?', 'ISC-CON-NORMAL'),
        (14, '¿Que garantiza una clave foranea?', 'ISC-CON-NORMAL'),
        (15, '¿Que problema reduce la normalizacion?', 'ISC-CON-NORMAL'),
        (16, '¿Que estructura acelera una consulta relacional?', 'ISC-CON-INDICE'),
        (17, '¿Que muestra un plan de ejecucion?', 'ISC-CON-INDICE'),
        (18, '¿Que efecto puede tener un indice innecesario?', 'ISC-CON-INDICE'),
        (19, '¿Que ocurre ante una ausencia de indice en una busqueda frecuente?', 'ISC-CON-INDICE'),
        (20, '¿Que cardinalidad favorece la selectividad de un indice?', 'ISC-CON-INDICE')
)
INSERT INTO pregunta (id, descripcion, id_concepto, activo)
SELECT
    'ISC-PREG-' || LPAD(n::text, 3, '0'),
    texto,
    concepto_id,
    TRUE
FROM preguntas
ON CONFLICT (id) DO UPDATE SET
    descripcion = EXCLUDED.descripcion,
    id_concepto = EXCLUDED.id_concepto,
    activo = EXCLUDED.activo;

CREATE TEMP TABLE isc_respuestas AS
WITH respuestas(n, correcta, distractor_1, distractor_2, distractor_3) AS (
    VALUES
        (1, 'El Product Backlog', 'El manual de usuario final', 'La licencia del servidor', 'El diagrama fisico'),
        (2, 'Un incremento acotado de trabajo', 'Una fase que no admite cambios', 'Una reunion anual', 'Un documento contractual'),
        (3, 'El Product Owner', 'El Scrum Master', 'El equipo completo', 'El cliente externo'),
        (4, 'Sincronizar avances e impedimentos', 'Firmar el acta del proyecto', 'Definir la arquitectura fisica', 'Calificar al equipo'),
        (5, 'Identificar mejoras de proceso', 'Cancelar el siguiente sprint', 'Cerrar historias sin revision', 'Cambiar el Product Owner'),
        (6, 'Una necesidad desde la perspectiva del usuario', 'Un script de base de datos', 'Una prueba tecnica sin contexto', 'Una tabla de capacitacion'),
        (7, 'Condiciones verificables de finalizacion', 'Precios de licencias', 'Nombres de desarrolladores', 'Direcciones IP'),
        (8, 'Cuando cumple sus criterios de aceptacion', 'Cuando inicia el desarrollo', 'Cuando se crea el backlog', 'Cuando finaliza el semestre'),
        (9, 'Dividirla por criterios funcionales', 'Agregar mas campos sin validacion', 'Convertirla en una reunion', 'Duplicarla en varios sprint'),
        (10, 'Como un rol desea lograr una meta', 'Como un diagrama entidad-relacion', 'Como una lista infinita', 'Como una especificacion binaria'),
        (11, 'Eliminar dependencias respecto a atributos parciales', 'Eliminar toda relacion entre tablas', 'Duplicar claves foraneas', 'Crear una sola tabla'),
        (12, 'La descomposicion de la tabla', 'La concatenacion de filas', 'La eliminacion de claves primarias', 'La agregacion de datos'),
        (13, 'Identificar de forma unica cada registro', 'Definir permisos del servidor', 'Codificar archivos adjuntos', 'Ordenar la tabla fisicamente'),
        (14, 'Mantener la integridad referencial', 'Evitar cualquier consulta', 'Definir tipos de archivos', 'Ocultar registros'),
        (15, 'Redundancia y anomalias de actualizacion', 'Disenos responsivos', 'Pruebas unitarias', 'Variables de entorno'),
        (16, 'Un indice apropiado', 'Un archivo de log', 'Una constante global', 'Una tabla temporal vacia'),
        (17, 'Operaciones y costos estimados de la consulta', 'Un registro de usuarios del sistema', 'El codigo fuente del programa', 'Las credenciales de la base'),
        (18, 'Reducir el costo de escritura y ocupar mas espacio', 'Aumentar siempre la velocidad', 'Eliminar consultas lentas', 'Cambiar la clave primaria'),
        (19, 'Escaneo completo y mayor tiempo de respuesta', 'Ordenamiento automatico sin costo', 'Conexion permanente al disco', 'Creacion automatica de claves'),
        (20, 'Alta selectividad con pocos registros coincidentes', 'Distribucion uniforme sin condiciones', 'Valores iguales en todas las filas', 'Una columna que nunca se consulta')
)
SELECT * FROM respuestas;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
SELECT 'ISC-OPC-' || LPAD(n::text, 3, '0') || '-A', correcta, TRUE,
       'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM isc_respuestas
ON CONFLICT (id) DO UPDATE SET descripcion = EXCLUDED.descripcion, es_correcta = EXCLUDED.es_correcta, id_pregunta = EXCLUDED.id_pregunta;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
SELECT 'ISC-OPC-' || LPAD(n::text, 3, '0') || '-B', distractor_1, FALSE,
       'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM isc_respuestas
ON CONFLICT (id) DO UPDATE SET descripcion = EXCLUDED.descripcion, es_correcta = EXCLUDED.es_correcta, id_pregunta = EXCLUDED.id_pregunta;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
SELECT 'ISC-OPC-' || LPAD(n::text, 3, '0') || '-C', distractor_2, FALSE,
       'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM isc_respuestas
ON CONFLICT (id) DO UPDATE SET descripcion = EXCLUDED.descripcion, es_correcta = EXCLUDED.es_correcta, id_pregunta = EXCLUDED.id_pregunta;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
SELECT 'ISC-OPC-' || LPAD(n::text, 3, '0') || '-D', distractor_3, FALSE,
       'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM isc_respuestas
ON CONFLICT (id) DO UPDATE SET descripcion = EXCLUDED.descripcion, es_correcta = EXCLUDED.es_correcta, id_pregunta = EXCLUDED.id_pregunta;

INSERT INTO quiz (id, detalles, fecha_creacion, fecha_inicio, fecha_finalizacion, id_grupo)
VALUES
    ('ISC-QZ-05', 'Diagnostico de Metodologias Agiles', NOW() - INTERVAL '61 days', NOW() - INTERVAL '60 days', NOW() - INTERVAL '58 days', 'ISC-GRP-A'),
    ('ISC-QZ-10', 'Evaluacion de Diseno y Consultas SQL', NOW() - INTERVAL '46 days', NOW() - INTERVAL '45 days', NOW() - INTERVAL '43 days', 'ISC-GRP-B'),
    ('ISC-QZ-20', 'Integracion de Ingenieria y Datos', NOW() - INTERVAL '31 days', NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days', 'ISC-GRP-C'),
    ('ISC-QZ-10B', 'Repaso de Scrum y Bases de Datos', NOW() - INTERVAL '16 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '13 days', 'ISC-GRP-A')
ON CONFLICT (id) DO UPDATE SET
    detalles = EXCLUDED.detalles,
    fecha_creacion = EXCLUDED.fecha_creacion,
    fecha_inicio = EXCLUDED.fecha_inicio,
    fecha_finalizacion = EXCLUDED.fecha_finalizacion,
    id_grupo = EXCLUDED.id_grupo;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT 'ISC-QXP-05-' || LPAD(n::text, 3, '0'), 'ISC-QZ-05', 'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 5) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT 'ISC-QXP-10-' || LPAD(n::text, 3, '0'), 'ISC-QZ-10', 'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(6, 15) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT 'ISC-QXP-20-' || LPAD(n::text, 3, '0'), 'ISC-QZ-20', 'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 20) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT 'ISC-QXP-10B-' || LPAD(n::text, 3, '0'), 'ISC-QZ-10B',
       'ISC-PREG-' || LPAD(n::text, 3, '0')
FROM (SELECT n FROM generate_series(1, 5) AS n UNION ALL SELECT n FROM generate_series(16, 20) AS n) d(n)
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

-- Intentos reales y no entregas. Quedan tres alumnos sin entrega por quiz.
WITH miembros AS (
    SELECT 'ISC-QZ-05' AS quiz_id, 'ISC-ALU-' || LPAD(n::text, 3, '0') AS alumno_id
    FROM generate_series(1, 20) AS n
    WHERE n NOT IN (3, 18, 19)
    UNION ALL
    SELECT 'ISC-QZ-10', 'ISC-ALU-' || LPAD(n::text, 3, '0')
    FROM generate_series(11, 30) AS n
    WHERE n NOT IN (11, 25, 30)
    UNION ALL
    SELECT 'ISC-QZ-20', 'ISC-ALU-' || LPAD(n::text, 3, '0')
    FROM (SELECT n FROM generate_series(1, 10) AS n UNION ALL SELECT n FROM generate_series(21, 30) AS n) d(n)
    WHERE n NOT IN (5, 22, 29)
    UNION ALL
    SELECT 'ISC-QZ-10B', 'ISC-ALU-' || LPAD(n::text, 3, '0')
    FROM generate_series(1, 20) AS n
    WHERE n NOT IN (1, 14, 17)
)
INSERT INTO intentos (id, id_quiz, id_alumno, fecha_inicio, fecha_envio)
SELECT
    CASE quiz_id
        WHEN 'ISC-QZ-05' THEN 'ISC-INT-05-' || RIGHT(alumno_id, 3)
        WHEN 'ISC-QZ-10' THEN 'ISC-INT-10-' || RIGHT(alumno_id, 3)
        WHEN 'ISC-QZ-20' THEN 'ISC-INT-20-' || RIGHT(alumno_id, 3)
        WHEN 'ISC-QZ-10B' THEN 'ISC-INT-10B-' || RIGHT(alumno_id, 3)
    END,
    quiz_id,
    alumno_id,
    NOW() - INTERVAL '40 minutes',
    NOW() - INTERVAL '5 minutes'
FROM miembros
ON CONFLICT (id_quiz, id_alumno) DO NOTHING;

-- Foco alto en los alumnos 001 y 002; 0-1 incidencia para el resto.
WITH payload AS (
    SELECT i.id AS intento_id, i.id_alumno AS alumno_id,
           qp.id_pregunta AS pregunta_id,
           ROW_NUMBER() OVER (PARTITION BY i.id ORDER BY qp.id) AS posicion
    FROM intentos i
    JOIN quiz_x_pregunta qp ON qp.id_quiz = i.id_quiz
    WHERE i.id LIKE 'ISC-INT-%'
)
INSERT INTO detalles_intentos (
    id, id_intento, id_pregunta, cambios_pantalla, id_opcion, es_correcto
)
SELECT
    'ISC-DET-' || intento_id || '-' || LPAD(posicion::text, 2, '0'),
    intento_id,
    pregunta_id,
    CASE
        WHEN alumno_id IN ('ISC-ALU-001', 'ISC-ALU-002')
            THEN 2 + ABS(MOD(HASHTEXT(intento_id || pregunta_id), 2))
        ELSE ABS(MOD(HASHTEXT(intento_id || pregunta_id), 3)) / 2
    END,
    CASE
        WHEN ABS(MOD(HASHTEXT(intento_id || pregunta_id), 17)) = 0 THEN NULL
        WHEN ABS(MOD(HASHTEXT(intento_id || pregunta_id), 5)) < 3 THEN correcta.id
        ELSE incorrecta.id
    END,
    CASE
        WHEN ABS(MOD(HASHTEXT(intento_id || pregunta_id), 17)) = 0 THEN FALSE
        ELSE ABS(MOD(HASHTEXT(intento_id || pregunta_id), 5)) < 3
    END
FROM payload
LEFT JOIN LATERAL (
    SELECT o.id FROM opciones o
    WHERE o.id_pregunta = payload.pregunta_id AND o.es_correcta = TRUE
    LIMIT 1
) correcta ON TRUE
LEFT JOIN LATERAL (
    SELECT o.id FROM opciones o
    WHERE o.id_pregunta = payload.pregunta_id AND o.es_correcta = FALSE
    ORDER BY o.id
    LIMIT 1
) incorrecta ON TRUE
ON CONFLICT (id_intento, id_pregunta) DO NOTHING;

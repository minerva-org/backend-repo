-- Datos reproducibles para visualizar grupos, resultados de quizzes y incidencias de foco.

-- 30 alumnos; los tres grupos tienen 20 alumnos cada uno y se solapan en bloques de 10.
INSERT INTO personas (
    id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo
)
SELECT
    'DEMO-ALU-' || LPAD(n::text, 3, '0'),
    'Alumno' || LPAD(n::text, 3, '0'),
    'Visual',
    CASE WHEN n % 2 = 0 THEN 'Norte' ELSE 'Sur' END,
    'demo.alumno' || LPAD(n::text, 3, '0') || '@minerva.local',
    'ALUMNO',
    CASE WHEN n <= 15 THEN 1 ELSE 2 END,
    TRUE
FROM generate_series(1, 30) AS n
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
SELECT
    'demo.alumno' || LPAD(n::text, 3, '0'),
    '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa',
    FALSE,
    'DEMO-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 30) AS n
ON CONFLICT (username) DO NOTHING;

INSERT INTO alumnos (
    id, tutor, numero_tutor, fecha_ingreso, promedio_general
)
SELECT
    'DEMO-ALU-' || LPAD(n::text, 3, '0'),
    'Tutor ' || LPAD(n::text, 3, '0'),
    '33' || LPAD((10000000 + n)::text, 8, '0'),
    NOW() - ((31 - n) || ' days')::interval,
    ROUND((70 + (n * 7 % 25))::numeric, 1)::real
FROM generate_series(1, 30) AS n
ON CONFLICT (id) DO NOTHING;

INSERT INTO grupo (
    id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo
)
VALUES
    ('DEMO-GRP-A', 'DEMO-A', 'Visualización Grupo A', '5', 'DOC-001', 'MAT-SW', 1, TRUE),
    ('DEMO-GRP-B', 'DEMO-B', 'Visualización Grupo B', '5', 'DOC-001', 'MAT-DB', 1, TRUE),
    ('DEMO-GRP-C', 'DEMO-C', 'Visualización Grupo C', '5', 'DOC-002', 'MAT-SW', 2, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Grupo A: alumnos 001-020.
INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'DEMO-GRP-A', 'DEMO-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 20) AS n
ON CONFLICT DO NOTHING;

-- Grupo B: alumnos 011-030.
INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'DEMO-GRP-B', 'DEMO-ALU-' || LPAD(n::text, 3, '0')
FROM generate_series(11, 30) AS n
ON CONFLICT DO NOTHING;

-- Grupo C: alumnos 001-010 y 021-030.
INSERT INTO grupos_alumnos (id_grupo, id_alumno)
SELECT 'DEMO-GRP-C', 'DEMO-ALU-' || LPAD(n::text, 3, '0')
FROM (
    SELECT n FROM generate_series(1, 10) AS n
    UNION ALL
    SELECT n FROM generate_series(21, 30) AS n
) datos
ON CONFLICT DO NOTHING;

-- 20 preguntas base y tres distractores por cada una.
INSERT INTO pregunta (id, descripcion, id_concepto, activo)
SELECT
    'DEMO-PREG-' || LPAD(n::text, 3, '0'),
    CASE n
        WHEN 1 THEN '¿Cuál es el propósito principal de una metodología ágil?'
        WHEN 2 THEN '¿Qué caracteriza a Scrum?'
        WHEN 3 THEN '¿Qué representa el Sprint en Scrum?'
        WHEN 4 THEN '¿Quién prioriza el Product Backlog?'
        WHEN 5 THEN '¿Cuál es el objetivo de una Daily Scrum?'
        WHEN 6 THEN '¿Qué describe una historia de usuario?'
        WHEN 7 THEN '¿Qué contiene típicamente un criterio de aceptación?'
        WHEN 8 THEN '¿Para qué se utiliza un sprint backlog?'
        WHEN 9 THEN '¿Qué mide una retrospectiva de Scrum?'
        WHEN 10 THEN '¿Cuál es una práctica común de mejora continua?'
        WHEN 11 THEN '¿Qué expresa la primera forma normal de una tabla?'
        WHEN 12 THEN '¿Qué busca la normalización de una base de datos?'
        WHEN 13 THEN '¿Qué es una clave primaria?'
        WHEN 14 THEN '¿Para qué sirve una clave foránea?'
        WHEN 15 THEN '¿Qué describe un índice en una base de datos?'
        WHEN 16 THEN '¿Qué información puede mostrar un plan de ejecución?'
        WHEN 17 THEN '¿Qué tipo de operación puede crear un índice?'
        WHEN 18 THEN '¿Qué significa cardinalidad alta en una columna?'
        WHEN 19 THEN '¿Qué problema puede producir la falta de índices?'
        WHEN 20 THEN '¿Cuál es una consecuencia frecuente de una consulta mal optimizada?'
    END,
    CASE
        WHEN n <= 5 THEN 'CON-SW-1'
        WHEN n <= 10 THEN 'CON-SW-2'
        WHEN n <= 15 THEN 'CON-DB-1'
        ELSE 'CON-DB-2'
    END,
    TRUE
FROM generate_series(1, 20) AS n
ON CONFLICT (id) DO NOTHING;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
SELECT
    'DEMO-OPC-' || LPAD(n::text, 3, '0') || '-' || suffix,
    CASE suffix
        WHEN 'A' THEN respuesta_correcta
        WHEN 'B' THEN distractor_1
        WHEN 'C' THEN distractor_2
        WHEN 'D' THEN distractor_3
    END,
    (suffix = 'A'),
    'DEMO-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 20) AS n
CROSS JOIN unnest(ARRAY['A', 'B', 'C', 'D']) AS suffix
CROSS JOIN LATERAL (
    SELECT
        (ARRAY[
            'Entregar valor de forma incremental y adaptable',
            'Aplicar iteraciones cortas con revisión frecuente',
            'Organizar el trabajo en incrementos de tiempo',
            'Representar las necesidades del dueño del producto',
            'Sincronizar avances, impedimentos y próximos pasos',
            'Una necesidad expresada desde la perspectiva del usuario',
            'Una condición verificable que define cuándo se cumple la historia',
            'Planificar el trabajo comprometido para el sprint',
            'Analizar el proceso del equipo y acordar mejoras',
            'Refinar un proceso existente con ciclos de mejora',
            'Eliminar grupos repetidos dentro de una columna',
            'Reducir redundancia y anomalías de modificación',
            'Identificar de forma única cada registro',
            'Relacionar registros con una clave de otra tabla',
            'Acelerar búsquedas sobre columnas indexadas',
            'Mostrar operaciones, costos y accesos usados por el motor',
            'Crear una estructura que mejore ciertas búsquedas',
            'Distinguir muchos valores diferentes en una columna',
            'Generar recorridos completos y mayor costo de lectura',
            'Aumentar el tiempo de respuesta y consumo de recursos'
        ])[n::int] AS respuesta_correcta,
        (ARRAY[
            'Definir únicamente una lista final sin cambios',
            'Realizar una entrega única al terminar el proyecto',
            'Extender las actividades hasta agotar el presupuesto',
            'Asignar todas las tareas exclusivamente al cliente',
            'Realizar una demostración sin participación del equipo',
            'Un diagrama exclusivo del esquema físico',
            'Una instrucción SQL sin contexto de negocio',
            'Enumerar todos los recursos futuros del proyecto',
            'Medir solamente cuántas líneas escribió cada persona',
            'Eliminar retrospectivas y reuniones periódicas',
            'Permitir datos duplicados dentro de cada fila',
            'Fusionar todas las tablas en una sola',
            'Almacenar siempre un texto libre como identificador',
            'Definir cualquier relación sin restricciones',
            'Recorrer siempre todas las filas sin estadísticas',
            'Ocultar por completo los pasos del optimizador',
            'Recrear tablas completas en cada consulta',
            'Repetir siempre el mismo valor en una columna',
            'Reducir automáticamente el volumen de datos',
            'Garantizar que todas las consultas sean instantáneas'
        ])[n::int] AS distractor_1,
        (ARRAY[
            'Aumentar la dependencia de requisitos cerrados',
            'Evitar cualquier tipo de retroalimentación',
            'Separar al equipo por fases rígidas',
            'Transferir toda la responsabilidad al desarrollador',
            'Sustituir el trabajo colaborativo por reportes aislados',
            'Una colección de tablas sin atributos',
            'Un resultado esperado calculado por el servidor',
            'Registrar tareas que nunca serán priorizadas',
            'Comparar únicamente salarios del equipo',
            'Reiniciar el proyecto después de cada actividad',
            'Guardar atributos multivaluados en una sola celda',
            'Aumentar deliberadamente redundancia no controlada',
            'Cambiar el nombre de todas las columnas',
            'Copiar datos entre tablas sin validar',
            'Ordenar físicamente el archivo de resultados',
            'Mostrar solamente el texto de la consulta',
            'Eliminar todos los índices existentes',
            'Forzar que todas las columnas tengan valores únicos',
            'Agregar índices en cada comparación del sistema',
            'Convertir consultas rápidas en consultas sin resultados'
        ])[n::int] AS distractor_2,
        (ARRAY[
            'Postergar la entrega hasta finalizar toda la planeación',
            'Bloquear cambios una vez iniciada una fase',
            'Trabajar sin ciclos ni intervalos definidos',
            'Permitir que únicamente un jefe cree prioridades',
            'Eliminar comunicación dentro del equipo',
            'Un documento técnico sin relación con el usuario',
            'Una captura de pantalla sin especificación',
            'Crear requisitos permanentes sin revisión',
            'Cerrar el proyecto al final de cada reunión',
            'Mantener siempre el mismo proceso sin mediciones',
            'Duplicar llaves primarias en todas las relaciones',
            'Incrementar dependencias entre valores repetidos',
            'Usar cualquier columna como identificador opcional',
            'Eliminar referencias entre entidades',
            'Descartar registros fuera de las consultas',
            'Ejecutar procesos sin observar su costo',
            'Recargar la tabla después de cada búsqueda',
            'Usar una sola fila para todos los valores',
            'Leer una sola fila aun con millones disponibles',
            'Forzar que todas las consultas usen tablas temporales'
        ])[n::int] AS distractor_3
) datos
ON CONFLICT (id) DO NOTHING;

-- Cuatro quizzes pasados: 5, 10, 20 y 10 preguntas.
INSERT INTO quiz (
    id, detalles, fecha_creacion, fecha_inicio, fecha_finalizacion, id_grupo
)
VALUES
    ('DEMO-QZ-05', 'Diagnóstico Ágil - 5 preguntas', NOW() - INTERVAL '61 days', NOW() - INTERVAL '60 days', NOW() - INTERVAL '58 days', 'DEMO-GRP-A'),
    ('DEMO-QZ-10', 'Base de Datos - 10 preguntas', NOW() - INTERVAL '46 days', NOW() - INTERVAL '45 days', NOW() - INTERVAL '43 days', 'DEMO-GRP-B'),
    ('DEMO-QZ-20', 'Integración de Ingeniería - 20 preguntas', NOW() - INTERVAL '31 days', NOW() - INTERVAL '30 days', NOW() - INTERVAL '28 days', 'DEMO-GRP-C'),
    ('DEMO-QZ-10B', 'Repaso Ágil y Datos - 10 preguntas', NOW() - INTERVAL '16 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '13 days', 'DEMO-GRP-A')
ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT
    'DEMO-QXP-05-' || LPAD(n::text, 3, '0'),
    'DEMO-QZ-05',
    'DEMO-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 5) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT
    'DEMO-QXP-10-' || LPAD(n::text, 3, '0'),
    'DEMO-QZ-10',
    'DEMO-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(6, 15) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT
    'DEMO-QXP-20-' || LPAD(n::text, 3, '0'),
    'DEMO-QZ-20',
    'DEMO-PREG-' || LPAD(n::text, 3, '0')
FROM generate_series(1, 20) AS n
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
SELECT
    'DEMO-QXP-10B-' || LPAD(n::text, 3, '0'),
    'DEMO-QZ-10B',
    'DEMO-PREG-' || LPAD(n::text, 3, '0')
FROM (
    SELECT n FROM generate_series(1, 5) AS n
    UNION ALL
    SELECT n FROM generate_series(16, 20) AS n
) datos(n)
ON CONFLICT (id_quiz, id_pregunta) DO NOTHING;

-- Intentos completados. Algunos alumnos se omiten deliberadamente para representar no entregas.
WITH miembros AS (
    SELECT 'DEMO-QZ-05' AS quiz_id, 'DEMO-ALU-' || LPAD(n::text, 3, '0') AS alumno_id, NOW() - INTERVAL '58 days 5 minutes' AS limite
    FROM generate_series(1, 20) AS n
    WHERE n NOT IN (3, 18, 19)
    UNION ALL
    SELECT 'DEMO-QZ-10', 'DEMO-ALU-' || LPAD(n::text, 3, '0'), NOW() - INTERVAL '43 days 5 minutes'
    FROM generate_series(11, 30) AS n
    WHERE n NOT IN (11, 25, 30)
    UNION ALL
    SELECT 'DEMO-QZ-20', 'DEMO-ALU-' || LPAD(n::text, 3, '0'), NOW() - INTERVAL '28 days 5 minutes'
    FROM (
        SELECT n FROM generate_series(1, 10) AS n
        UNION ALL
        SELECT n FROM generate_series(21, 30) AS n
    ) datos(n)
    WHERE n NOT IN (5, 22, 29)
    UNION ALL
    SELECT 'DEMO-QZ-10B', 'DEMO-ALU-' || LPAD(n::text, 3, '0'), NOW() - INTERVAL '13 days 5 minutes'
    FROM generate_series(1, 20) AS n
    WHERE n NOT IN (1, 14, 17)
)
INSERT INTO intentos (
    id, id_quiz, id_alumno, fecha_inicio, fecha_envio
)
SELECT
    CASE quiz_id
        WHEN 'DEMO-QZ-05' THEN 'DINT-05-' || RIGHT(alumno_id, 3)
        WHEN 'DEMO-QZ-10' THEN 'DINT-10-' || RIGHT(alumno_id, 3)
        WHEN 'DEMO-QZ-20' THEN 'DINT-20-' || RIGHT(alumno_id, 3)
        WHEN 'DEMO-QZ-10B' THEN 'DINT-10B-' || RIGHT(alumno_id, 3)
    END,
    quiz_id,
    alumno_id,
    limite - INTERVAL '35 minutes',
    limite
FROM miembros
ON CONFLICT (id_quiz, id_alumno) DO NOTHING;

-- Detalles por pregunta. DEMO-ALU-001 y -002 tienen foco alto (2-3 cambios por pregunta);
-- el resto alterna entre cero y una incidencia.
WITH payload AS (
    SELECT
        i.id AS intento_id,
        i.id_alumno AS alumno_id,
        qp.id_pregunta AS pregunta_id,
        qp.id_quiz AS quiz_id,
        ROW_NUMBER() OVER (PARTITION BY i.id ORDER BY qp.id) AS posicion
    FROM intentos i
    JOIN quiz_x_pregunta qp ON qp.id_quiz = i.id_quiz
    WHERE i.id LIKE 'DINT-%'
)
INSERT INTO detalles_intentos (
    id, id_intento, id_pregunta, cambios_pantalla, id_opcion, es_correcto
)
SELECT
    'DDT-' || intento_id || '-' || LPAD(posicion::text, 2, '0'),
    intento_id,
    pregunta_id,
    CASE
        WHEN alumno_id IN ('DEMO-ALU-001', 'DEMO-ALU-002')
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
    SELECT o.id
    FROM opciones o
    WHERE o.id_pregunta = payload.pregunta_id
      AND o.es_correcta = TRUE
    ORDER BY o.id
    LIMIT 1
) correcta ON TRUE
LEFT JOIN LATERAL (
    SELECT o.id
    FROM opciones o
    WHERE o.id_pregunta = payload.pregunta_id
      AND o.es_correcta = FALSE
    ORDER BY o.id
    LIMIT 1
) incorrecta ON TRUE
ON CONFLICT (id_intento, id_pregunta) DO NOTHING;

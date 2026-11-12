-- ==========================================================
-- DATOS DE PRUEBA (SEED) PARA EL SISTEMA MINERVA
-- ==========================================================

-- 1. CREAR SEGUNDO PLANTEL
INSERT INTO planteles (id, detalles, calle, colonia, municipio, estado, codigo_postal, id_institucion, activo)
VALUES (2, 'Plantel Norte', 'Av. Universidad 123', 'Centro', 'Guadalajara', 'Jalisco', '44100', 1, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 2. CREAR DIRECTOR PARA EL PLANTEL 2
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('DIR-NORTE-01', 'Roberto', 'Gomez', 'Bolaños', 'roberto.gomez@minerva.local', 'DIRECTOR_PLANTEL', 2, TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('director_norte', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'DIR-NORTE-01')
ON CONFLICT (username) DO NOTHING;

-- 3. CREAR DOCENTES (2 Docentes)
-- Docente 1 (Plantel 1)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('DOC-001', 'Ana', 'Martinez', 'Lopez', 'ana.martinez@minerva.local', 'DOCENTE', 1, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('ana.martinez', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'DOC-001')
ON CONFLICT (username) DO NOTHING;

-- Docente 2 (Plantel 2)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('DOC-002', 'Carlos', 'Perez', 'Ruiz', 'carlos.perez@minerva.local', 'DOCENTE', 2, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('carlos.perez', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'DOC-002')
ON CONFLICT (username) DO NOTHING;

-- 4. CREAR ALUMNOS (4 Alumnos)
-- Alumno 1 (Plantel 1)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('ALU-001', 'Luis', 'Hernandez', 'Castro', 'luis.hernandez@minerva.local', 'ALUMNO', 1, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('luis.hernandez', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'ALU-001')
ON CONFLICT (username) DO NOTHING;
-- Insertar en tabla alumnos (extensión de persona)
INSERT INTO alumnos (id, tutor, numero_tutor, fecha_ingreso, promedio_general)
VALUES ('ALU-001', 'Maria Castro', '3312345678', NOW(), 8.5)
ON CONFLICT (id) DO NOTHING;

-- Alumno 2 (Plantel 1)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('ALU-002', 'Sofia', 'Ramirez', 'Diaz', 'sofia.ramirez@minerva.local', 'ALUMNO', 1, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('sofia.ramirez', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'ALU-002')
ON CONFLICT (username) DO NOTHING;
INSERT INTO alumnos (id, tutor, numero_tutor, fecha_ingreso, promedio_general)
VALUES ('ALU-002', 'Jorge Diaz', '3387654321', NOW(), 9.2)
ON CONFLICT (id) DO NOTHING;

-- Alumno 3 (Plantel 2)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('ALU-003', 'Pedro', 'Lopez', 'Garcia', 'pedro.lopez@minerva.local', 'ALUMNO', 2, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('pedro.lopez', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'ALU-003')
ON CONFLICT (username) DO NOTHING;
INSERT INTO alumnos (id, tutor, numero_tutor, fecha_ingreso, promedio_general)
VALUES ('ALU-003', 'Ana Garcia', '3398765432', NOW(), 7.8)
ON CONFLICT (id) DO NOTHING;

-- Alumno 4 (Plantel 2)
INSERT INTO personas (id, nombre, apellido_paterno, apellido_materno, email, rol, id_plantel, activo)
VALUES ('ALU-004', 'Elena', 'Torres', 'Vega', 'elena.torres@minerva.local', 'ALUMNO', 2, TRUE)
ON CONFLICT (id) DO NOTHING;
INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES ('elena.torres', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'ALU-004')
ON CONFLICT (username) DO NOTHING;
INSERT INTO alumnos (id, tutor, numero_tutor, fecha_ingreso, promedio_general)
VALUES ('ALU-004', 'Raul Vega', '3355555555', NOW(), 8.9)
ON CONFLICT (id) DO NOTHING;

-- 5. CREAR PLAN DE ESTUDIO Y MATERIAS
INSERT INTO plan_estudio (detalles) VALUES ('Ingeniería en Sistemas Computacionales')
ON CONFLICT (detalles) DO NOTHING;

INSERT INTO materia (id, detalles, prefijo, id_plan_estudio, activo)
VALUES ('MAT-001', 'Programación Orientada a Objetos', 'PROG', 'Ingeniería en Sistemas Computacionales', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO materia (id, detalles, prefijo, id_plan_estudio, activo)
VALUES ('MAT-002', 'Base de Datos', 'BD', 'Ingeniería en Sistemas Computacionales', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 6. CREAR GRUPOS
-- Grupo A (Plantel 1, Docente 1, Materia PROG)
INSERT INTO grupo (id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo)
VALUES ('GRP-A', 'SIST-1A', 'Sistemas 1A', '1', 'DOC-001', 'MAT-001', 1, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Grupo B (Plantel 2, Docente 2, Materia BD)
INSERT INTO grupo (id, clave_grupo, nombre, semestre, id_docente, id_materia, id_plantel, activo)
VALUES ('GRP-B', 'SIST-2B', 'Sistemas 2B', '2', 'DOC-002', 'MAT-002', 2, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 7. ASIGNAR ALUMNOS A GRUPOS
-- Alumnos al Grupo A
INSERT INTO grupos_alumnos (id_grupo, id_alumno) VALUES ('GRP-A', 'ALU-001') ON CONFLICT DO NOTHING;
INSERT INTO grupos_alumnos (id_grupo, id_alumno) VALUES ('GRP-A', 'ALU-002') ON CONFLICT DO NOTHING;

-- Alumnos al Grupo B
INSERT INTO grupos_alumnos (id_grupo, id_alumno) VALUES ('GRP-B', 'ALU-003') ON CONFLICT DO NOTHING;
INSERT INTO grupos_alumnos (id_grupo, id_alumno) VALUES ('GRP-B', 'ALU-004') ON CONFLICT DO NOTHING;

-- 8. CREAR ESTRUCTURA DE QUIZ (Preguntas y Opciones)
-- Unidad -> Tema -> Concepto -> Pregunta -> Opciones
INSERT INTO unidad (id, detalles, id_materia, activo)
VALUES ('UNI-001', 'Fundamentos de POO', 'MAT-001', TRUE) ON CONFLICT DO NOTHING;

INSERT INTO tema (id, detalles, id_unidad, activo)
VALUES ('TEMA-001', 'Clases y Objetos', 'UNI-001', TRUE) ON CONFLICT DO NOTHING;

INSERT INTO concepto (id, detalles, id_tema, activo)
VALUES ('CONC-001', 'Definición de Clase', 'TEMA-001', TRUE) ON CONFLICT DO NOTHING;

INSERT INTO pregunta (id, descripcion, id_concepto, activo)
VALUES ('PREG-001', '¿Qué es una clase en POO?', 'CONC-001', TRUE) ON CONFLICT DO NOTHING;

INSERT INTO opciones (id, descripcion, es_correcta, id_pregunta)
VALUES 
('OPC-001', 'Una plantilla para crear objetos', TRUE, 'PREG-001'),
('OPC-002', 'Un tipo de variable primitiva', FALSE, 'PREG-001'),
('OPC-003', 'Una función matemática', FALSE, 'PREG-001')
ON CONFLICT (id) DO NOTHING;

-- 9. CREAR QUIZ Y ASIGNAR PREGUNTA
INSERT INTO quiz (id, detalles, fecha_inicio, fecha_finalizacion, id_grupo)
VALUES ('QUIZ-001', 'Examen Diagnóstico POO', NOW(), NOW() + INTERVAL '7 days', 'GRP-A')
ON CONFLICT (id) DO NOTHING;

INSERT INTO quiz_x_pregunta (id, id_quiz, id_pregunta)
VALUES ('QUIZP-001', 'QUIZ-001', 'PREG-001')
ON CONFLICT (id) DO NOTHING;

-- 10. SIMULAR UN INTENTO DE EXAMEN (Alumno 1)
INSERT INTO intentos (id, id_quiz, id_alumno, fecha_inicio, fecha_envio)
VALUES ('INT-001', 'QUIZ-001', 'ALU-001', NOW(), NOW() + INTERVAL '10 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO detalles_intentos (id, id_intento, id_pregunta, id_opcion, es_correcto)
VALUES ('DET-001', 'INT-001', 'PREG-001', 'OPC-001', TRUE)
ON CONFLICT (id) DO NOTHING;

-- 11. ACTUALIZAR PROMEDIOS (Opcional, pero realista)
-- El promedio se actualizaría por lógica de negocio, pero aquí lo dejamos fijo.
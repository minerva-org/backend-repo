INSERT INTO persona (id, nombre, apellido, email, rol, id_plantel)
VALUES
    ('DIRECTOR-PLANTEL-DEFAULT', 'Director', 'Plantel', 'director.plantel@minerva.local', 'DIRECTOR_PLANTEL', 1),
    ('DOCENTE-DEFAULT', 'Docente', 'Default', 'docente@minerva.local', 'DOCENTE', 1),
    ('COORDINADOR-DEFAULT', 'Coordinador', 'Default', 'coordinador@minerva.local', 'COORDINADOR', 1),
    ('ALUMNO-DEFAULT', 'Alumno', 'Default', 'alumno@minerva.local', 'ALUMNO', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (username, password, reestablecimiento, id_persona)
VALUES
    ('director_plantel', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'DIRECTOR-PLANTEL-DEFAULT'),
    ('docente', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'DOCENTE-DEFAULT'),
    ('coordinador', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'COORDINADOR-DEFAULT'),
    ('alumno', '$2a$10$uQLQWy0yPw1QkIbK4U4c7e07jZeThR6OzjkRwwPbJuxWZQq5RivXa', FALSE, 'ALUMNO-DEFAULT')
ON CONFLICT (username) DO NOTHING;

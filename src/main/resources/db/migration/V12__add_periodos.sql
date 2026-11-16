CREATE TABLE periodos (
    id VARCHAR(255) PRIMARY KEY,
    descripcion VARCHAR(20) NOT NULL,
    curso VARCHAR(5) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO periodos (id, descripcion, curso)
VALUES ('PRIMERO', 'AGO-DIC', 2026);

INSERT INTO configuraciones (id, valor, descripcion)
    VALUES ('PERIODO', 'PRIMERO', 'AGO-DIC 2026')

-- CREATE TABLE configuraciones (
--     id VARCHAR(255) PRIMARY KEY,
--     valor VARCHAR(255) NOT NULL,
--     descripcion VARCHAR(255) NOT NULL,
--     fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
-- );
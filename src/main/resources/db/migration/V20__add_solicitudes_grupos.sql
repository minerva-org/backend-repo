CREATE TABLE IF NOT EXISTS solicitudes_grupos(
    id          VARCHAR(40)  NOT NULL,
    id_grupo    VARCHAR(255) NOT NULL,
    id_alumno   VARCHAR(255) NOT NULL,

    CONSTRAINT pk_solicitudes_grupos PRIMARY KEY (id),
    CONSTRAINT fk_alumnos_persona FOREIGN KEY (id_alumno) REFERENCES personas (id),
    CONSTRAINT fk_grupo FOREIGN KEY (id_grupo) REFERENCES grupo (id),
    CONSTRAINT uk_inscripcion_alumno_curso UNIQUE (id_grupo, id_alumno));
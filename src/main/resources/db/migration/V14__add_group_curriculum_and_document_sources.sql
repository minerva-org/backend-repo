ALTER TABLE materia
    ADD COLUMN IF NOT EXISTS id_documento_materia VARCHAR(128);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_materia_documento_materia'
          AND conrelid = 'materia'::regclass
    ) THEN
        ALTER TABLE materia
            ADD CONSTRAINT fk_materia_documento_materia
            FOREIGN KEY (id_documento_materia)
            REFERENCES documentos (id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_materia_documento_materia
    ON materia (id_documento_materia)
    WHERE id_documento_materia IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_materia_documento_materia
    ON materia (id_documento_materia);

CREATE TABLE IF NOT EXISTS unidad_grupo (
    id VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    id_materia VARCHAR(255),
    id_grupo VARCHAR(255) NOT NULL,
    id_unidad_origen VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_unidad_grupo PRIMARY KEY (id),
    CONSTRAINT fk_unidad_grupo_materia
        FOREIGN KEY (id_materia) REFERENCES materia (id) ON DELETE SET NULL,
    CONSTRAINT fk_unidad_grupo_grupo
        FOREIGN KEY (id_grupo) REFERENCES grupo (id) ON DELETE CASCADE,
    CONSTRAINT fk_unidad_grupo_origen
        FOREIGN KEY (id_unidad_origen) REFERENCES unidad (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_unidad_grupo_grupo
    ON unidad_grupo (id_grupo);
CREATE INDEX IF NOT EXISTS idx_unidad_grupo_materia
    ON unidad_grupo (id_materia);
CREATE INDEX IF NOT EXISTS idx_unidad_grupo_origen
    ON unidad_grupo (id_unidad_origen);

CREATE TABLE IF NOT EXISTS tema_grupo (
    id VARCHAR(255) NOT NULL,
    clave VARCHAR(255),
    nombre VARCHAR(255) NOT NULL,
    id_unidad_grupo VARCHAR(255) NOT NULL,
    id_tema_origen VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_tema_grupo PRIMARY KEY (id),
    CONSTRAINT fk_tema_grupo_unidad
        FOREIGN KEY (id_unidad_grupo) REFERENCES unidad_grupo (id) ON DELETE CASCADE,
    CONSTRAINT fk_tema_grupo_origen
        FOREIGN KEY (id_tema_origen) REFERENCES tema (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_tema_grupo_unidad
    ON tema_grupo (id_unidad_grupo);
CREATE INDEX IF NOT EXISTS idx_tema_grupo_origen
    ON tema_grupo (id_tema_origen);

CREATE TABLE IF NOT EXISTS concepto_grupo (
    id VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    id_tema_grupo VARCHAR(255) NOT NULL,
    id_concepto_origen VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_concepto_grupo PRIMARY KEY (id),
    CONSTRAINT fk_concepto_grupo_tema
        FOREIGN KEY (id_tema_grupo) REFERENCES tema_grupo (id) ON DELETE CASCADE,
    CONSTRAINT fk_concepto_grupo_origen
        FOREIGN KEY (id_concepto_origen) REFERENCES concepto (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_concepto_grupo_tema
    ON concepto_grupo (id_tema_grupo);
CREATE INDEX IF NOT EXISTS idx_concepto_grupo_origen
    ON concepto_grupo (id_concepto_origen);

CREATE TABLE IF NOT EXISTS contenido_grupo (
    id VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    id_concepto_grupo VARCHAR(255) NOT NULL,

    CONSTRAINT pk_contenido_grupo PRIMARY KEY (id),
    CONSTRAINT fk_contenido_grupo_concepto
        FOREIGN KEY (id_concepto_grupo) REFERENCES concepto_grupo (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contenido_grupo_concepto
    ON contenido_grupo (id_concepto_grupo);

CREATE TABLE IF NOT EXISTS fuentes_grupo (
    id_documento VARCHAR(128) NOT NULL,
    id_grupo VARCHAR(255) NOT NULL,

    CONSTRAINT pk_fuentes_grupo PRIMARY KEY (id_documento, id_grupo),
    CONSTRAINT fk_fuentes_grupo_documento
        FOREIGN KEY (id_documento) REFERENCES documentos (id) ON DELETE CASCADE,
    CONSTRAINT fk_fuentes_grupo_grupo
        FOREIGN KEY (id_grupo) REFERENCES grupo (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fuentes_grupo_grupo
    ON fuentes_grupo (id_grupo);
CREATE INDEX IF NOT EXISTS idx_fuentes_grupo_documento
    ON fuentes_grupo (id_documento);

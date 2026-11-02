ALTER TABLE planteles DROP CONSTRAINT IF EXISTS fk_plantel_universidad;
ALTER TABLE planteles RENAME COLUMN id_universidad TO id_institucion;
ALTER TABLE universidad RENAME TO institucion;

ALTER TABLE planteles
    ADD CONSTRAINT fk_plantel_institucion
    FOREIGN KEY (id_institucion) REFERENCES institucion (id);

UPDATE institucion
SET detalles = 'Institucion Chapala Gutierrez'
WHERE id = 1;

INSERT INTO institucion (id, detalles, activo)
SELECT 1, 'Institucion Chapala Gutierrez', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM institucion WHERE id = 1
);

ALTER TABLE personas
ALTER COLUMN apellido_materno DROP NOT NULL;

UPDATE personas SET apellido_paterno = 'DEFAULT' WHERE apellido_paterno IS NULL;

ALTER TABLE personas
ALTER COLUMN apellido_paterno SET NOT NULL;
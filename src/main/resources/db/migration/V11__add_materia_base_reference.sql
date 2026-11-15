ALTER TABLE materia
    ADD COLUMN IF NOT EXISTS id_materia_base VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_materia_base'
          AND conrelid = 'materia'::regclass
    ) THEN
        ALTER TABLE materia
            ADD CONSTRAINT fk_materia_base
                FOREIGN KEY (id_materia_base) REFERENCES materia (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_materia_plantel_base
    ON materia (id_plantel, id_materia_base);

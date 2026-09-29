ALTER TABLE grupo ADD COLUMN IF NOT EXISTS id_plantel BIGINT;

UPDATE grupo g
SET id_plantel = (
    SELECT p.id
    FROM plantel p
    ORDER BY p.id
    LIMIT 1
)
WHERE g.id_plantel IS NULL
  AND EXISTS (SELECT 1 FROM plantel);

ALTER TABLE grupo
    ALTER COLUMN id_plantel SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_grupo_plantel'
    ) THEN
        ALTER TABLE grupo
            ADD CONSTRAINT fk_grupo_plantel
            FOREIGN KEY (id_plantel) REFERENCES plantel (id);
    END IF;
END $$;

ALTER TABLE organisasi
    ADD COLUMN IF NOT EXISTS uuid UUID;

UPDATE organisasi
SET uuid = gen_random_uuid()
WHERE uuid IS NULL;

ALTER TABLE organisasi
    ALTER COLUMN uuid SET NOT NULL;

ALTER TABLE organisasi
    ADD CONSTRAINT uq_organisasi_uuid UNIQUE (uuid);

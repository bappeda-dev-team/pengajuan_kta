ALTER TABLE form_pengajuan
    ADD COLUMN IF NOT EXISTS nik VARCHAR(255);

-- 2) backfill user_nik based on existing user_id
UPDATE form_pengajuan fp
SET nik = a.nik
FROM account a
WHERE fp.user_id = a.id
  AND fp.nik IS NULL;

-- 3) make it NOT NULL after backfill
ALTER TABLE form_pengajuan
    ALTER COLUMN nik SET NOT NULL;

-- 4) drop old FK (name must match your DB)
ALTER TABLE form_pengajuan
    DROP CONSTRAINT IF EXISTS fk_form_pengajuan_user_id;

-- 5) add new FK to account(nik)
ALTER TABLE form_pengajuan
    ADD CONSTRAINT fk_form_pengajuan_nik
        FOREIGN KEY (nik)
            REFERENCES account (nik)
            ON DELETE CASCADE;

-- 6) (optional but recommended) drop old column user_id
ALTER TABLE form_pengajuan
    DROP COLUMN IF EXISTS user_id;
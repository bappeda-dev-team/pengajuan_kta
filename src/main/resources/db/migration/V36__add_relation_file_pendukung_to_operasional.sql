-- 1. Add the column to store the UUID of the izin_operasional
ALTER TABLE file_pendukung
    ADD COLUMN operasional_uuid UUID;

-- 2. Add the Foreign Key constraint
ALTER TABLE file_pendukung
    ADD CONSTRAINT fk_file_pendukung_izin
        FOREIGN KEY (operasional_uuid)
            REFERENCES izin_operasional (uuid)
            ON DELETE CASCADE; -- If izin_operasional is deleted, delete the files too
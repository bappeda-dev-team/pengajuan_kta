-- 1. Add the column to store the UUID of the parent form
ALTER TABLE izin_operasional
    ADD COLUMN form_uuid UUID;

-- 2. Add the Foreign Key constraint
ALTER TABLE izin_operasional
    ADD CONSTRAINT fk_izin_operasional_form
        FOREIGN KEY (form_uuid)
            REFERENCES form_pengajuan (uuid)
            ON DELETE CASCADE; -- Optional: Delete izin_operasional if the parent form is deleted
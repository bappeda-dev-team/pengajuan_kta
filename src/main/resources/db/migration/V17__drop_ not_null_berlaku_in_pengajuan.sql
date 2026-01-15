ALTER TABLE form_pengajuan
    ALTER COLUMN berlaku_dari DROP NOT NULL,
    ALTER COLUMN berlaku_sampai DROP NOT NULL;

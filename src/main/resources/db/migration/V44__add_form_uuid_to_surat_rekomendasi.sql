ALTER TABLE surat_rekomendasi
    ADD COLUMN form_uuid UUID;

ALTER TABLE surat_rekomendasi
    ADD CONSTRAINT fk_surat_rekomendasi_form_pengajuan
        FOREIGN KEY (form_uuid)
            REFERENCES form_pengajuan (uuid)
            ON DELETE CASCADE;

-- Tambahkan kolom untuk foreign key
ALTER TABLE file_pendukung
    ADD COLUMN rekom_uuid UUID;

-- Tambahkan foreign key constraint
ALTER TABLE file_pendukung
    ADD CONSTRAINT fk_file_surat_rekomendasi
        FOREIGN KEY (rekom_uuid)
            REFERENCES surat_rekomendasi (uuid)
            ON DELETE CASCADE;

-- Tambahkan index untuk performa query
CREATE INDEX idx_file_pendukung_surat_uuid
    ON file_pendukung(rekom_uuid);
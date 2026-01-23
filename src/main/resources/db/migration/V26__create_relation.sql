-- 1. Relation: Organisasi -> Form Pengajuan
-- Ubah tipe data menjadi UUID (bukan VARCHAR)
ALTER TABLE form_pengajuan
    ADD COLUMN organisasi_uuid UUID;

ALTER TABLE form_pengajuan
    ADD CONSTRAINT fk_form_organisasi
        FOREIGN KEY (organisasi_uuid) REFERENCES organisasi(uuid);

-- 2. Relation: Organisasi -> Surat Rekomendasi
-- Ubah tipe data menjadi UUID
ALTER TABLE surat_rekomendasi
    ADD COLUMN organisasi_uuid UUID;

ALTER TABLE surat_rekomendasi
    ADD CONSTRAINT fk_surat_organisasi
        FOREIGN KEY (organisasi_uuid) REFERENCES organisasi(uuid);

-- 3. Relation: Organisasi -> File Pendukung
-- Ubah tipe data menjadi UUID
ALTER TABLE file_pendukung
    ADD COLUMN organisasi_uuid UUID;

ALTER TABLE file_pendukung
    ADD CONSTRAINT fk_file_organisasi
        FOREIGN KEY (organisasi_uuid) REFERENCES organisasi(uuid);

-- 4. Relation: Account -> Organisasi
-- Pastikan tabel organisasi sudah ada sebelumnya.
-- NIK bertipe VARCHAR (sesuai Account), jadi ini sudah benar VARCHAR.
ALTER TABLE organisasi
    ADD COLUMN nik VARCHAR(16); -- Disamakan panjangnya dengan account.nik (16) agar lebih presisi

ALTER TABLE organisasi
    ADD CONSTRAINT fk_organisasi_account
        FOREIGN KEY (nik) REFERENCES account(nik);
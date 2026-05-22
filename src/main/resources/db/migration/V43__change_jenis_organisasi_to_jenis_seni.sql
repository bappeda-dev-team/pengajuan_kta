-- 1. Merubah nama tabel jenis_organisasi menjadi jenis_seni
ALTER TABLE jenis_organisasi RENAME TO jenis_seni;

-- 2. Merubah nama kolom pada tabel jenis_seni
ALTER TABLE jenis_seni RENAME COLUMN kode_jenis_organisasi TO kode_jenis_seni;
ALTER TABLE jenis_seni RENAME COLUMN nama_jenis_organisasi TO jenis_seni;

-- 3. Merubah relasi agar form_pengajuan menggunakan 'id' dari tabel jenis_seni

-- a. Hapus constraint foreign key lama di tabel form_pengajuan
ALTER TABLE form_pengajuan DROP CONSTRAINT fk_form_pengajuan_kode_jenis_organisasi;

-- b. Hapus kolom lama yang menggunakan VARCHAR
ALTER TABLE form_pengajuan DROP COLUMN kode_jenis_organisasi;

-- c. Tambahkan kolom baru menggunakan tipe data yang sama dengan PK di jenis_seni (BIGINT)
ALTER TABLE form_pengajuan ADD COLUMN jenis_seni_id BIGINT;

-- d. Buat constraint foreign key baru yang mengarah ke kolom 'id' di tabel jenis_seni
ALTER TABLE form_pengajuan
    ADD CONSTRAINT fk_form_pengajuan_jenis_seni_id
        FOREIGN KEY (jenis_seni_id)
            REFERENCES jenis_seni(id);
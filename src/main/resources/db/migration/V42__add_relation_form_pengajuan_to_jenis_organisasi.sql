ALTER TABLE form_pengajuan
ADD COLUMN kode_jenis_organisasi VARCHAR(255);

ALTER TABLE form_pengajuan
ADD CONSTRAINT fk_form_pengajuan_kode_jenis_organisasi
FOREIGN KEY (kode_jenis_organisasi)
REFERENCES jenis_organisasi(kode_jenis_organisasi);

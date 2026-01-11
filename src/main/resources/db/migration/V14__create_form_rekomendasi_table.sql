CREATE TABLE IF NOT EXISTS surat_rekomendasi
(
    id            BIGSERIAL PRIMARY KEY,
    nik VARCHAR(255) NOT NULL,
    uuid UUID NOT NULL UNIQUE,
    nomor_surat VARCHAR(255)  NOT NULL,
    nomor_induk VARCHAR(255)  NOT NULL,
    tujuan VARCHAR(255)  NOT NULL,
    tanggal DATE NOT NULL,
    tempat VARCHAR(255)  NOT NULL,
    tanggal_berlaku DATE NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    keterangan VARCHAR(255),
    catatan        VARCHAR(20),
    tertanda          JSONB,
    tanggal_surat DATE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_surat_rekomendasi_nik
        FOREIGN KEY (nik)
            REFERENCES account (nik)
            ON DELETE CASCADE
);
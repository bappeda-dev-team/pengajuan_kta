CREATE TABLE IF NOT EXISTS izin_operasional
(
    id                BIGSERIAL PRIMARY KEY,
    nik               VARCHAR(255) NOT NULL,
    uuid              UUID NOT NULL UNIQUE,
    tanggal_pendirian DATE,
    berlaku_dari      DATE,
    berlaku_sampai    DATE,
    status            VARCHAR(20)  NOT NULL,
    keterangan        VARCHAR(255),
    catatan           VARCHAR(20),
    tertanda          JSONB,
    status_tanggal    DATE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_izin_operasional_nik
        FOREIGN KEY (nik)
            REFERENCES account (nik)
            ON DELETE CASCADE
);
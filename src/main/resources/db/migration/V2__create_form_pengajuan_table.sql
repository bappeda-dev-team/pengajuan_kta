CREATE TABLE IF NOT EXISTS form_pengajuan
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    uuid              UUID         NOT NULL UNIQUE,
    induk_organisasi  VARCHAR(255) NOT NULL,
    nomor_induk       VARCHAR(100) NOT NULL,
    jumlah_anggota    INT          NOT NULL,
    daerah            VARCHAR(50)  NOT NULL,
    berlaku_dari      DATE,
    berlaku_sampai    DATE,
    nama              VARCHAR(100) NOT NULL,
    tempat_lahir      VARCHAR(100) NOT NULL,
    tanggal_lahir     DATE         NOT NULL,
    jenis_kelamin     VARCHAR(100) NOT NULL,
    alamat            TEXT,
    profesi           VARCHAR(255) NOT NULL,
    dibuat_di         VARCHAR(50)  NOT NULL,
    dokumen_pendukung JSONB,
    tertanda          JSONB,
    status            VARCHAR(20)  NOT NULL,
    keterangan        VARCHAR(255) NOT NULL,
    catatan           VARCHAR(255),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_form_pengajuan_user_id
        FOREIGN KEY (user_id)
            REFERENCES account (id)
            ON DELETE CASCADE
);
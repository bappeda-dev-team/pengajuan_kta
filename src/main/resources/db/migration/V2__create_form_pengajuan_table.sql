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

-- https://api-ekak.zeabur.app/pohon_kinerja_opd/create
-- {
--     "nama_pohon": "tes tactical baru",
--     "Keterangan": "tidak ada",
--     "jenis_pohon": "Tactical",
--     "level_pohon": 5,
--     "parent": 9346,
--     "tahun": "2025",
--     "kode_opd": "5.01.5.05.0.00.01.0000",
--     "indikator": [
--         {
--             "indikator": "andaikan bumi berputar",
--             "target": [
--                 {
--                     "target": "100",
--                     "satuan": "persen"
--                 }
--             ]
--         },
--         {
--             "indikator": "cuma",
--             "target": [
--                 {
--                     "target": "90",
--                     "satuan": "dokumen"
--                 }
--             ]
--         }
--     ],
--     "tagging": [
--         {
--             "nama_tagging": "Program Unggulan Bupati",
--             "keterangan_tagging_program": [
--                 {
--                     "kode_program_unggulan": "PRG-UNG-73198f",
--                     "tahun": "2025"
--                 }
--             ]
--         }
--     ]
-- }
--
--
-- {
--     "nama_pohon": "Tes sub sub pemda",
--     "Keterangan": "-",
--     "jenis_pohon": "Sub Sub Tematik",
--     "level_pohon": 2,
--     "parent": 2487,
--     "tahun": "2025",
--     "kode_opd": null,
--     "status": "",
--     "indikator": [
--         {
--             "indikator": "indikator contoh",
--             "target": [
--                 {
--                     "target": "100",
--                     "satuan": "%"
--                 }
--             ]
--         },
--         {
--             "indikator": "contoh indikator 2",
--             "target": [
--                 {
--                     "target": "90",
--                     "satuan": "docs"
--                 }
--             ]
--         }
--     ],
--     "tagging": []
-- }
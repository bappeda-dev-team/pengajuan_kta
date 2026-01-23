CREATE TABLE IF NOT EXISTS organisasi (
                                          id BIGSERIAL PRIMARY KEY,
                                          nama_ketua VARCHAR(255) NOT NULL,
                                          bidang_keahlian VARCHAR(255),
                                          nomor_telepon VARCHAR(50),
                                          alamat TEXT
);
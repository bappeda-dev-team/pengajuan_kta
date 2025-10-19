CREATE TABLE IF NOT EXISTS account (
                                              id BIGSERIAL PRIMARY KEY ,
                                              username VARCHAR(255) NOT NULL UNIQUE,
                                              password VARCHAR(255) NOT NULL,
                                              nomor_telepon VARCHAR(16) NOT NULL,
                                              tipe_akun VARCHAR(50) NOT NULL,
                                              role VARCHAR(30) NOT NULL,
                                              status VARCHAR(20) NOT NULL,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
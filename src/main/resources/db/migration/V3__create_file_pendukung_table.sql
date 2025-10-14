CREATE TABLE IF NOT EXISTS file_pendukung (
                                              id BIGSERIAL PRIMARY KEY,
                                              file_url TEXT,
                                              nama_file VARCHAR(255),
                                              form_uuid UUID,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              CONSTRAINT fk_form_pendukung
                                                  FOREIGN KEY (form_uuid)
                                                  REFERENCES form_pengajuan (uuid)
                                                      ON DELETE CASCADE
);
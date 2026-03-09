ALTER TABLE file_pendukung 
  ALTER COLUMN form_uuid DROP NOT NULL,
  ALTER COLUMN rekom_uuid DROP NOT NULL,
  ALTER COLUMN operasional_uuid DROP NOT NULL;
CREATE TABLE documentos (
    id                VARCHAR(128)  NOT NULL,
    object_keys       VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(150) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    checksum_sha256   CHAR(64)     NOT NULL,
    uploaded_by       VARCHAR(255) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_documentos PRIMARY KEY (id),
    CONSTRAINT uq_documentos_object_keys UNIQUE (object_keys),
    CONSTRAINT fk_documentos_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES personas (id)
);

CREATE INDEX idx_documentos_uploaded_by ON documentos (uploaded_by);
CREATE INDEX idx_documentos_checksum    ON documentos (checksum_sha256);
-- id, object_keys, original_filename, content_type, size_bytes, checksum_sha256, uploaded_by, created_ats
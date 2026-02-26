-- =============================================
-- Phase 6: File Metadata — Report export tracking
-- =============================================

CREATE TABLE file_metadata (
    id            UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    entity_type   VARCHAR(50) NOT NULL,
    entity_id     UUID,
    file_name     VARCHAR(255) NOT NULL,
    file_url      VARCHAR(500) NOT NULL,
    file_size     BIGINT NOT NULL DEFAULT 0,
    mime_type     VARCHAR(100) NOT NULL DEFAULT 'text/csv',
    storage_type  VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    uploaded_by   UUID REFERENCES users(id),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_metadata_entity ON file_metadata (entity_type, entity_id);
CREATE INDEX idx_file_metadata_uploaded_by ON file_metadata (uploaded_by);

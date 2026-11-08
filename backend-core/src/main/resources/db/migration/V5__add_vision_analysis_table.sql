-- ============================================================================
-- V5__add_vision_analysis_table.sql
-- Adds AI Vision & Receipt OCR (M14) multi-tenant table and AI tool
-- ============================================================================

CREATE TABLE vision_analysis_records (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_name          VARCHAR(255) NOT NULL,
    analysis_mode       VARCHAR(64) NOT NULL, -- RECEIPT_OCR, DOCUMENT_OCR, PRODUCT_ATTRIBUTE, GENERAL_DESCRIPTION
    extracted_text      TEXT,
    extracted_data_json TEXT,                 -- Stored as JSON string (ANSI SQL compatible for H2 & Postgres)
    total_amount        NUMERIC(15, 2),
    merchant_name       VARCHAR(160),
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vision_records_user_created ON vision_analysis_records(user_id, created_at DESC);
CREATE INDEX idx_vision_records_user_mode ON vision_analysis_records(user_id, analysis_mode);

INSERT INTO ai_tools (id, code, name, category, permission_level, input_schema_json, timeout_seconds, rate_limit_per_hour, is_enabled_globally, version)
VALUES
  ('22222222-2222-2222-2222-222222222217', 'vision_image_analyze', 'Phân tích hình ảnh, quét hóa đơn & OCR văn bản', 'VISION', 2, '{"type":"object","required":["image_data","mode"]}', 20, 30, TRUE, '1.0.0');


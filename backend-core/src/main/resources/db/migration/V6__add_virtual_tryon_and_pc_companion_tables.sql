-- ============================================================================
-- V6__add_virtual_tryon_and_pc_companion_tables.sql
-- Adds Virtual Try-On (M15) & PC Companion (M16) tables and AI tools
-- ============================================================================

CREATE TABLE virtual_tryon_sessions (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    garment_title       VARCHAR(255) NOT NULL,
    garment_category    VARCHAR(64) NOT NULL, -- TOP, BOTTOM, DRESS, FULL_OUTFIT
    garment_image_url   VARCHAR(1024),
    result_image_url    VARCHAR(1024),
    status              VARCHAR(32) NOT NULL DEFAULT 'COMPLETED', -- PROCESSING, COMPLETED, FAILED, EXPIRED
    expires_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tryon_user_created ON virtual_tryon_sessions(user_id, created_at DESC);

CREATE TABLE pc_companion_devices (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_name         VARCHAR(120) NOT NULL,
    os_type             VARCHAR(32) NOT NULL, -- WINDOWS, MACOS, LINUX
    pairing_token_hash  VARCHAR(64) NOT NULL,
    sandbox_folder_path VARCHAR(512) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'CONNECTED', -- CONNECTED, PAUSED, REVOKED
    last_heartbeat_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pc_device_user ON pc_companion_devices(user_id, status);

INSERT INTO ai_tools (id, code, name, category, permission_level, input_schema_json, timeout_seconds, rate_limit_per_hour, is_enabled_globally, version)
VALUES
  ('22222222-2222-2222-2222-222222222218', 'virtual_tryon_synthesize', 'Thử đồ ảo AI & phối trang phục trực quan', 'VISION', 2, '{"type":"object","required":["garment_category"]}', 25, 20, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222219', 'pc_companion_sync', 'Đồng bộ thư mục an toàn máy tính cá nhân', 'INTEGRATION', 3, '{"type":"object","required":["folder_path"]}', 15, 30, TRUE, '1.0.0');


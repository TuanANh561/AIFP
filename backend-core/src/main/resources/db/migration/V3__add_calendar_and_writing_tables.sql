-- ============================================================================
-- V3__add_calendar_and_writing_tables.sql
-- Adds Calendar Assistant (M11) and AI Writing Studio (M12) multi-tenant tables
-- ============================================================================

CREATE TABLE calendar_events (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    start_time          TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time            TIMESTAMP WITH TIME ZONE NOT NULL,
    location            VARCHAR(255),
    description         TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    approval_request_id UUID,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_calendar_events_user_start ON calendar_events(user_id, start_time ASC);
CREATE INDEX idx_calendar_events_approval ON calendar_events(approval_request_id);

CREATE TABLE writing_drafts (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    doc_type            VARCHAR(40) NOT NULL DEFAULT 'EMAIL',
    tone                VARCHAR(40) NOT NULL DEFAULT 'CONCISE',
    prompt_brief        TEXT NOT NULL,
    content_markdown    TEXT NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_writing_drafts_user_created ON writing_drafts(user_id, created_at DESC);

INSERT INTO ai_tools (id, code, name, category, permission_level, input_schema_json, timeout_seconds, rate_limit_per_hour, is_enabled_globally, version)
VALUES
  ('22222222-2222-2222-2222-222222222213', 'calendar_event_read', 'Đọc lịch làm việc cá nhân', 'CALENDAR', 1, '{"type":"object","required":[]}', 10, 120, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222214', 'writing_draft_generate', 'Soạn thảo & biên tập nội dung chuẩn hóa', 'WRITING', 2, '{"type":"object","required":["prompt_brief"]}', 20, 60, TRUE, '1.0.0');

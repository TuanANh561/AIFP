-- ============================================================================
-- V8__add_open_source_and_article_cleanup.sql
-- Adds user-isolated open source model/tool library and article pin status
-- ============================================================================

CREATE TABLE user_open_source_items (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    stars           VARCHAR(64),
    url             VARCHAR(1024) NOT NULL,
    tag             VARCHAR(128),
    summary         TEXT NOT NULL,
    query_keyword   VARCHAR(255),
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_open_source_url UNIQUE (user_id, url)
);

CREATE INDEX idx_user_open_source_user ON user_open_source_items(user_id, created_at DESC);
CREATE INDEX idx_user_open_source_pinned ON user_open_source_items(user_id, is_pinned);

ALTER TABLE live_articles ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN NOT NULL DEFAULT FALSE;

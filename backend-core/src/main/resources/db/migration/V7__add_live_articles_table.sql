-- ============================================================================
-- V7__add_live_articles_table.sql
-- Adds persistent table for verified real-time articles and news crawls
-- ============================================================================

CREATE TABLE live_articles (
    id              UUID PRIMARY KEY,
    channel_id      VARCHAR(64) NOT NULL, -- shopping, jobs, tech, finance, social, news
    source          VARCHAR(128) NOT NULL,
    source_type     VARCHAR(32) NOT NULL, -- vn, tech, shop, fb
    category        VARCHAR(128) NOT NULL,
    title           TEXT NOT NULL,
    summary         TEXT,
    image_url       VARCHAR(1024),
    url             VARCHAR(1024) NOT NULL UNIQUE,
    published_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    fetched_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_live_articles_published ON live_articles(published_at DESC);
CREATE INDEX idx_live_articles_channel ON live_articles(channel_id, published_at DESC);


-- ============================================================================
-- V4__add_shopping_and_vision_tables.sql
-- Adds AI Shopping & Price Tracker (M13) multi-tenant table and AI tools
-- ============================================================================

CREATE TABLE shopping_tracked_items (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    target_price        NUMERIC(15, 2),
    current_price       NUMERIC(15, 2),
    currency            VARCHAR(10) NOT NULL DEFAULT 'VND',
    store_url           VARCHAR(1024) NOT NULL,
    store_name          VARCHAR(120),
    status              VARCHAR(32) NOT NULL DEFAULT 'TRACKING', -- TRACKING, PRICE_DROPPED, PAUSED, PURCHASED
    last_checked_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shopping_items_user_status ON shopping_tracked_items(user_id, status);
CREATE INDEX idx_shopping_items_user_created ON shopping_tracked_items(user_id, created_at DESC);

INSERT INTO ai_tools (id, code, name, category, permission_level, input_schema_json, timeout_seconds, rate_limit_per_hour, is_enabled_globally, version)
VALUES
  ('22222222-2222-2222-2222-222222222215', 'product_price_compare', 'Tìm kiếm & so sánh giá sản phẩm đa sàn', 'SHOPPING', 1, '{"type":"object","required":["query"]}', 15, 60, TRUE, '1.0.0'),
  ('22222222-2222-2222-2222-222222222216', 'shopping_price_track', 'Theo dõi biến động giá & cảnh báo giảm giá', 'SHOPPING', 2, '{"type":"object","required":["title","target_price","store_url"]}', 10, 60, TRUE, '1.0.0');


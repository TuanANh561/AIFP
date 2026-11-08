-- ============================================================================
-- V1__init_core_schema.sql
-- Multi-Tenant Schema for AI Life Assistant Platform (AIFP)
-- Every personal table enforces user_id UUID NOT NULL ownership.
-- ============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    token_version INTEGER NOT NULL DEFAULT 1,
    daily_token_quota INTEGER NOT NULL DEFAULT 150000,
    daily_tokens_used INTEGER NOT NULL DEFAULT 0,
    quota_reset_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_refresh_tokens_user_family ON refresh_tokens(user_id, family_id);

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(120) NOT NULL,
    persona_type VARCHAR(64) NOT NULL DEFAULT 'GENERAL',
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    locale VARCHAR(16) NOT NULL DEFAULT 'vi-VN',
    notification_email VARCHAR(255),
    preferred_channels TEXT NOT NULL DEFAULT '["IN_APP","WEB_PUSH"]',
    memory_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    agent_id UUID,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    last_message_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_conversations_user_last_msg ON ai_conversations(user_id, last_message_at DESC);

CREATE TABLE ai_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(24) NOT NULL,
    content TEXT NOT NULL,
    model_used VARCHAR(64),
    prompt_tokens INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    citations_json TEXT NOT NULL DEFAULT '[]',
    tool_calls_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_messages_conv_created ON ai_messages(conversation_id, created_at ASC);
CREATE INDEX idx_messages_user_id ON ai_messages(user_id);

CREATE TABLE ai_memories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(48) NOT NULL,
    topic_key VARCHAR(120),
    content TEXT NOT NULL,
    embedding_json TEXT,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    confidence NUMERIC(3,2) NOT NULL DEFAULT 1.00,
    source_message_id UUID REFERENCES ai_messages(id) ON DELETE SET NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_memories_user_status ON ai_memories(user_id, status);

CREATE TABLE ai_tasks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    natural_language_prompt TEXT NOT NULL,
    task_type VARCHAR(48) NOT NULL,
    structured_config_json TEXT NOT NULL,
    allowed_tools_json TEXT NOT NULL DEFAULT '[]',
    delivery_mode VARCHAR(24) NOT NULL DEFAULT 'DELTA_ONLY',
    delivery_channels_json TEXT NOT NULL DEFAULT '["IN_APP","EMAIL","WEB_PUSH"]',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tasks_user_status ON ai_tasks(user_id, status);

CREATE TABLE task_schedules (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES ai_tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cron_expression VARCHAR(64) NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    misfire_policy VARCHAR(32) NOT NULL DEFAULT 'FIRE_ONCE_WITHIN_WINDOW',
    next_run_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_run_at TIMESTAMP WITH TIME ZONE,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_schedules_next_run ON task_schedules(next_run_at ASC);

CREATE TABLE task_executions (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES ai_tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    idempotency_key VARCHAR(128) NOT NULL,
    trigger_type VARCHAR(24) NOT NULL DEFAULT 'SCHEDULED',
    status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    attempt_count INTEGER NOT NULL DEFAULT 1,
    step_checkpoints_json TEXT NOT NULL DEFAULT '{}',
    result_summary TEXT,
    result_items_json TEXT NOT NULL DEFAULT '[]',
    content_fingerprints_json TEXT NOT NULL DEFAULT '[]',
    verified_citations_json TEXT NOT NULL DEFAULT '[]',
    tokens_consumed INTEGER NOT NULL DEFAULT 0,
    error_code VARCHAR(64),
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_task_execution_idempotency UNIQUE (task_id, idempotency_key)
);
CREATE INDEX idx_executions_user_started ON task_executions(user_id, started_at DESC);

CREATE TABLE ai_agents (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    default_model_tier VARCHAR(32) NOT NULL DEFAULT 'FAST',
    system_prompt_template TEXT NOT NULL,
    allowed_tool_codes_json TEXT NOT NULL DEFAULT '[]',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_tools (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(48) NOT NULL,
    permission_level SMALLINT NOT NULL CHECK (permission_level BETWEEN 1 AND 4),
    input_schema_json TEXT NOT NULL,
    timeout_seconds INTEGER NOT NULL DEFAULT 15,
    rate_limit_per_hour INTEGER NOT NULL DEFAULT 60,
    is_enabled_globally BOOLEAN NOT NULL DEFAULT TRUE,
    version VARCHAR(24) NOT NULL DEFAULT '1.0.0'
);

CREATE TABLE user_tool_permissions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tool_id UUID NOT NULL REFERENCES ai_tools(id) ON DELETE CASCADE,
    policy_override VARCHAR(24) NOT NULL DEFAULT 'DEFAULT',
    daily_call_limit INTEGER NOT NULL DEFAULT 100,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_tool UNIQUE (user_id, tool_id)
);

CREATE TABLE task_approval_requests (
    id UUID PRIMARY KEY,
    execution_id UUID REFERENCES task_executions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tool_code VARCHAR(64) NOT NULL,
    proposed_payload_json TEXT NOT NULL,
    langgraph_checkpoint_json TEXT NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_approvals_user_status ON task_approval_requests(user_id, status);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    execution_id UUID REFERENCES task_executions(id) ON DELETE SET NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    web_push_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read, created_at DESC);

CREATE TABLE email_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    execution_id UUID REFERENCES task_executions(id) ON DELETE SET NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    provider_message_id VARCHAR(128),
    status VARCHAR(24) NOT NULL,
    error_detail TEXT,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_email_per_execution UNIQUE (execution_id, recipient_email)
);

CREATE TABLE integrations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(48) NOT NULL,
    encrypted_credentials VARBINARY(4096) NOT NULL,
    key_version SMALLINT NOT NULL DEFAULT 1,
    scopes_json TEXT NOT NULL DEFAULT '[]',
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE security_audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(64) NOT NULL,
    severity VARCHAR(16) NOT NULL DEFAULT 'INFO',
    ip_address VARCHAR(64),
    user_agent TEXT,
    resource_type VARCHAR(48),
    resource_id VARCHAR(64),
    metadata_json TEXT NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_user_created ON security_audit_logs(user_id, created_at DESC);


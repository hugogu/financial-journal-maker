-- AI Analysis Session Tables
-- Feature: 004-ai-analysis-session

-- Analysis Sessions table
CREATE TABLE analysis_sessions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_phase VARCHAR(30) NOT NULL DEFAULT 'PRODUCT',
    analyst_id VARCHAR(100) NOT NULL,
    config_snapshot JSONB,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_analyst_status ON analysis_sessions(analyst_id, status);
CREATE INDEX idx_session_status ON analysis_sessions(status);

-- Session Messages table
CREATE TABLE session_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES analysis_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_session ON session_messages(session_id, created_at);

-- Design Decisions table
CREATE TABLE design_decisions (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES analysis_sessions(id) ON DELETE CASCADE,
    decision_type VARCHAR(30) NOT NULL,
    entity_type VARCHAR(50),
    content JSONB NOT NULL,
    is_confirmed BOOLEAN NOT NULL DEFAULT false,
    linked_entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_decision_session_type ON design_decisions(session_id, decision_type);
CREATE INDEX idx_decision_confirmed ON design_decisions(session_id, is_confirmed);

-- AI Configurations table
CREATE TABLE ai_configurations (
    id BIGSERIAL PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    api_key VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_config_active ON ai_configurations(is_active);

-- Prompt Templates table
CREATE TABLE prompt_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    design_phase VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prompt_phase_active ON prompt_templates(design_phase, is_active);

-- Export Artifacts table
CREATE TABLE export_artifacts (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES analysis_sessions(id) ON DELETE CASCADE,
    artifact_type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    exported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_export_session ON export_artifacts(session_id);

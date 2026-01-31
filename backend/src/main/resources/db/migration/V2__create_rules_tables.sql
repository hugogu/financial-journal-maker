-- V2: Accounting Rules Management Schema
-- Feature: 002-accounting-rules

-- Accounting Rules
CREATE TABLE accounting_rules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    shared_across_scenarios BOOLEAN NOT NULL DEFAULT FALSE,
    current_version INTEGER NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_rule_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_rules_code ON accounting_rules(code);
CREATE INDEX idx_rules_status ON accounting_rules(status);
CREATE INDEX idx_rules_shared ON accounting_rules(shared_across_scenarios);

-- Rule Versions (unlimited history, no auto-purge)
CREATE TABLE accounting_rule_versions (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL REFERENCES accounting_rules(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    snapshot_json TEXT NOT NULL,
    change_description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT uk_rule_version UNIQUE (rule_id, version_number)
);

CREATE INDEX idx_versions_rule_id ON accounting_rule_versions(rule_id);
CREATE INDEX idx_versions_created_at ON accounting_rule_versions(created_at);

-- Entry Templates (1:1 with AccountingRule)
CREATE TABLE entry_templates (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL UNIQUE REFERENCES accounting_rules(id) ON DELETE CASCADE,
    description TEXT,
    variable_schema_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Entry Lines (debit/credit lines within templates)
CREATE TABLE entry_lines (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES entry_templates(id) ON DELETE CASCADE,
    sequence_number INTEGER NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount_expression VARCHAR(500) NOT NULL,
    memo_template VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT uk_line_sequence UNIQUE (template_id, sequence_number)
);

CREATE INDEX idx_entry_lines_template_id ON entry_lines(template_id);
CREATE INDEX idx_entry_lines_account_code ON entry_lines(account_code);

-- Trigger Conditions (composable AND/OR logic stored as JSON)
CREATE TABLE trigger_conditions (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL REFERENCES accounting_rules(id) ON DELETE CASCADE,
    condition_json TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trigger_conditions_rule_id ON trigger_conditions(rule_id);

-- Updated timestamp triggers (reuse function from V1 if exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'update_updated_at_column') THEN
        CREATE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS $func$
        BEGIN
            NEW.updated_at = CURRENT_TIMESTAMP;
            RETURN NEW;
        END;
        $func$ language 'plpgsql';
    END IF;
END$$;

CREATE TRIGGER update_accounting_rules_updated_at
    BEFORE UPDATE ON accounting_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_entry_templates_updated_at
    BEFORE UPDATE ON entry_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_entry_lines_updated_at
    BEFORE UPDATE ON entry_lines
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trigger_conditions_updated_at
    BEFORE UPDATE ON trigger_conditions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

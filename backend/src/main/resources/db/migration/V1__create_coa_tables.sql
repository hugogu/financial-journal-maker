-- Chart of Accounts Management Database Schema
-- Version: 1.0.0
-- Description: Creates tables for accounts, mappings, references, and import jobs

-- Accounts table: Core account data with hierarchical structure
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    parent_id BIGINT REFERENCES accounts(id) ON DELETE RESTRICT,
    shared_across_scenarios BOOLEAN NOT NULL DEFAULT false,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Indexes for accounts table
CREATE INDEX idx_accounts_parent ON accounts(parent_id);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);
CREATE INDEX idx_accounts_code ON accounts(code);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Account mappings table: Maps COA codes to Formance Ledger accounts
CREATE TABLE account_mappings (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL UNIQUE REFERENCES accounts(code) ON DELETE CASCADE,
    formance_ledger_account VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for account_mappings table
CREATE INDEX idx_mappings_ledger_account ON account_mappings(formance_ledger_account);

CREATE TRIGGER update_account_mappings_updated_at
    BEFORE UPDATE ON account_mappings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Account references table: Tracks where accounts are used
CREATE TABLE account_references (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL REFERENCES accounts(code) ON DELETE RESTRICT,
    reference_source_id VARCHAR(255) NOT NULL,
    reference_type VARCHAR(50) NOT NULL CHECK (reference_type IN ('RULE', 'SCENARIO')),
    reference_description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_account_ref_composite UNIQUE (account_code, reference_source_id, reference_type)
);

-- Indexes for account_references table
CREATE INDEX idx_refs_account_code ON account_references(account_code);
CREATE INDEX idx_refs_source_id ON account_references(reference_source_id);

-- Import jobs table: Tracks batch import operations
CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_format VARCHAR(20) NOT NULL CHECK (file_format IN ('EXCEL', 'CSV')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    total_records INTEGER NOT NULL DEFAULT 0,
    processed_records INTEGER NOT NULL DEFAULT 0,
    failed_records INTEGER NOT NULL DEFAULT 0,
    error_details TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- Indexes for import_jobs table
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_created_at ON import_jobs(created_at);

-- Comments for documentation
COMMENT ON TABLE accounts IS 'Hierarchical chart of accounts structure';
COMMENT ON TABLE account_mappings IS 'Mappings between COA codes and Formance Ledger accounts';
COMMENT ON TABLE account_references IS 'Tracks where accounts are referenced (rules/scenarios)';
COMMENT ON TABLE import_jobs IS 'Audit trail for batch import operations';

COMMENT ON COLUMN accounts.code IS 'Unique account code (alphanumeric with dots/hyphens)';
COMMENT ON COLUMN accounts.parent_id IS 'Self-reference for hierarchical structure';
COMMENT ON COLUMN accounts.shared_across_scenarios IS 'Whether account can be reused across scenarios';
COMMENT ON COLUMN accounts.version IS 'Optimistic locking version';

COMMENT ON COLUMN account_references.reference_type IS 'Type of reference: RULE or SCENARIO';
COMMENT ON COLUMN import_jobs.error_details IS 'JSON array of validation errors';

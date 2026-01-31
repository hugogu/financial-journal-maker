-- V003: Create Product, Scenario, TransactionType tables
-- Feature: 003-product-scenario-types

-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    business_model TEXT,
    participants TEXT,
    fund_flow TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_products_code ON products(code);
CREATE INDEX idx_products_status ON products(status);

-- Scenarios table
CREATE TABLE scenarios (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    trigger_description TEXT,
    fund_flow_path TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(product_id, code)
);

CREATE INDEX idx_scenarios_product_id ON scenarios(product_id);
CREATE INDEX idx_scenarios_code ON scenarios(code);
CREATE INDEX idx_scenarios_status ON scenarios(status);

-- Transaction Types table
CREATE TABLE transaction_types (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES scenarios(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(scenario_id, code)
);

CREATE INDEX idx_transaction_types_scenario_id ON transaction_types(scenario_id);
CREATE INDEX idx_transaction_types_code ON transaction_types(code);
CREATE INDEX idx_transaction_types_status ON transaction_types(status);

-- Transaction Type Rules junction table
CREATE TABLE transaction_type_rules (
    id BIGSERIAL PRIMARY KEY,
    transaction_type_id BIGINT NOT NULL REFERENCES transaction_types(id),
    rule_id BIGINT NOT NULL REFERENCES accounting_rules(id),
    sequence_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    UNIQUE(transaction_type_id, rule_id)
);

CREATE INDEX idx_transaction_type_rules_type_id ON transaction_type_rules(transaction_type_id);
CREATE INDEX idx_transaction_type_rules_rule_id ON transaction_type_rules(rule_id);

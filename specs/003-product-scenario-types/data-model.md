# Data Model: Product/Scenario/TransactionType Module

**Feature**: 003-product-scenario-types  
**Date**: 2026-01-31

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              PRODUCT                                     │
│  PK: id (BIGINT)                                                        │
│  UK: code (VARCHAR 50)                                                  │
│  ─────────────────────────────────────────────────────────────────────  │
│  name, description, business_model, participants, fund_flow             │
│  status (ENUM), version, created_at, updated_at, created_by, updated_by │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ 1:N
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              SCENARIO                                    │
│  PK: id (BIGINT)                                                        │
│  FK: product_id → PRODUCT                                               │
│  UK: (product_id, code)                                                 │
│  ─────────────────────────────────────────────────────────────────────  │
│  code, name, description, trigger_description, fund_flow_path           │
│  status (ENUM), version, created_at, updated_at, created_by, updated_by │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ 1:N
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          TRANSACTION_TYPE                                │
│  PK: id (BIGINT)                                                        │
│  FK: scenario_id → SCENARIO                                             │
│  UK: (scenario_id, code)                                                │
│  ─────────────────────────────────────────────────────────────────────  │
│  code, name, description                                                │
│  status (ENUM), version, created_at, updated_at, created_by, updated_by │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ N:M (via join table)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      TRANSACTION_TYPE_RULE                               │
│  PK: id (BIGINT)                                                        │
│  FK: transaction_type_id → TRANSACTION_TYPE                             │
│  FK: rule_id → ACCOUNTING_RULES (existing)                              │
│  UK: (transaction_type_id, rule_id)                                     │
│  ─────────────────────────────────────────────────────────────────────  │
│  sequence_number, created_at, created_by                                │
└─────────────────────────────────────────────────────────────────────────┘
```

## Entities

### Product

业务产品线定义，层级结构的根节点。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO | 主键 |
| code | VARCHAR(50) | UK, NOT NULL | 产品代码，全局唯一 |
| name | VARCHAR(200) | NOT NULL | 产品名称 |
| description | TEXT | | 业务描述（Markdown） |
| business_model | TEXT | | 业务模式描述（Markdown） |
| participants | TEXT | | 参与方描述（Markdown） |
| fund_flow | TEXT | | 资金流向描述（Markdown） |
| status | ENUM | NOT NULL, DEFAULT 'DRAFT' | 状态：DRAFT, ACTIVE, ARCHIVED |
| version | BIGINT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| created_by | VARCHAR(100) | | 创建人 |
| updated_by | VARCHAR(100) | | 更新人 |

**Indexes**:
- `idx_product_code` on `code`
- `idx_product_status` on `status`

**State Transitions**:
```
DRAFT ──activate──► ACTIVE ──archive──► ARCHIVED
  ▲                                        │
  └──────────────restore───────────────────┘
```

### Scenario

产品下的业务场景。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO | 主键 |
| product_id | BIGINT | FK, NOT NULL | 所属产品 |
| code | VARCHAR(50) | NOT NULL | 场景代码，产品内唯一 |
| name | VARCHAR(200) | NOT NULL | 场景名称 |
| description | TEXT | | 业务描述（Markdown） |
| trigger_description | TEXT | | 触发条件描述（Markdown） |
| fund_flow_path | TEXT | | 资金流转路径描述（Markdown） |
| status | ENUM | NOT NULL, DEFAULT 'DRAFT' | 状态：DRAFT, ACTIVE, ARCHIVED |
| version | BIGINT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| created_by | VARCHAR(100) | | 创建人 |
| updated_by | VARCHAR(100) | | 更新人 |

**Constraints**:
- UK: `(product_id, code)` - 同一产品下code唯一
- FK: `product_id` references `products(id)` ON DELETE RESTRICT

**Indexes**:
- `idx_scenario_product` on `product_id`
- `idx_scenario_code` on `code`
- `idx_scenario_status` on `status`

**Business Rules**:
- Cannot create new scenario if parent product status is ARCHIVED

### TransactionType

场景下的交易类型。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO | 主键 |
| scenario_id | BIGINT | FK, NOT NULL | 所属场景 |
| code | VARCHAR(50) | NOT NULL | 类型代码，场景内唯一 |
| name | VARCHAR(200) | NOT NULL | 类型名称 |
| description | TEXT | | 业务描述（Markdown） |
| status | ENUM | NOT NULL, DEFAULT 'DRAFT' | 状态：DRAFT, ACTIVE, ARCHIVED |
| version | BIGINT | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |
| created_by | VARCHAR(100) | | 创建人 |
| updated_by | VARCHAR(100) | | 更新人 |

**Constraints**:
- UK: `(scenario_id, code)` - 同一场景下code唯一
- FK: `scenario_id` references `scenarios(id)` ON DELETE RESTRICT

**Indexes**:
- `idx_transaction_type_scenario` on `scenario_id`
- `idx_transaction_type_code` on `code`
- `idx_transaction_type_status` on `status`

**Business Rules**:
- Cannot create new type if parent scenario status is ARCHIVED

### TransactionTypeRule

交易类型与记账规则的多对多关联。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGINT | PK, AUTO | 主键 |
| transaction_type_id | BIGINT | FK, NOT NULL | 交易类型 |
| rule_id | BIGINT | FK, NOT NULL | 记账规则 |
| sequence_number | INT | NOT NULL, DEFAULT 0 | 排序号 |
| created_at | TIMESTAMP | NOT NULL | 关联创建时间 |
| created_by | VARCHAR(100) | | 关联创建人 |

**Constraints**:
- UK: `(transaction_type_id, rule_id)` - 防止重复关联
- FK: `transaction_type_id` references `transaction_types(id)` ON DELETE CASCADE
- FK: `rule_id` references `accounting_rules(id)` ON DELETE RESTRICT

**Indexes**:
- `idx_type_rule_type` on `transaction_type_id`
- `idx_type_rule_rule` on `rule_id`

**Business Rules**:
- Only allow association with rules in DRAFT or ACTIVE status (not ARCHIVED)
- When transaction type is deleted, associations are cascade deleted (rules remain)

## Shared Enum: EntityStatus

Reuse pattern from `RuleStatus` in accounting-rules module.

```java
public enum EntityStatus {
    DRAFT,    // Initial state, editable
    ACTIVE,   // In use, limited editing
    ARCHIVED  // Soft deleted, read-only
}
```

## Database Migration

**Migration File**: `V003__create_product_scenario_types.sql`

```sql
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

CREATE INDEX idx_product_code ON products(code);
CREATE INDEX idx_product_status ON products(status);

-- Scenarios table
CREATE TABLE scenarios (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
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
    CONSTRAINT uk_scenario_product_code UNIQUE (product_id, code)
);

CREATE INDEX idx_scenario_product ON scenarios(product_id);
CREATE INDEX idx_scenario_code ON scenarios(code);
CREATE INDEX idx_scenario_status ON scenarios(status);

-- Transaction Types table
CREATE TABLE transaction_types (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES scenarios(id) ON DELETE RESTRICT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_type_scenario_code UNIQUE (scenario_id, code)
);

CREATE INDEX idx_transaction_type_scenario ON transaction_types(scenario_id);
CREATE INDEX idx_transaction_type_code ON transaction_types(code);
CREATE INDEX idx_transaction_type_status ON transaction_types(status);

-- Transaction Type Rules (join table)
CREATE TABLE transaction_type_rules (
    id BIGSERIAL PRIMARY KEY,
    transaction_type_id BIGINT NOT NULL REFERENCES transaction_types(id) ON DELETE CASCADE,
    rule_id BIGINT NOT NULL REFERENCES accounting_rules(id) ON DELETE RESTRICT,
    sequence_number INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uk_type_rule UNIQUE (transaction_type_id, rule_id)
);

CREATE INDEX idx_type_rule_type ON transaction_type_rules(transaction_type_id);
CREATE INDEX idx_type_rule_rule ON transaction_type_rules(rule_id);
```

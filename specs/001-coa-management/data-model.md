# Data Model: Chart of Accounts Management

**Feature**: Chart of Accounts Management  
**Phase**: 1 - Design & Contracts  
**Date**: 2026-01-28

## Overview

This document defines the domain entities, relationships, validation rules, and state transitions for the Chart of Accounts Management module.

---

## Entity Definitions

### 1. Account

The core entity representing a single account in the chart of accounts hierarchy.

**Purpose**: Store account metadata and maintain tree structure via parent-child relationships.

**Attributes**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, Auto-increment | Internal identifier |
| `code` | String(50) | NOT NULL, UNIQUE | Unique account code (e.g., "1000", "1100-01") |
| `name` | String(255) | NOT NULL | Account name (e.g., "Assets", "Cash") |
| `description` | String(1000) | NULLABLE | Optional detailed description |
| `parentId` | Long | FK to Account(id), NULLABLE | Parent account for hierarchy (NULL = root) |
| `sharedAcrossScenarios` | Boolean | NOT NULL, DEFAULT false | Whether account can be reused |
| `version` | Long | NOT NULL, DEFAULT 0 | Optimistic locking version |
| `createdAt` | Timestamp | NOT NULL | Creation timestamp |
| `updatedAt` | Timestamp | NOT NULL | Last modification timestamp |
| `createdBy` | String(100) | NULLABLE | User who created (future auth) |

**Relationships**:
- Self-referential: `parent` (Many-to-One to Account)
- Self-referential: `children` (One-to-Many to Account)
- One-to-One: `mapping` → AccountMapping
- One-to-Many: `references` → AccountReference

**Validation Rules**:
- `code` must match pattern: `^[A-Za-z0-9.-]+$`
- `code` must be unique across all accounts
- `name` must not be blank
- `parentId` must reference existing account or be NULL
- Circular references not allowed (validated at service layer)
- Cannot delete if `children.size() > 0`
- Cannot delete if `references.size() > 0`
- Cannot update `code` if `references.size() > 0`

**State Transitions**:
```
[Created] → [Active]
    ↓
[Active] → [Referenced] (when first reference added)
    ↓
[Referenced] → [Active] (if all references removed)
    ↓
[Active] → [Deleted] (if no children and no references)
```

**Indexes**:
- `idx_accounts_code` (UNIQUE) on `code`
- `idx_accounts_parent` on `parentId`
- `idx_accounts_created_at` on `createdAt` (for audit queries)

---

### 2. AccountMapping

Represents the mapping between a chart of accounts code and a Formance Ledger account.

**Purpose**: Bridge designed accounts to executable ledger accounts.

**Attributes**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, Auto-increment | Internal identifier |
| `accountCode` | String(50) | FK to Account(code), UNIQUE | Source account code |
| `formanceLedgerAccount` | String(255) | NOT NULL | Target ledger account path (e.g., "assets:cash") |
| `createdAt` | Timestamp | NOT NULL | Creation timestamp |
| `updatedAt` | Timestamp | NOT NULL | Last modification timestamp |
| `version` | Long | NOT NULL, DEFAULT 0 | Optimistic locking version |

**Relationships**:
- Many-to-One: `account` → Account (via `accountCode`)

**Validation Rules**:
- `accountCode` must reference existing account
- `formanceLedgerAccount` must not be blank
- One mapping per account (enforced by UNIQUE constraint)
- Formance account path should follow pattern: `[category]:[subcategory]:[...]:account`

**State Transitions**:
```
[Created] → [Active]
    ↓
[Active] → [Updated] (when ledger account changed)
    ↓
[Active] → [Deleted] (when mapping no longer needed)
```

**Indexes**:
- `idx_mappings_account_code` (UNIQUE) on `accountCode`
- `idx_mappings_ledger_account` on `formanceLedgerAccount` (for reverse lookups)

---

### 3. AccountReference

Tracks where an account is being used (by rules or scenarios).

**Purpose**: Enforce immutability constraints and provide reference traceability.

**Attributes**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, Auto-increment | Internal identifier |
| `accountCode` | String(50) | FK to Account(code), NOT NULL | Referenced account code |
| `referenceSourceId` | String(255) | NOT NULL | ID of referencing entity (rule_id or scenario_id) |
| `referenceType` | Enum | NOT NULL | Type of reference: RULE, SCENARIO |
| `referenceDescription` | String(500) | NULLABLE | Optional context about reference |
| `createdAt` | Timestamp | NOT NULL | When reference was created |

**Relationships**:
- Many-to-One: `account` → Account (via `accountCode`)

**Validation Rules**:
- `accountCode` must reference existing account
- `referenceSourceId` must not be blank
- `referenceType` must be one of: RULE, SCENARIO
- Composite uniqueness: (`accountCode`, `referenceSourceId`, `referenceType`)

**State Transitions**:
```
[Created] → [Active] (reference established)
    ↓
[Active] → [Deleted] (when rule/scenario deleted)
```

**Indexes**:
- `idx_refs_account_code` on `accountCode` (for "list all references for account" queries)
- `idx_refs_source_id` on `referenceSourceId` (for "find all accounts used by rule/scenario")
- `idx_refs_composite` on (`accountCode`, `referenceSourceId`, `referenceType`) UNIQUE

---

### 4. ImportJob

Tracks batch import operations from Excel/CSV files.

**Purpose**: Provide audit trail and async processing status for imports.

**Attributes**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | PK, Auto-increment | Internal identifier |
| `fileName` | String(255) | NOT NULL | Original uploaded file name |
| `fileFormat` | Enum | NOT NULL | EXCEL or CSV |
| `status` | Enum | NOT NULL | PENDING, PROCESSING, COMPLETED, FAILED |
| `totalRecords` | Integer | NOT NULL, DEFAULT 0 | Total accounts in file |
| `processedRecords` | Integer | NOT NULL, DEFAULT 0 | Accounts successfully imported |
| `failedRecords` | Integer | NOT NULL, DEFAULT 0 | Accounts that failed validation |
| `errorDetails` | Text | NULLABLE | JSON array of error messages |
| `startedAt` | Timestamp | NULLABLE | When processing started |
| `completedAt` | Timestamp | NULLABLE | When processing finished |
| `createdAt` | Timestamp | NOT NULL | When job was created |
| `createdBy` | String(100) | NULLABLE | User who initiated import |

**Relationships**:
- None (standalone audit entity)

**Validation Rules**:
- `fileName` must not be blank
- `fileFormat` must be EXCEL or CSV
- `status` transitions: PENDING → PROCESSING → (COMPLETED | FAILED)
- `startedAt` must be after `createdAt`
- `completedAt` must be after `startedAt`
- `processedRecords + failedRecords ≤ totalRecords`

**State Transitions**:
```
[Created] → PENDING
    ↓
PENDING → PROCESSING (when worker picks up job)
    ↓
PROCESSING → COMPLETED (all records processed successfully)
    ↓
PROCESSING → FAILED (validation errors or system failure)
```

**Indexes**:
- `idx_import_jobs_status` on `status` (for querying active jobs)
- `idx_import_jobs_created_at` on `createdAt` (for history queries)

---

## Domain Relationships Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Account                              │
│  ─────────────────────────────────────────────────────────  │
│  id, code (unique), name, description                        │
│  parentId (self-ref), sharedAcrossScenarios                  │
│  version, createdAt, updatedAt                               │
└───────┬─────────────────────────┬───────────────────────────┘
        │                         │
        │ 1:1                     │ 1:N
        │                         │
        ▼                         ▼
┌───────────────────┐     ┌──────────────────────┐
│ AccountMapping    │     │ AccountReference     │
│ ───────────────── │     │ ──────────────────── │
│ accountCode (FK)  │     │ accountCode (FK)     │
│ formanceLedger... │     │ referenceSourceId    │
│ version           │     │ referenceType        │
└───────────────────┘     │ createdAt            │
                          └──────────────────────┘

┌──────────────────────────┐
│      ImportJob           │
│  ──────────────────────  │
│  fileName, fileFormat    │
│  status, totalRecords    │
│  errorDetails            │
│  startedAt, completedAt  │
└──────────────────────────┘
    (standalone - no FK relationships)
```

---

## Business Rules

### Account Creation
1. `code` must be unique across all accounts
2. `parentId` must reference existing account or be NULL
3. Parent-child relationship cannot create cycles
4. Maximum tree depth: 10 levels (soft limit, not enforced by DB)

### Account Update
1. `code` cannot be changed if account has references
2. `name` and `description` can always be updated
3. `parentId` can be changed if no cycles created
4. Version must match for optimistic locking

### Account Deletion
1. Cannot delete if account has child accounts
2. Cannot delete if account has references (rules/scenarios)
3. Cascade delete mapping when account deleted
4. Soft delete NOT implemented (explicit hard delete)

### Account Mapping
1. One mapping per account (1:1 relationship)
2. Mapping can be updated at any time
3. Mapping deletion is allowed (unmaps account)

### Account Reference
1. Adding reference marks account as immutable (code cannot change)
2. Reference can only be deleted by owning module (rule/scenario)
3. Multiple references from different sources allowed

### File Import
1. Entire file validated before any account created (transactional)
2. Duplicate codes in file rejected
3. Circular parent references in file rejected
4. Missing parent codes in file rejected
5. Import creates accounts in dependency order (parents before children)
6. Import job status tracked for async processing

---

## DTO Schemas

### AccountDto (Create/Update Request)

```json
{
  "code": "1000",
  "name": "Assets",
  "description": "All company assets",
  "parentCode": null,
  "sharedAcrossScenarios": true
}
```

### AccountResponseDto (API Response)

```json
{
  "id": 123,
  "code": "1000",
  "name": "Assets",
  "description": "All company assets",
  "parentCode": null,
  "hasChildren": true,
  "isReferenced": false,
  "sharedAcrossScenarios": true,
  "version": 1,
  "createdAt": "2026-01-28T10:00:00Z",
  "updatedAt": "2026-01-28T10:00:00Z"
}
```

### AccountTreeDto (Tree Structure Response)

```json
{
  "code": "1000",
  "name": "Assets",
  "children": [
    {
      "code": "1100",
      "name": "Current Assets",
      "children": [
        {
          "code": "1110",
          "name": "Cash",
          "children": []
        }
      ]
    }
  ]
}
```

### AccountMappingDto

```json
{
  "accountCode": "1000",
  "formanceLedgerAccount": "assets:current:cash",
  "createdAt": "2026-01-28T10:00:00Z",
  "updatedAt": "2026-01-28T10:00:00Z"
}
```

### ImportRequestDto

```json
{
  "fileName": "chart_of_accounts.xlsx",
  "fileFormat": "EXCEL",
  "validateOnly": false
}
```

### ImportJobResponseDto

```json
{
  "id": 456,
  "fileName": "chart_of_accounts.xlsx",
  "fileFormat": "EXCEL",
  "status": "COMPLETED",
  "totalRecords": 500,
  "processedRecords": 500,
  "failedRecords": 0,
  "errorDetails": null,
  "startedAt": "2026-01-28T10:00:00Z",
  "completedAt": "2026-01-28T10:00:05Z"
}
```

### AccountReferenceDto

```json
{
  "accountCode": "1000",
  "referenceSourceId": "rule-123",
  "referenceType": "RULE",
  "referenceDescription": "Cash receipt accounting rule",
  "createdAt": "2026-01-28T10:00:00Z"
}
```

### ErrorResponseDto

```json
{
  "timestamp": "2026-01-28T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete account: referenced by 3 rules",
  "path": "/api/v1/accounts/1000",
  "errorCode": "ACCOUNT_REFERENCED",
  "details": {
    "accountCode": "1000",
    "referenceCount": 3
  }
}
```

---

## Database Schema (SQL)

```sql
-- Accounts table
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

CREATE INDEX idx_accounts_parent ON accounts(parent_id);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

-- Account mappings table
CREATE TABLE account_mappings (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL UNIQUE REFERENCES accounts(code) ON DELETE CASCADE,
    formance_ledger_account VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mappings_ledger_account ON account_mappings(formance_ledger_account);

-- Account references table
CREATE TABLE account_references (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL REFERENCES accounts(code) ON DELETE RESTRICT,
    reference_source_id VARCHAR(255) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_account_ref_composite UNIQUE (account_code, reference_source_id, reference_type)
);

CREATE INDEX idx_refs_account_code ON account_references(account_code);
CREATE INDEX idx_refs_source_id ON account_references(reference_source_id);

-- Import jobs table
CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_format VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_records INTEGER NOT NULL DEFAULT 0,
    processed_records INTEGER NOT NULL DEFAULT 0,
    failed_records INTEGER NOT NULL DEFAULT 0,
    error_details TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_created_at ON import_jobs(created_at);
```

---

## Validation Summary

| Entity | Validation Rules | Enforcement |
|--------|------------------|-------------|
| Account | Unique code, valid parent, no cycles | DB constraint + Service layer |
| Account | Code immutable if referenced | Service layer |
| Account | Cannot delete with children | Service layer |
| AccountMapping | One mapping per account | DB UNIQUE constraint |
| AccountReference | Unique composite key | DB UNIQUE constraint |
| ImportJob | Status transitions | Service layer |

---

## Next Steps

Data model complete. Ready to generate:
- `contracts/coa-api.yaml` - OpenAPI specification
- `quickstart.md` - Setup and usage guide
- Update agent context with technology decisions

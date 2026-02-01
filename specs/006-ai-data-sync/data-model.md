# Data Model: AI-System Data Bidirectional Sync

**Feature**: 006-ai-data-sync  
**Date**: 2026-02-01  
**Source**: Derived from spec.md requirements and research.md decisions

## Entity Overview

This feature introduces 3 new entities and extends 5 existing entities with metadata fields.

### New Entities

1. **DataSyncContext** - Tracks sync state per AI session
2. **SyncOperation** - Records individual sync actions (audit trail)
3. **ConflictResolution** - Stores conflict detection and resolution history

### Extended Entities

Existing entities gain optional AI provenance metadata:
- Product
- Scenario  
- Account
- AccountingRule
- TransactionType

---

## Entity Definitions

### DataSyncContext

**Purpose**: Maintains bidirectional mapping between AI conversation sessions and system entities.

**Relationships**:
- One-to-one with `AnalysisSession` (existing AI session entity)
- One-to-many with `SyncOperation`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| sessionId | Long | PK, FK to analysis_sessions | Links to AI conversation session |
| loadedEntities | JSONB | NOT NULL | Map of entity types to loaded IDs: `{"products": [1,2], "accounts": [10,11]}` |
| createdEntities | JSONB | NOT NULL | Map of entity types to created IDs: `{"products": [3], "rules": [5]}` |
| updatedEntities | JSONB | NOT NULL | Map of entity types to updated IDs: `{"scenarios": [7]}` |
| lastSyncAt | Timestamp | NULL | Last successful sync operation timestamp |
| createdAt | Timestamp | NOT NULL | Context creation timestamp |
| updatedAt | Timestamp | NOT NULL | Last update timestamp |

**Validation Rules**:
- sessionId must reference valid AnalysisSession
- JSONB fields must be valid JSON objects (enforced by PostgreSQL)
- Entity IDs in JSONB must reference existing entities (validated at application layer)

**State Transitions**: None (data container only)

**Indexes**:
- Primary key on sessionId
- Index on lastSyncAt for cleanup queries

---

### SyncOperation

**Purpose**: Audit trail for every data synchronization action.

**Relationships**:
- Many-to-one with `DataSyncContext`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto-increment | Unique operation ID |
| sessionId | Long | FK to data_sync_contexts, NOT NULL | Session this operation belongs to |
| operationType | Enum | NOT NULL | READ, CREATE, UPDATE, DELETE |
| entityType | String(50) | NOT NULL | Product, Scenario, Account, AccountingRule, TransactionType |
| entityId | Long | NULL | ID of affected entity (NULL for failed operations) |
| entityCode | String(50) | NULL | Code of affected entity (for reference) |
| status | Enum | NOT NULL | PENDING, SUCCESS, FAILED, ROLLED_BACK |
| errorMessage | Text | NULL | Error details if status=FAILED |
| metadata | JSONB | NULL | Operation-specific data (e.g., conflict details) |
| createdAt | Timestamp | NOT NULL | Operation timestamp |

**Validation Rules**:
- operationType must be valid enum value
- entityType must match known entity types
- If status=SUCCESS, entityId must be NOT NULL
- If status=FAILED, errorMessage should be populated

**State Transitions**:
```
PENDING → SUCCESS (normal flow)
PENDING → FAILED (validation/constraint error)
SUCCESS → ROLLED_BACK (transaction rollback)
```

**Indexes**:
- Primary key on id
- Index on (sessionId, createdAt) for session history queries
- Index on (entityType, entityId) for entity provenance lookups

---

### ConflictResolution

**Purpose**: Records detected conflicts and user resolution choices.

**Relationships**:
- Many-to-one with `DataSyncContext`

**Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto-increment | Unique conflict ID |
| sessionId | Long | FK to data_sync_contexts, NOT NULL | Session where conflict occurred |
| conflictType | Enum | NOT NULL | DUPLICATE_CODE, OVERLAPPING_RULE, CONSTRAINT_VIOLATION |
| entityType | String(50) | NOT NULL | Type of entity causing conflict |
| suggestedCode | String(50) | NULL | Code AI suggested (if applicable) |
| existingEntityId | Long | NULL | ID of conflicting existing entity |
| resolutionOptions | JSONB | NOT NULL | Array of options presented: `["USE_EXISTING", "CREATE_NEW", "UPDATE_EXISTING"]` |
| chosenResolution | String(50) | NULL | User's choice (NULL if unresolved) |
| resolvedAt | Timestamp | NULL | When user made choice |
| createdAt | Timestamp | NOT NULL | Conflict detection timestamp |

**Validation Rules**:
- conflictType must be valid enum value
- If chosenResolution is NOT NULL, resolvedAt must be NOT NULL
- resolutionOptions must be non-empty JSON array

**State Transitions**:
```
DETECTED (chosenResolution=NULL) → RESOLVED (chosenResolution set, resolvedAt set)
```

**Indexes**:
- Primary key on id
- Index on (sessionId, createdAt) for session conflict history
- Index on chosenResolution for analytics

---

## Extended Entity Metadata

### AI Provenance Fields

Add to existing entities: Product, Scenario, Account, AccountingRule, TransactionType

**New Fields**:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| aiCreated | Boolean | DEFAULT FALSE | True if entity created via AI sync |
| aiSessionId | Long | FK to analysis_sessions, NULL | Session that created this entity |
| aiModifiedAt | Timestamp | NULL | Last AI modification timestamp |

**Migration Strategy**:
- Add columns with DEFAULT FALSE / NULL to existing tables
- No data migration needed (existing entities remain non-AI)
- Nullable foreign key to analysis_sessions (optional relationship)

**Usage**:
```sql
-- Find all AI-created products
SELECT * FROM products WHERE ai_created = TRUE;

-- Find entities created in specific session
SELECT * FROM accounts WHERE ai_session_id = 123;

-- Find recently AI-modified rules
SELECT * FROM accounting_rules 
WHERE ai_modified_at > NOW() - INTERVAL '7 days';
```

---

## Relationships Diagram

```
AnalysisSession (existing)
    ↓ 1:1
DataSyncContext
    ↓ 1:N
SyncOperation

DataSyncContext
    ↓ 1:N
ConflictResolution

Product/Scenario/Account/AccountingRule/TransactionType
    ↑ N:1 (optional)
AnalysisSession (via aiSessionId)
```

---

## Database Schema (PostgreSQL DDL)

```sql
-- New table: data_sync_contexts
CREATE TABLE data_sync_contexts (
    session_id BIGINT PRIMARY KEY,
    loaded_entities JSONB NOT NULL DEFAULT '{}',
    created_entities JSONB NOT NULL DEFAULT '{}',
    updated_entities JSONB NOT NULL DEFAULT '{}',
    last_sync_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sync_context_session 
        FOREIGN KEY (session_id) REFERENCES analysis_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_sync_context_last_sync ON data_sync_contexts(last_sync_at);

-- New table: sync_operations
CREATE TABLE sync_operations (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL CHECK (operation_type IN ('READ', 'CREATE', 'UPDATE', 'DELETE')),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    entity_code VARCHAR(50),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'ROLLED_BACK')),
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sync_op_context 
        FOREIGN KEY (session_id) REFERENCES data_sync_contexts(session_id) ON DELETE CASCADE
);

CREATE INDEX idx_sync_op_session ON sync_operations(session_id, created_at);
CREATE INDEX idx_sync_op_entity ON sync_operations(entity_type, entity_id);

-- New table: conflict_resolutions
CREATE TABLE conflict_resolutions (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    conflict_type VARCHAR(50) NOT NULL CHECK (conflict_type IN ('DUPLICATE_CODE', 'OVERLAPPING_RULE', 'CONSTRAINT_VIOLATION')),
    entity_type VARCHAR(50) NOT NULL,
    suggested_code VARCHAR(50),
    existing_entity_id BIGINT,
    resolution_options JSONB NOT NULL,
    chosen_resolution VARCHAR(50),
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conflict_context 
        FOREIGN KEY (session_id) REFERENCES data_sync_contexts(session_id) ON DELETE CASCADE
);

CREATE INDEX idx_conflict_session ON conflict_resolutions(session_id, created_at);
CREATE INDEX idx_conflict_resolution ON conflict_resolutions(chosen_resolution);

-- Extend existing tables with AI provenance
ALTER TABLE products ADD COLUMN ai_created BOOLEAN DEFAULT FALSE;
ALTER TABLE products ADD COLUMN ai_session_id BIGINT REFERENCES analysis_sessions(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN ai_modified_at TIMESTAMP;

ALTER TABLE scenarios ADD COLUMN ai_created BOOLEAN DEFAULT FALSE;
ALTER TABLE scenarios ADD COLUMN ai_session_id BIGINT REFERENCES analysis_sessions(id) ON DELETE SET NULL;
ALTER TABLE scenarios ADD COLUMN ai_modified_at TIMESTAMP;

ALTER TABLE accounts ADD COLUMN ai_created BOOLEAN DEFAULT FALSE;
ALTER TABLE accounts ADD COLUMN ai_session_id BIGINT REFERENCES analysis_sessions(id) ON DELETE SET NULL;
ALTER TABLE accounts ADD COLUMN ai_modified_at TIMESTAMP;

ALTER TABLE accounting_rules ADD COLUMN ai_created BOOLEAN DEFAULT FALSE;
ALTER TABLE accounting_rules ADD COLUMN ai_session_id BIGINT REFERENCES analysis_sessions(id) ON DELETE SET NULL;
ALTER TABLE accounting_rules ADD COLUMN ai_modified_at TIMESTAMP;

ALTER TABLE transaction_types ADD COLUMN ai_created BOOLEAN DEFAULT FALSE;
ALTER TABLE transaction_types ADD COLUMN ai_session_id BIGINT REFERENCES analysis_sessions(id) ON DELETE SET NULL;
ALTER TABLE transaction_types ADD COLUMN ai_modified_at TIMESTAMP;
```

---

## Data Integrity Rules

### Referential Integrity
- DataSyncContext.sessionId → AnalysisSession.id (CASCADE DELETE)
- SyncOperation.sessionId → DataSyncContext.sessionId (CASCADE DELETE)
- ConflictResolution.sessionId → DataSyncContext.sessionId (CASCADE DELETE)
- Entity.aiSessionId → AnalysisSession.id (SET NULL on delete)

### Business Rules
1. **Atomic Sync**: All SyncOperations in a transaction must succeed or all fail
2. **Conflict Before Create**: ConflictResolution must be created before attempting conflicting entity creation
3. **Provenance Tracking**: If entity.aiCreated=TRUE, entity.aiSessionId must be NOT NULL
4. **Operation Ordering**: SyncOperations.createdAt must be monotonically increasing per session

### Validation Constraints
- JSONB fields must parse as valid JSON
- Enum fields must match defined values
- Timestamps must be reasonable (not future dates beyond 1 hour)
- Entity IDs in JSONB must reference existing entities (application-level check)

---

## Query Patterns

### Load Context for AI Session
```sql
SELECT 
    dsc.*,
    COUNT(so.id) as operation_count,
    COUNT(CASE WHEN cr.chosen_resolution IS NULL THEN 1 END) as unresolved_conflicts
FROM data_sync_contexts dsc
LEFT JOIN sync_operations so ON so.session_id = dsc.session_id
LEFT JOIN conflict_resolutions cr ON cr.session_id = dsc.session_id
WHERE dsc.session_id = ?
GROUP BY dsc.session_id;
```

### Find AI-Created Entities by Session
```sql
SELECT 'Product' as type, id, code, name FROM products WHERE ai_session_id = ?
UNION ALL
SELECT 'Scenario', id, code, name FROM scenarios WHERE ai_session_id = ?
UNION ALL
SELECT 'Account', id, code, name FROM accounts WHERE ai_session_id = ?
UNION ALL
SELECT 'Rule', id, code, name FROM accounting_rules WHERE ai_session_id = ?;
```

### Audit Trail for Entity
```sql
SELECT 
    so.operation_type,
    so.status,
    so.created_at,
    ases.title as session_title
FROM sync_operations so
JOIN data_sync_contexts dsc ON dsc.session_id = so.session_id
JOIN analysis_sessions ases ON ases.id = dsc.session_id
WHERE so.entity_type = ? AND so.entity_id = ?
ORDER BY so.created_at DESC;
```

---

## Migration Strategy

### Phase 1: Schema Creation
1. Create new tables (data_sync_contexts, sync_operations, conflict_resolutions)
2. Add AI provenance columns to existing tables
3. Create indexes

### Phase 2: Data Seeding
- No data migration needed (all new entities start empty)
- Existing entities have ai_created=FALSE by default

### Phase 3: Rollback Plan
```sql
-- If rollback needed
DROP TABLE IF EXISTS conflict_resolutions CASCADE;
DROP TABLE IF EXISTS sync_operations CASCADE;
DROP TABLE IF EXISTS data_sync_contexts CASCADE;

ALTER TABLE products DROP COLUMN IF EXISTS ai_created, DROP COLUMN IF EXISTS ai_session_id, DROP COLUMN IF EXISTS ai_modified_at;
ALTER TABLE scenarios DROP COLUMN IF EXISTS ai_created, DROP COLUMN IF EXISTS ai_session_id, DROP COLUMN IF EXISTS ai_modified_at;
ALTER TABLE accounts DROP COLUMN IF EXISTS ai_created, DROP COLUMN IF EXISTS ai_session_id, DROP COLUMN IF EXISTS ai_modified_at;
ALTER TABLE accounting_rules DROP COLUMN IF EXISTS ai_created, DROP COLUMN IF EXISTS ai_session_id, DROP COLUMN IF EXISTS ai_modified_at;
ALTER TABLE transaction_types DROP COLUMN IF EXISTS ai_created, DROP COLUMN IF EXISTS ai_session_id, DROP COLUMN IF EXISTS ai_modified_at;
```

---

## Performance Considerations

### Indexing Strategy
- Primary keys for fast lookups
- Foreign key indexes for join performance
- Composite indexes on (sessionId, createdAt) for timeline queries
- JSONB GIN indexes deferred to v2 (only if JSONB queries become bottleneck)

### Expected Data Volume
- DataSyncContext: 1 per AI session (~1000 sessions/month)
- SyncOperation: 5-10 per session (~5000-10000 operations/month)
- ConflictResolution: 0.5 per session (~500 conflicts/month)

### Cleanup Strategy
- Archive SyncOperations older than 90 days
- Keep DataSyncContext indefinitely (small footprint)
- Keep ConflictResolution for analytics (small volume)

---

## Summary

Data model supports all functional requirements:
- **FR-001 to FR-006**: DataSyncContext.loadedEntities tracks what AI read
- **FR-007 to FR-014**: SyncOperation records all write operations
- **FR-015 to FR-019**: ConflictResolution handles conflict detection/resolution
- **FR-020 to FR-023**: AI provenance fields enable full traceability
- **FR-024 to FR-027**: SyncOperation status enables UI feedback

All entities use existing patterns (JPA, optimistic locking, audit timestamps). No new architectural patterns introduced.

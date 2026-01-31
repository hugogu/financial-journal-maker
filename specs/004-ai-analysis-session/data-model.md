# Data Model: AI Analysis Session

**Feature**: 004-ai-analysis-session
**Date**: 2026-01-31

## Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐
│ AIConfiguration │       │ PromptTemplate  │
├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │
│ providerName    │       │ name            │
│ endpoint        │       │ designPhase     │
│ modelName       │       │ content         │
│ apiKey(encrypt) │       │ version         │
│ isActive        │       │ isActive        │
│ priority        │       │ createdAt       │
│ createdAt       │       │ updatedAt       │
│ updatedAt       │       └─────────────────┘
└─────────────────┘

┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ AnalysisSession │──1:N──│ SessionMessage  │       │ DesignDecision  │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │       │ id              │
│ title           │──1:N──│ sessionId (FK)  │       │ sessionId (FK)  │
│ status          │       │ role            │       │ decisionType    │
│ currentPhase    │       │ content         │       │ entityType      │
│ analystId       │       │ metadata (JSON) │       │ content (JSON)  │
│ configSnapshot  │       │ createdAt       │       │ isConfirmed     │
│ version         │       └─────────────────┘       │ createdAt       │
│ createdAt       │                                 │ updatedAt       │
│ updatedAt       │──1:N──────────────────────────▶ └─────────────────┘
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐
│ ExportArtifact  │
├─────────────────┤
│ id              │
│ sessionId (FK)  │
│ artifactType    │
│ content (TEXT)  │
│ exportedAt      │
└─────────────────┘
```

## Entities

### AnalysisSession

Represents a complete AI-assisted design conversation.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| title | VARCHAR(200) | NOT NULL | User-provided or auto-generated session title |
| status | VARCHAR(20) | NOT NULL | DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED |
| currentPhase | VARCHAR(30) | NOT NULL | PRODUCT, SCENARIO, TRANSACTION_TYPE, ACCOUNTING |
| analystId | VARCHAR(100) | NOT NULL | Identifier for the analyst (placeholder for v2 auth) |
| configSnapshot | JSONB | | Snapshot of AI config at session start |
| version | BIGINT | NOT NULL, DEFAULT 0 | Optimistic locking version |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

**State Transitions**:
```
DRAFT ──create──▶ ACTIVE ──pause──▶ PAUSED
                    │                  │
                    │◀────resume───────┘
                    │
                    ▼
               COMPLETED ──archive──▶ ARCHIVED
```

**Validation Rules**:
- Title max 200 characters
- Status must be valid enum value
- Only ACTIVE sessions can be modified
- Max 5 ACTIVE/PAUSED sessions per analystId

---

### SessionMessage

Individual message in the conversation.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| sessionId | BIGINT | FK, NOT NULL | Reference to AnalysisSession |
| role | VARCHAR(20) | NOT NULL | USER, ASSISTANT, SYSTEM |
| content | TEXT | NOT NULL | Message content (markdown supported) |
| metadata | JSONB | | Additional data (tokens used, model, etc.) |
| createdAt | TIMESTAMP | NOT NULL | Message timestamp |

**Validation Rules**:
- Content cannot be empty
- Role must be valid enum value
- Messages ordered by createdAt within session

---

### DesignDecision

A confirmed design element at any hierarchy level.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| sessionId | BIGINT | FK, NOT NULL | Reference to AnalysisSession |
| decisionType | VARCHAR(30) | NOT NULL | PRODUCT, SCENARIO, TRANSACTION_TYPE, ACCOUNTING |
| entityType | VARCHAR(50) | | Specific entity (e.g., "Product", "AccountingRule") |
| content | JSONB | NOT NULL | Decision details (name, code, attributes) |
| isConfirmed | BOOLEAN | NOT NULL, DEFAULT false | Whether user confirmed this decision |
| linkedEntityId | BIGINT | | Reference to existing system entity if linked |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

**Validation Rules**:
- DecisionType must match session's currentPhase or earlier
- Content schema varies by decisionType
- Only one confirmed decision per (sessionId, decisionType, entityType, code) combination

---

### AIConfiguration

System-level AI settings.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| providerName | VARCHAR(50) | NOT NULL, UNIQUE | Provider identifier (openai, anthropic, azure) |
| displayName | VARCHAR(100) | NOT NULL | Human-readable name |
| endpoint | VARCHAR(500) | NOT NULL | API endpoint URL |
| modelName | VARCHAR(100) | NOT NULL | Model identifier (gpt-4, claude-3, etc.) |
| apiKey | VARCHAR(500) | NOT NULL | Encrypted API key |
| isActive | BOOLEAN | NOT NULL, DEFAULT false | Whether this provider is currently active |
| priority | INTEGER | NOT NULL, DEFAULT 0 | For ordering in admin UI |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

**Validation Rules**:
- Only one provider can be active at a time
- API key encrypted at rest using application secret
- Endpoint must be valid URL format

---

### PromptTemplate

Customizable prompt text for each design phase.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Template name (e.g., "Product Analysis") |
| designPhase | VARCHAR(30) | NOT NULL | PRODUCT, SCENARIO, TRANSACTION_TYPE, ACCOUNTING, SYSTEM |
| content | TEXT | NOT NULL | Prompt content with {{variables}} |
| version | INTEGER | NOT NULL | Version number for history |
| isActive | BOOLEAN | NOT NULL, DEFAULT false | Whether this version is active |
| createdAt | TIMESTAMP | NOT NULL | Creation timestamp |
| updatedAt | TIMESTAMP | NOT NULL | Last update timestamp |

**Validation Rules**:
- One active version per (name, designPhase) combination
- Content supports variables: {{existingProducts}}, {{confirmedDecisions}}, {{userMessage}}, etc.
- Version auto-increments on save

---

### ExportArtifact

Generated export from a completed session.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | BIGSERIAL | PK | Unique identifier |
| sessionId | BIGINT | FK, NOT NULL | Reference to AnalysisSession |
| artifactType | VARCHAR(30) | NOT NULL | COA, RULES, NUMSCRIPT |
| content | TEXT | NOT NULL | Exported content |
| metadata | JSONB | | Export details (entity count, warnings) |
| exportedAt | TIMESTAMP | NOT NULL | Export timestamp |

**Validation Rules**:
- Session must be COMPLETED status to export
- Content format depends on artifactType

## Indexes

```sql
-- Session queries
CREATE INDEX idx_session_analyst_status ON analysis_sessions(analyst_id, status);
CREATE INDEX idx_session_status ON analysis_sessions(status);

-- Message queries  
CREATE INDEX idx_message_session ON session_messages(session_id, created_at);

-- Decision queries
CREATE INDEX idx_decision_session_type ON design_decisions(session_id, decision_type);
CREATE INDEX idx_decision_confirmed ON design_decisions(session_id, is_confirmed);

-- Config queries
CREATE INDEX idx_config_active ON ai_configurations(is_active);

-- Prompt queries
CREATE INDEX idx_prompt_phase_active ON prompt_templates(design_phase, is_active);

-- Export queries
CREATE INDEX idx_export_session ON export_artifacts(session_id);
```

## Migration Script Reference

Migration: `V004__create_ai_session_tables.sql`

Tables created:
1. `analysis_sessions`
2. `session_messages`
3. `design_decisions`
4. `ai_configurations`
5. `prompt_templates`
6. `export_artifacts`

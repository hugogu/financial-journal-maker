# Data Model: Transaction Flow Viewer

**Feature**: 005-transaction-flow-viewer  
**Date**: 2026-01-31

## Overview

This feature is **read-only** and does not introduce new persistent entities. Instead, it defines **view models** and **projection DTOs** that aggregate data from existing entities in 004-ai-analysis-session and 001-coa-management.

## Data Source Relationships

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    EXISTING ENTITIES (from 004/001)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  AnalysisSession ──1:N──▶ DesignDecision ◀── Contains account/entry data    │
│         │                      │                                             │
│         │                      │ decisionType: PRODUCT, SCENARIO,            │
│         │                      │               TRANSACTION_TYPE, ACCOUNTING  │
│         │                      │ content: JSON with design details           │
│         │                      │                                             │
│         └──1:N──▶ ExportArtifact ◀── Contains generated Numscript           │
│                        │                                                     │
│                        │ artifactType: NUMSCRIPT                             │
│                        │ content: Numscript DSL code                         │
│                                                                              │
│  Account (001) ◀── Referenced by code in DesignDecision.content             │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              │
                              │ Projected/Transformed into
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    VIEW MODELS (this feature)                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ProductSummary ◀── Aggregated from PRODUCT decisions                       │
│       │                                                                      │
│       └──1:N──▶ ScenarioSummary ◀── Aggregated from SCENARIO decisions      │
│                      │                                                       │
│                      └──1:N──▶ TransactionFlowView ◀── Complete view model  │
│                                      │                                       │
│                                      ├── AccountNodeDto (computed)           │
│                                      ├── JournalEntryDisplayDto (computed)   │
│                                      ├── FlowConnectionDto (computed)        │
│                                      └── NumscriptViewDto (from export)      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## View Models (DTOs)

### ProductSummary

Aggregated view of a product with counts.

```json
{
  "productCode": "CONSUMER_LOAN",
  "productName": "Consumer Loan Product",
  "description": "Personal loan for retail customers",
  "scenarioCount": 3,
  "transactionTypeCount": 12,
  "sourceSessionId": 101,
  "createdAt": "2026-01-31T10:00:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| productCode | String | Unique product identifier |
| productName | String | Display name |
| description | String | Optional description |
| scenarioCount | Integer | Number of scenarios under this product |
| transactionTypeCount | Integer | Total transaction types across scenarios |
| sourceSessionId | Long | AI session that created this product |
| createdAt | Timestamp | When product was confirmed |

---

### ScenarioSummary

Aggregated view of a scenario under a product.

```json
{
  "scenarioCode": "DISBURSEMENT",
  "scenarioName": "Loan Disbursement",
  "description": "Initial loan fund release to customer",
  "productCode": "CONSUMER_LOAN",
  "transactionTypeCount": 4,
  "sourceSessionId": 101,
  "createdAt": "2026-01-31T10:00:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| scenarioCode | String | Unique scenario identifier within product |
| scenarioName | String | Display name |
| description | String | Optional description |
| productCode | String | Parent product code |
| transactionTypeCount | Integer | Number of transaction types |
| sourceSessionId | Long | AI session that created this scenario |
| createdAt | Timestamp | When scenario was confirmed |

---

### TransactionFlowView

Complete view model for a transaction type with all accounting details.

```json
{
  "transactionTypeCode": "LOAN_DISBURSEMENT_TO_CUSTOMER",
  "transactionTypeName": "Loan Disbursement to Customer Account",
  "description": "Transfer loan principal to customer's linked bank account",
  "productCode": "CONSUMER_LOAN",
  "scenarioCode": "DISBURSEMENT",
  "accounts": [...],
  "journalEntries": [...],
  "flowConnections": [...],
  "numscript": "...",
  "numscriptValid": true,
  "sourceSessionId": 101,
  "createdAt": "2026-01-31T10:00:00Z",
  "updatedAt": "2026-01-31T12:00:00Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| transactionTypeCode | String | Unique transaction type identifier |
| transactionTypeName | String | Display name |
| description | String | Business description |
| productCode | String | Parent product code |
| scenarioCode | String | Parent scenario code |
| accounts | AccountNodeDto[] | All accounts involved |
| journalEntries | JournalEntryDisplayDto[] | All journal entries |
| flowConnections | FlowConnectionDto[] | Cash/info flow edges |
| numscript | String | Generated Numscript DSL |
| numscriptValid | Boolean | Whether Numscript passes validation |
| sourceSessionId | Long | AI session that created this |
| createdAt | Timestamp | Creation time |
| updatedAt | Timestamp | Last update time |

---

### AccountNodeDto

Visual representation of an account for flow diagrams.

```json
{
  "accountCode": "2202-07",
  "accountName": "应付客户-冻结",
  "accountType": "CUSTOMER",
  "accountState": "FROZEN",
  "position": { "x": 200, "y": 150 },
  "linkedToCoA": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| accountCode | String | Account code (may be COA code or temporary design code) |
| accountName | String | Display name |
| accountType | Enum | CUSTOMER, BANK, CHANNEL, REVENUE, COST, OTHER |
| accountState | Enum | AVAILABLE, FROZEN, IN_TRANSIT, null |
| position | Position | X,Y coordinates for diagram (computed by layout) |
| linkedToCoA | Boolean | Whether linked to existing Chart of Accounts entry |

**Account Type Enum**:
```
CUSTOMER   - Customer-facing accounts (teal #14B8A6)
BANK       - Bank/settlement accounts (gray #9CA3AF)
CHANNEL    - Payment channel accounts (blue #3B82F6)
REVENUE    - Income/revenue P&L accounts (green #22C55E)
COST       - Expense/cost P&L accounts (pink #F472B6)
OTHER      - Uncategorized accounts (gray #6B7280)
```

**Account State Enum**:
```
AVAILABLE  - Normal available balance (solid border)
FROZEN     - Frozen/held balance (dotted border)
IN_TRANSIT - In-transit/pending balance (dashed border)
null       - No specific state (default solid)
```

---

### JournalEntryDisplayDto

Presentation model for a single journal entry line.

```json
{
  "entryId": "entry-001",
  "operation": "DR",
  "accountCode": "2202-07",
  "accountName": "应付客户-冻结",
  "amountExpression": "1015$",
  "triggerEvent": "ON_ORDER_CREATION",
  "triggerEventLabel": "订单创建时",
  "condition": null,
  "sequenceNumber": 1,
  "groupId": "group-freeze"
}
```

| Field | Type | Description |
|-------|------|-------------|
| entryId | String | Unique entry identifier within transaction |
| operation | Enum | DR (debit) or CR (credit) |
| accountCode | String | Target account code |
| accountName | String | Target account display name |
| amountExpression | String | Amount or formula (e.g., "1000$", "amount * 0.015") |
| triggerEvent | String | Event code that triggers this entry |
| triggerEventLabel | String | Human-readable trigger label |
| condition | String | Optional condition (e.g., "fee > 0") |
| sequenceNumber | Integer | Order within trigger event group |
| groupId | String | Logical grouping for related entries |

**Operation Enum**:
```
DR - Debit
CR - Credit
```

---

### FlowConnectionDto

Edge representing fund or information flow between accounts.

```json
{
  "connectionId": "conn-001",
  "sourceAccountCode": "2202-01",
  "targetAccountCode": "2202-07",
  "flowType": "CASH",
  "amountExpression": "1015$",
  "label": "冻结金额",
  "triggerEvent": "ON_ORDER_CREATION",
  "condition": null
}
```

| Field | Type | Description |
|-------|------|-------------|
| connectionId | String | Unique connection identifier |
| sourceAccountCode | String | Source account code |
| targetAccountCode | String | Target account code |
| flowType | Enum | CASH (solid arrow) or INFO (dashed arrow) |
| amountExpression | String | Amount/formula for cash flows |
| label | String | Edge label text |
| triggerEvent | String | When this flow occurs |
| condition | String | Optional condition |

**Flow Type Enum**:
```
CASH - Actual fund movement (solid arrow)
INFO - Information/notification flow (dashed arrow)
```

---

### NumscriptViewDto

Numscript code with metadata.

```json
{
  "numscript": "vars {\n  monetary $amount\n}\nsend $amount (\n  source = @customer:available\n  destination = @customer:frozen\n)",
  "isValid": true,
  "validationErrors": [],
  "generatedAt": "2026-01-31T12:00:00Z",
  "sourceArtifactId": 456
}
```

| Field | Type | Description |
|-------|------|-------------|
| numscript | String | Complete Numscript DSL code |
| isValid | Boolean | Syntax validation result |
| validationErrors | String[] | List of validation errors if invalid |
| generatedAt | Timestamp | When Numscript was generated |
| sourceArtifactId | Long | ExportArtifact ID if from stored export |

---

### DesignPreviewDto

Real-time preview state during AI session.

```json
{
  "sessionId": 101,
  "currentPhase": "ACCOUNTING",
  "tentativeAccounts": [...],
  "confirmedAccounts": [...],
  "tentativeEntries": [...],
  "confirmedEntries": [...],
  "previewNumscript": "...",
  "lastUpdated": "2026-01-31T12:00:00.123Z"
}
```

| Field | Type | Description |
|-------|------|-------------|
| sessionId | Long | Active session ID |
| currentPhase | String | Current design phase |
| tentativeAccounts | AccountNodeDto[] | AI-proposed but not confirmed |
| confirmedAccounts | AccountNodeDto[] | User-confirmed accounts |
| tentativeEntries | JournalEntryDisplayDto[] | AI-proposed entries |
| confirmedEntries | JournalEntryDisplayDto[] | User-confirmed entries |
| previewNumscript | String | On-demand generated Numscript preview |
| lastUpdated | Timestamp | Last preview update time |

---

### TransactionTimelineDto

Timeline view for multi-timing transactions.

```json
{
  "transactionTypeCode": "LOAN_DISBURSEMENT",
  "events": [
    {
      "eventCode": "T0_ORDER_CREATION",
      "eventLabel": "订单创建 (T+0)",
      "timeOffset": "T+0",
      "isRealtime": true,
      "journalEntryIds": ["entry-001", "entry-002"]
    },
    {
      "eventCode": "T1_SETTLEMENT",
      "eventLabel": "渠道清算 (T+1)",
      "timeOffset": "T+1",
      "isRealtime": false,
      "journalEntryIds": ["entry-003", "entry-004"]
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| transactionTypeCode | String | Transaction type identifier |
| events | TimelineEventDto[] | Ordered list of events |

**TimelineEventDto**:
| Field | Type | Description |
|-------|------|-------------|
| eventCode | String | Event identifier |
| eventLabel | String | Display label |
| timeOffset | String | Timing (T+0, T+1, etc.) |
| isRealtime | Boolean | True if real-time, false if batch |
| journalEntryIds | String[] | Associated journal entry IDs |

---

## Data Extraction from DesignDecision

The `content` JSONB field in DesignDecision stores different structures based on `decisionType`:

### PRODUCT content schema:
```json
{
  "code": "CONSUMER_LOAN",
  "name": "Consumer Loan Product",
  "description": "..."
}
```

### SCENARIO content schema:
```json
{
  "code": "DISBURSEMENT",
  "name": "Loan Disbursement",
  "description": "...",
  "productCode": "CONSUMER_LOAN"
}
```

### TRANSACTION_TYPE content schema:
```json
{
  "code": "LOAN_DISBURSEMENT_TO_CUSTOMER",
  "name": "Loan Disbursement to Customer",
  "description": "...",
  "scenarioCode": "DISBURSEMENT"
}
```

### ACCOUNTING content schema:
```json
{
  "transactionTypeCode": "LOAN_DISBURSEMENT_TO_CUSTOMER",
  "accounts": [
    {
      "code": "2202-01",
      "name": "应付客户-可用",
      "type": "CUSTOMER",
      "state": "AVAILABLE"
    }
  ],
  "entries": [
    {
      "operation": "DR",
      "accountCode": "2202-01",
      "amountExpression": "1015$",
      "triggerEvent": "ON_ORDER_CREATION",
      "condition": null
    }
  ],
  "flows": [
    {
      "source": "2202-01",
      "target": "2202-07",
      "type": "CASH",
      "amount": "1015$"
    }
  ]
}
```

---

## JPA Repository Projections

Since this feature reads from existing tables, we use Spring Data JPA projections:

```java
// ProductSummaryProjection
public interface ProductSummaryProjection {
    String getProductCode();
    String getProductName();
    String getDescription();
    Integer getScenarioCount();
    Integer getTransactionTypeCount();
    Long getSourceSessionId();
    Instant getCreatedAt();
}

// Native query example
@Query(value = """
    SELECT 
        d.content->>'code' as productCode,
        d.content->>'name' as productName,
        d.content->>'description' as description,
        (SELECT COUNT(*) FROM design_decisions s 
         WHERE s.decision_type = 'SCENARIO' 
         AND s.content->>'productCode' = d.content->>'code'
         AND s.is_confirmed = true) as scenarioCount,
        d.session_id as sourceSessionId,
        d.created_at as createdAt
    FROM design_decisions d
    WHERE d.decision_type = 'PRODUCT' 
    AND d.is_confirmed = true
    """, nativeQuery = true)
List<ProductSummaryProjection> findAllProductSummaries();
```

---

## No New Tables Required

This feature does not require database migrations. All data is derived from:
- `design_decisions` (004-ai-analysis-session)
- `export_artifacts` (004-ai-analysis-session)
- `accounts` (001-coa-management) - for COA linking verification

---

## Validation Rules

| View Model | Validation | Enforcement |
|------------|------------|-------------|
| TransactionFlowView | All accounts must have valid codes | Service layer |
| JournalEntryDisplayDto | DR/CR entries must balance | Service layer warning |
| NumscriptViewDto | Numscript syntax validation | Numscript validator service |
| FlowConnectionDto | Source/target must exist in accounts | Service layer |

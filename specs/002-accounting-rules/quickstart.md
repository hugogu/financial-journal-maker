# Quickstart Guide: Accounting Rules Management

**Feature**: 002-accounting-rules  
**Date**: 2026-01-31

---

## Prerequisites

- Docker and Docker Compose installed
- COA module (001-coa-management) running (provides account lookup)
- curl or HTTP client for API testing

## Quick Start with Docker

```bash
# From project root - starts all services including rules module
docker-compose up -d

# Wait for services to be healthy
docker-compose ps

# Check rules service health
curl http://localhost:8080/actuator/health
```

## API Base URL

- Local: `http://localhost:8080/api/v1/rules`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Common Operations

### 1. Create a Simple Rule

```bash
# Create a basic payment received rule
curl -X POST http://localhost:8080/api/v1/rules \
  -H "Content-Type: application/json" \
  -d '{
    "code": "RULE-001",
    "name": "Payment Received Rule",
    "description": "Generates entries when payment is received",
    "sharedAcrossScenarios": false,
    "entryTemplate": {
      "description": "Standard payment entry",
      "variableSchema": [
        {
          "name": "transaction.amount",
          "type": "MONEY",
          "currency": "USD",
          "description": "Payment amount"
        }
      ],
      "lines": [
        {
          "accountCode": "1110",
          "entryType": "DEBIT",
          "amountExpression": "transaction.amount",
          "memoTemplate": "Payment received"
        },
        {
          "accountCode": "4100",
          "entryType": "CREDIT",
          "amountExpression": "transaction.amount",
          "memoTemplate": "Revenue from payment"
        }
      ]
    },
    "triggerConditions": [
      {
        "conditionJson": {
          "type": "SIMPLE",
          "field": "event.type",
          "operator": "EQUALS",
          "value": "payment_received"
        },
        "description": "Triggers on payment received events"
      }
    ]
  }'
```

**Response** (201 Created):
```json
{
  "id": 1,
  "code": "RULE-001",
  "name": "Payment Received Rule",
  "status": "DRAFT",
  "currentVersion": 1,
  "version": 0,
  ...
}
```

### 2. List All Rules

```bash
# List all rules
curl http://localhost:8080/api/v1/rules

# Filter by status
curl "http://localhost:8080/api/v1/rules?status=DRAFT"

# Filter shared rules
curl "http://localhost:8080/api/v1/rules?shared=true"

# Search by name/code
curl "http://localhost:8080/api/v1/rules?search=payment"
```

### 3. Get Rule Details

```bash
curl http://localhost:8080/api/v1/rules/1
```

### 4. Update a Rule

```bash
# Update rule (must include version for optimistic locking)
curl -X PUT http://localhost:8080/api/v1/rules/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Payment Received Rule (Updated)",
    "description": "Updated description",
    "version": 0
  }'
```

**On version conflict** (409):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Version mismatch. Current version is 1, but you provided 0",
  "errorCode": "VERSION_CONFLICT"
}
```

### 5. Validate Expression Syntax

```bash
# Validate an amount expression
curl -X POST http://localhost:8080/api/v1/rules/validate-expression \
  -H "Content-Type: application/json" \
  -d '{
    "expression": "transaction.amount * 0.1",
    "variableSchema": [
      {
        "name": "transaction.amount",
        "type": "MONEY",
        "currency": "USD"
      }
    ]
  }'
```

**Response**:
```json
{
  "valid": true,
  "parsedType": "MONEY",
  "errors": [],
  "warnings": []
}
```

---

## Rule Lifecycle Operations

### 6. Activate a Rule (DRAFT → ACTIVE)

```bash
# Activates rule after validation
curl -X POST http://localhost:8080/api/v1/rules/1/activate
```

**On validation failure** (400):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Rule activation failed",
  "validationErrors": [
    {
      "field": "entryLines[0].accountCode",
      "message": "Account not found: 9999"
    },
    {
      "field": "entryLines[1].accountCode",
      "message": "Account has no Formance mapping: 4100"
    }
  ]
}
```

### 7. Archive a Rule

```bash
curl -X POST http://localhost:8080/api/v1/rules/1/archive
```

### 8. Restore an Archived Rule

```bash
# Restores to DRAFT status (creates new version)
curl -X POST http://localhost:8080/api/v1/rules/1/restore
```

### 9. Clone a Rule

```bash
curl -X POST http://localhost:8080/api/v1/rules/1/clone \
  -H "Content-Type: application/json" \
  -d '{
    "newCode": "RULE-002",
    "newName": "Payment Received Rule (Copy)"
  }'
```

---

## Version History Operations

### 10. List Rule Versions

```bash
curl http://localhost:8080/api/v1/rules/1/versions
```

**Response**:
```json
{
  "content": [
    {
      "versionNumber": 2,
      "changeDescription": "Updated entry template",
      "createdAt": "2026-01-31T09:00:00Z"
    },
    {
      "versionNumber": 1,
      "changeDescription": "Initial version",
      "createdAt": "2026-01-31T08:00:00Z"
    }
  ],
  "totalElements": 2
}
```

### 11. Get Specific Version

```bash
curl http://localhost:8080/api/v1/rules/1/versions/1
```

### 12. Rollback to Previous Version

```bash
# Rollback to version 1 (creates version 3)
curl -X POST http://localhost:8080/api/v1/rules/1/rollback/1
```

---

## Simulation & Generation

### 13. Simulate Rule Execution

```bash
# Test rule with sample event data
curl -X POST http://localhost:8080/api/v1/rules/1/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "eventData": {
      "type": "payment_received",
      "amount": 1000.00,
      "currency": "USD",
      "customer": "CUST-001"
    }
  }'
```

**Response** (rule fires):
```json
{
  "wouldFire": true,
  "entries": [
    {
      "accountCode": "1110",
      "accountName": "Cash",
      "entryType": "DEBIT",
      "amount": 1000.00,
      "currency": "USD",
      "memo": "Payment received"
    },
    {
      "accountCode": "4100",
      "accountName": "Sales Revenue",
      "entryType": "CREDIT",
      "amount": 1000.00,
      "currency": "USD",
      "memo": "Revenue from payment"
    }
  ],
  "totalDebits": 1000.00,
  "totalCredits": 1000.00,
  "isBalanced": true,
  "warnings": [],
  "errors": []
}
```

**Response** (rule doesn't fire):
```json
{
  "wouldFire": false,
  "reasonNotFired": "Trigger condition not met: event.type != 'payment_received'",
  "entries": [],
  "warnings": [],
  "errors": []
}
```

### 14. Generate Numscript DSL

```bash
curl -X POST http://localhost:8080/api/v1/rules/1/generate
```

**Response**:
```json
{
  "numscript": "vars {\n  monetary $amount = balance(@world, USD)\n}\n\nsend $amount (\n  source = @world\n  destination = @customer:CUST-001:cash\n)",
  "validationResult": {
    "valid": true,
    "errors": [],
    "warnings": []
  }
}
```

---

## Expression Syntax Reference

### Supported Operators
| Operator | Example |
|----------|---------|
| Addition | `a + b` |
| Subtraction | `a - b` |
| Multiplication | `a * b` |
| Division | `a / b` |
| Parentheses | `(a + b) * c` |

### Variable Types
| Type | Description | Example |
|------|-------------|---------|
| MONEY | Monetary value with currency | `transaction.amount` |
| DECIMAL | Numeric value | `transaction.tax_rate` |
| BOOLEAN | True/false | `transaction.is_refund` |
| STRING | Text value | `transaction.description` |

### Expression Examples
```
# Simple variable
transaction.amount

# Percentage calculation
transaction.amount * 0.1

# Complex calculation
(transaction.amount - transaction.discount) * (1 + transaction.tax_rate)
```

---

## Trigger Condition Syntax

### Simple Condition
```json
{
  "type": "SIMPLE",
  "field": "event.type",
  "operator": "EQUALS",
  "value": "payment_received"
}
```

### Combined Conditions (AND)
```json
{
  "type": "AND",
  "conditions": [
    { "type": "SIMPLE", "field": "event.type", "operator": "EQUALS", "value": "payment" },
    { "type": "SIMPLE", "field": "event.amount", "operator": "GREATER_THAN", "value": 1000 }
  ]
}
```

### Combined Conditions (OR)
```json
{
  "type": "OR",
  "conditions": [
    { "type": "SIMPLE", "field": "event.priority", "operator": "EQUALS", "value": "high" },
    { "type": "SIMPLE", "field": "event.amount", "operator": "GREATER_THAN", "value": 10000 }
  ]
}
```

### Supported Operators
| Operator | Description |
|----------|-------------|
| EQUALS | Exact match |
| NOT_EQUALS | Not equal |
| GREATER_THAN | > |
| GREATER_THAN_OR_EQUALS | >= |
| LESS_THAN | < |
| LESS_THAN_OR_EQUALS | <= |
| CONTAINS | Substring match |
| MATCHES | Regex pattern |
| IN | Value in list |
| NOT_IN | Value not in list |

---

## Error Handling

### Common Error Codes

| Code | Status | Description |
|------|--------|-------------|
| RULE_NOT_FOUND | 404 | Rule ID does not exist |
| VERSION_CONFLICT | 409 | Optimistic locking failure |
| INVALID_STATE_TRANSITION | 409 | Cannot perform operation in current status |
| EXPRESSION_PARSE_ERROR | 400 | Invalid expression syntax |
| ACCOUNT_NOT_FOUND | 400 | Referenced account doesn't exist |
| ACCOUNT_NOT_MAPPED | 400 | Account has no Formance mapping |
| VALIDATION_FAILED | 400 | Rule validation failed |

### Error Response Format
```json
{
  "timestamp": "2026-01-31T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Expression validation failed",
  "errorCode": "EXPRESSION_PARSE_ERROR",
  "details": {
    "expression": "amount * ",
    "position": 9,
    "expected": "operand"
  }
}
```

---

## Integration with COA Module

The rules module depends on the COA module (001-coa-management) for:

1. **Account Validation**: Entry line account codes must exist in COA
2. **Formance Mapping**: Accounts must have Formance paths for Numscript generation
3. **Reference Tracking**: Rules create references to accounts (prevents account deletion)

### Ensure Accounts Exist

```bash
# Create accounts in COA before creating rules
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code": "1110", "name": "Cash"}'

curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code": "4100", "name": "Sales Revenue"}'

# Create Formance mappings
curl -X POST http://localhost:8080/api/v1/accounts/mappings \
  -H "Content-Type: application/json" \
  -d '{"accountCode": "1110", "formanceLedgerAccount": "@bank:main:cash"}'
```

---

## Troubleshooting

### Rule Won't Activate

1. Check all account codes exist: `GET /api/v1/accounts/{code}`
2. Check accounts have Formance mappings: `GET /api/v1/accounts/mappings/{code}`
3. Validate expressions: `POST /api/v1/rules/validate-expression`

### Version Conflict on Update

1. Refresh rule: `GET /api/v1/rules/{id}`
2. Use returned `version` value in update request
3. Retry update

### Simulation Shows Unexpected Results

1. Check trigger conditions match event data
2. Verify variable schema types match event data types
3. Check expression syntax with validate endpoint

---

## Next Steps

- [Full API Reference](./contracts/accounting-rules-api.yaml)
- [Data Model Documentation](./data-model.md)
- [Technical Research](./research.md)

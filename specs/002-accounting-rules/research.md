# Technical Research: Accounting Rules Management

**Feature**: 002-accounting-rules  
**Date**: 2026-01-31  
**Status**: Complete

---

## 1. Expression Language Design

### Decision
Implement a **strictly-typed expression parser** with schema-defined variables and compile-time type checking.

### Rationale
- Financial calculations require precision; runtime type errors could cause incorrect journal entries
- Schema-defined variables enable static validation before rule activation
- Aligns with clarification decision: "Strict types with schema-defined variables"

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| Dynamic typing (JavaScript-like) | Runtime errors in production; harder to validate |
| Money-only types | Too restrictive for rate calculations and percentages |
| Full expression language (SpEL, JEXL) | Overkill for arithmetic; security concerns with code injection |

### Implementation Approach

```java
// Type system
enum ExpressionType { DECIMAL, MONEY, BOOLEAN, STRING }

// Variable schema per rule
class VariableSchema {
    String name;           // e.g., "transaction.amount"
    ExpressionType type;   // e.g., MONEY
    String currency;       // required if type == MONEY
}

// Expression AST
interface Expression {
    ExpressionType getType();
    Object evaluate(Map<String, Object> context);
}
```

**Parser**: Use recursive descent parser or ANTLR4 grammar for expressions.
**Validation**: Type-check at parse time, reject mismatched operations (e.g., MONEY + BOOLEAN).

---

## 2. Numscript DSL Generation

### Decision
Implement a **template-based generator** with AST transformation for expressions.

### Rationale
- Numscript has specific syntax for monetary operations (`send`, `source`, `destination`)
- Account mappings from COA module provide path translation
- Expression-to-Numscript translation requires understanding of monetary semantics

### Numscript Syntax Reference

```numscript
// Basic send operation
send [monetary-amount] (
  source = @account:path
  destination = @account:path
)

// Conditional
if [condition] {
  send ...
}

// Variables
vars {
  monetary $amount
  account $source
}
```

### Implementation Approach

```java
interface OutputGenerator {
    String generate(AccountingRule rule);
    ValidationResult validate(String output);
}

class NumscriptGenerator implements OutputGenerator {
    // 1. Generate vars block from rule's variable schema
    // 2. Generate conditional block from trigger conditions
    // 3. Generate send operations from entry template lines
    // 4. Map COA codes to Formance paths via AccountMappingService
}
```

### COA Integration
- Use `AccountMappingService.getFormancePath(String coaCode)` from 001-coa-management
- Validate all referenced accounts have mappings before generation
- Error if unmapped accounts found

---

## 3. Rule Versioning Strategy

### Decision
Store **complete snapshots** per version with unlimited retention.

### Rationale
- Simplifies rollback (just load previous snapshot)
- Aligns with clarification: "Unlimited versions, no automatic purge"
- Audit requirements often need historical rule state

### Alternatives Considered

| Alternative | Rejected Because |
|-------------|------------------|
| Diff-based versioning | Complex merge logic; harder to reconstruct point-in-time |
| Limited retention | Compliance may require full history |
| Soft delete only | Doesn't capture intermediate edits |

### Implementation Approach

```java
@Entity
class AccountingRuleVersion {
    Long id;
    Long ruleId;              // Parent rule
    Integer versionNumber;    // 1, 2, 3...
    String snapshotJson;      // Full rule state as JSON
    String changeDescription; // User-provided or auto-generated
    LocalDateTime createdAt;
    String createdBy;
}
```

**Storage**: JSON serialization of rule state (entry template, trigger conditions, metadata).
**Rollback**: Create new version from historical snapshot.

---

## 4. Trigger Condition Evaluation

### Decision
Store conditions as **JSON AST**; provide in-memory evaluator for simulation.

### Rationale
- Conditions are defined here but evaluated by consuming systems
- Simulation requires local evaluation capability
- JSON storage enables flexible condition structures

### Condition Model

```json
{
  "type": "AND",
  "conditions": [
    { "field": "event.type", "operator": "EQUALS", "value": "payment_received" },
    { "field": "event.amount", "operator": "GREATER_THAN", "value": 10000 }
  ]
}
```

### Supported Operators

| Operator | Applies To | Example |
|----------|-----------|---------|
| EQUALS | All types | `status == "active"` |
| NOT_EQUALS | All types | `status != "pending"` |
| GREATER_THAN | Numeric | `amount > 1000` |
| LESS_THAN | Numeric | `amount < 500` |
| CONTAINS | String | `name contains "Corp"` |
| MATCHES | String | `code matches "^[A-Z]{3}$"` |
| IN | All types | `type in ["A", "B", "C"]` |

### Implementation

```java
interface ConditionEvaluator {
    boolean evaluate(TriggerCondition condition, Map<String, Object> eventData);
}
```

---

## 5. State Machine for Rule Lifecycle

### Decision
Implement **validation-gated transitions** with explicit state enum.

### Rationale
- Aligns with clarification: "draft → active requires validation pass"
- Prevents deployment of invalid rules
- Archived rules can be restored to draft for re-editing

### State Transition Diagram

```
     ┌─────────────────────────────────────┐
     │                                     │
     ▼                                     │
  ┌──────┐  validate()  ┌────────┐  archive()  ┌──────────┐
  │ DRAFT│─────────────►│ ACTIVE │────────────►│ ARCHIVED │
  └──────┘              └────────┘             └──────────┘
     ▲                      │                       │
     │                      │ archive()             │
     │                      ▼                       │
     │               ┌──────────┐                   │
     └───────────────│ ARCHIVED │◄──────────────────┘
       restore()     └──────────┘    (same state)
```

### Transition Rules

| From | To | Condition |
|------|----|-----------|
| DRAFT | ACTIVE | All validations pass (syntax, accounts, expressions) |
| ACTIVE | ARCHIVED | None (always allowed) |
| ARCHIVED | DRAFT | None (always allowed, creates new version) |
| DRAFT | ARCHIVED | None (skip activation) |

### Validation Checks for Activation
1. Expression syntax valid
2. All variables defined in schema
3. All account references exist in COA
4. At least one entry line exists
5. All COA accounts have Formance mappings (if Numscript output needed)

---

## 6. Simulation Engine

### Decision
Implement **stateless simulation** that evaluates rules against sample data.

### Rationale
- Users need to verify rules before activation
- No side effects (no actual ledger operations)
- Shows calculated amounts and resolved account names

### Simulation Flow

```
Input: Rule + Sample Event Data
  │
  ├─► Evaluate Trigger Conditions
  │     └─► If false: Return "Rule would not fire"
  │
  ├─► Parse and Evaluate Amount Expressions
  │     └─► On error: Return expression errors
  │
  ├─► Resolve Account References
  │     └─► Look up names from COA
  │
  └─► Build SimulationResult
        ├─► List of entry lines with resolved values
        ├─► Debit/Credit totals
        └─► Balance check (warning if unbalanced)
```

### Result Model

```java
class SimulationResult {
    boolean wouldFire;
    String reasonNotFired;  // if wouldFire == false
    List<SimulatedEntry> entries;
    BigDecimal totalDebits;
    BigDecimal totalCredits;
    boolean isBalanced;
    List<String> warnings;
    List<String> errors;
}

class SimulatedEntry {
    String accountCode;
    String accountName;  // resolved from COA
    EntryType type;      // DEBIT or CREDIT
    BigDecimal amount;
    String currency;
    String memo;
}
```

---

## 7. Integration with COA Module

### Decision
Use **service-layer integration** with clear API boundaries.

### Rationale
- COA module already provides account lookup and Formance mappings
- Loose coupling via interfaces enables independent testing
- Shared database simplifies transactions

### Integration Points

| COA Service | Usage in Rules Module |
|-------------|----------------------|
| `AccountService.getAccount(code)` | Validate account references exist |
| `AccountService.getAccountReferences(code)` | Track rule references to accounts |
| `AccountMappingService.getMapping(code)` | Get Formance path for Numscript generation |
| `AccountService.createReference(...)` | Register rule as account consumer |

### Reference Tracking
When a rule references an account, create an `AccountReference` with:
- `referenceType = RULE`
- `referenceSourceId = rule.id`
- `accountCode = referenced account code`

This enables:
- Preventing deletion of accounts used in rules
- Impact analysis when modifying shared accounts

---

## 8. Concurrency Control

### Decision
Use **optimistic locking with version field** and reject-on-conflict.

### Rationale
- Aligns with clarification: "Reject with error, require manual refresh"
- Simpler than merge-based approaches
- Consistent with COA module pattern

### Implementation

```java
@Entity
class AccountingRule {
    @Version
    private Long version;
    
    // Other fields...
}
```

**API Behavior**:
- PUT requests must include `version` in request body
- On version mismatch: HTTP 409 Conflict with current version in response
- Client must refresh and reapply changes

---

## 9. API Design Patterns

### Decision
Follow **RESTful conventions** consistent with COA module.

### Endpoint Structure

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/rules` | Create rule (draft) |
| GET | `/api/v1/rules` | List rules (paginated, filterable) |
| GET | `/api/v1/rules/{id}` | Get rule by ID |
| PUT | `/api/v1/rules/{id}` | Update rule |
| DELETE | `/api/v1/rules/{id}` | Delete rule |
| POST | `/api/v1/rules/{id}/activate` | Activate rule (draft → active) |
| POST | `/api/v1/rules/{id}/archive` | Archive rule |
| POST | `/api/v1/rules/{id}/restore` | Restore to draft |
| POST | `/api/v1/rules/{id}/clone` | Clone rule |
| GET | `/api/v1/rules/{id}/versions` | List version history |
| GET | `/api/v1/rules/{id}/versions/{versionNum}` | Get specific version |
| POST | `/api/v1/rules/{id}/rollback/{versionNum}` | Rollback to version |
| POST | `/api/v1/rules/{id}/simulate` | Simulate with sample data |
| POST | `/api/v1/rules/{id}/generate` | Generate Numscript |
| POST | `/api/v1/rules/validate-expression` | Validate expression syntax |

---

## 10. Error Handling Strategy

### Decision
Use **domain-specific exceptions** with structured error responses.

### Exception Hierarchy

```java
class RulesException extends RuntimeException { }
class RuleNotFoundException extends RulesException { }
class RuleValidationException extends RulesException { }
class ExpressionParseException extends RulesException { }
class InvalidStateTransitionException extends RulesException { }
class AccountNotMappedException extends RulesException { }
class SimulationException extends RulesException { }
```

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

## Summary

All technical decisions are aligned with:
- Constitution principles (design assistant, Numscript output, OpenAPI-first)
- Clarification answers (strict typing, unlimited versions, optimistic locking, no RBAC)
- Existing COA module patterns (Spring Boot, JPA, PostgreSQL)

**No unresolved NEEDS CLARIFICATION items remain.**

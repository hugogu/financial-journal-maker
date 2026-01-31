# Research: Product/Scenario/TransactionType Module

**Feature**: 003-product-scenario-types  
**Date**: 2026-01-31

## Technical Context Decisions

### 1. State Management Pattern

**Decision**: Reuse existing `RuleStatus` enum pattern (DRAFT → ACTIVE → ARCHIVED)

**Rationale**: 
- Consistent with AccountingRule module already implemented
- Provides familiar state transitions for users
- Simplifies codebase maintenance

**Alternatives Considered**:
- Custom state enum per entity → Rejected: Adds complexity without benefit
- No state management → Rejected: Clarification confirmed state is required

### 2. Hierarchical Data Modeling

**Decision**: Use JPA `@ManyToOne` parent references with cascade constraints

**Rationale**:
- Product → Scenario → TransactionType is fixed 3-level hierarchy
- Parent-child relationship is clear and doesn't need polymorphism
- JPA handles foreign key constraints naturally

**Alternatives Considered**:
- Closure table for arbitrary depth → Rejected: Fixed 3-level, overkill
- Nested set model → Rejected: Complex updates, not needed for 3 levels
- Adjacency list with recursive CTE → Acceptable but JPA parent reference is simpler

### 3. Many-to-Many Rule Association

**Decision**: Use explicit join entity `TransactionTypeRule` with additional metadata

**Rationale**:
- Allows tracking when association was created
- Supports ordering of rules within a transaction type
- Enables audit trail for rule associations

**Alternatives Considered**:
- Simple `@ManyToMany` annotation → Rejected: No room for metadata
- JSON array of rule IDs → Rejected: Loses referential integrity

### 4. Business Description Storage

**Decision**: Use PostgreSQL TEXT type with Markdown content

**Rationale**:
- Clarification confirmed Markdown format
- TEXT type supports unlimited length
- No need for separate rich text storage

**Alternatives Considered**:
- VARCHAR with length limit → Rejected: May truncate descriptions
- Separate document store → Rejected: Overkill for text content

### 5. Copy/Clone Implementation

**Decision**: Deep copy with `-copy-N` suffix, editable before save

**Rationale**:
- Clarification confirmed auto-suffix with user edit capability
- Copy structure but not rule associations (per spec)
- User has control over final code values

**Alternatives Considered**:
- UUID-based cloning → Rejected: Loses meaningful naming
- Shallow copy → Rejected: Need to copy child entities too

### 6. Rule Association Validation

**Decision**: Allow DRAFT and ACTIVE rules, reject ARCHIVED

**Rationale**:
- Clarification Q5 answer: C
- DRAFT rules may be in design phase, allowing association supports workflow
- ARCHIVED rules should not be newly associated

**Alternatives Considered**:
- Only ACTIVE → Rejected: Too restrictive during design
- Any status → Rejected: ARCHIVED should be excluded

## Integration Points

### AccountingRule Module

**Integration Pattern**: Reference by ID via join table

**API Dependencies**:
- `GET /api/v1/rules` - List available rules for association
- `GET /api/v1/rules/{id}` - Get rule details for display

**Data Dependencies**:
- `accounting_rules` table must exist
- `RuleStatus` enum shared

### COA Module (Chart of Accounts)

**Integration Pattern**: Query accounts used in associated rules

**API Dependencies**:
- Rules contain account references in entry templates
- Aggregate account queries traverse rule associations

## Performance Considerations

### Tree Queries

**Approach**: Eager fetch for small trees, lazy + batch for large trees

**Implementation**:
- Default: Fetch product with scenarios eagerly
- Large trees (>100 scenarios): Paginate scenarios, lazy load types
- Use `@EntityGraph` for controlled fetching

### Association Queries

**Approach**: Indexed foreign keys, batch fetching

**Implementation**:
- Index on `scenario_id` in `transaction_types`
- Index on `product_id` in `scenarios`
- Index on `transaction_type_id` in `transaction_type_rules`

## Security Considerations

**Authentication**: Inherited from existing Spring Security setup  
**Authorization**: All users can CRUD (no multi-tenancy per Out of Scope)  
**Input Validation**: Standard Bean Validation on DTOs

# Technical Research: Chart of Accounts Management

**Feature**: Chart of Accounts Management  
**Phase**: 0 - Outline & Research  
**Date**: 2026-01-28

## Research Areas

This document consolidates technical decisions, best practices, and patterns for implementing the COA management module.

---

## 1. Hierarchical Data Storage in PostgreSQL

### Decision: Adjacency List with Recursive CTEs

**Rationale**:
- Simple schema: `parent_id` foreign key to self-reference
- Excellent write performance for CRUD operations
- PostgreSQL's WITH RECURSIVE queries handle tree traversal efficiently
- JPA/Hibernate native support via `@ManyToOne` self-reference
- Meets performance requirements (<500ms for 1000-node trees)

**Implementation approach**:
```sql
-- Adjacency list structure
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES accounts(id),
    ...
);

-- Recursive query for tree retrieval
WITH RECURSIVE account_tree AS (
    SELECT * FROM accounts WHERE parent_id IS NULL  -- roots
    UNION ALL
    SELECT a.* FROM accounts a
    INNER JOIN account_tree at ON a.parent_id = at.id
)
SELECT * FROM account_tree;
```

**Alternatives considered**:
1. **Nested Sets**: Rejected - complex updates, poor concurrency, overkill for our scale
2. **Materialized Path**: Rejected - requires custom path maintenance, limited PostgreSQL native support
3. **Closure Table**: Rejected - additional table complexity, unnecessary for single-hierarchy use case

**Best practices**:
- Index on `parent_id` for join performance
- Index on `code` for lookup performance (already unique constraint)
- Use `@OnDelete(action = OnDeleteAction.RESTRICT)` in JPA to prevent orphaning
- Implement tree validation in service layer (cycle detection during imports)

---

## 2. File Import Strategy (Excel/CSV)

### Decision: Apache POI (Excel) + OpenCSV (CSV) with Streaming

**Rationale**:
- Apache POI: Industry standard, handles complex Excel formats (.xlsx, .xls)
- OpenCSV: Lightweight, fast for large CSV files
- Streaming approach prevents memory overflow on large imports
- Both libraries have Spring Boot integration

**Implementation approach**:
```java
// Excel parsing with POI streaming API
try (InputStream is = file.getInputStream();
     Workbook workbook = WorkbookFactory.create(is)) {
    Sheet sheet = workbook.getSheetAt(0);
    Iterator<Row> rowIterator = sheet.iterator();
    // Process row by row
}

// CSV parsing with OpenCSV
try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
        .withSkipLines(1).build()) {
    String[] line;
    while ((line = reader.readNext()) != null) {
        // Process line by line
    }
}
```

**Validation strategy** (pre-import):
1. Parse entire file into memory model (for files <10k rows)
2. Validate structure: required columns, duplicate codes, circular refs
3. Only persist after full validation passes
4. Return detailed error report on failure

**Error handling**:
- Wrap parsing exceptions with `InvalidImportFileException`
- Provide line number and specific error message
- Support dry-run mode (validate without persisting)

**Alternatives considered**:
1. **EasyExcel (Alibaba)**: Rejected - less mature for Western enterprise environments
2. **JExcelApi**: Rejected - older library, limited .xlsx support
3. **Custom parsers**: Rejected - reinventing the wheel

**Best practices**:
- Limit file size to 10MB (configurable)
- Use `@Transactional` with proper rollback on validation failure
- Implement `ImportJob` entity to track import history
- Consider background processing for very large files (future enhancement)

---

## 3. Concurrency Control

### Decision: Optimistic Locking with @Version

**Rationale**:
- Chart of accounts changes are infrequent (design-time activity)
- Multiple users rarely edit same account simultaneously
- Optimistic locking provides better read performance
- JPA native support via `@Version` annotation
- Simpler than pessimistic locking

**Implementation approach**:
```java
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;
    
    // Other fields...
}
```

**Conflict handling**:
- Spring Data throws `OptimisticLockingFailureException` on version mismatch
- API returns 409 Conflict with retry instruction
- Frontend/client responsible for refetching and retrying

**Alternatives considered**:
1. **Pessimistic locking**: Rejected - reduces concurrency, overkill for low-contention scenario
2. **No locking**: Rejected - violates requirement for zero data loss in concurrent ops
3. **Database-level locks**: Rejected - application-level control preferred

**Best practices**:
- Include version in API responses for client-side optimistic locking
- Document retry expectations in OpenAPI spec
- Add integration tests for concurrent updates

---

## 4. Reference Tracking & Immutability

### Decision: Separate Reference Table + Check Constraint

**Rationale**:
- Explicit tracking enables "list all references" queries
- Database constraint prevents orphaned references
- Supports multiple reference types (rules, scenarios)
- Decouples COA module from other modules initially

**Implementation approach**:
```sql
CREATE TABLE account_references (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL REFERENCES accounts(code),
    reference_source_id VARCHAR(255) NOT NULL,  -- rule_id or scenario_id
    reference_type VARCHAR(50) NOT NULL,        -- 'RULE' or 'SCENARIO'
    created_at TIMESTAMP NOT NULL
);

-- Application-level check: before update/delete, query this table
SELECT COUNT(*) FROM account_references WHERE account_code = ?;
```

**Service layer logic**:
```java
public void updateAccountCode(String oldCode, String newCode) {
    if (accountReferenceRepository.existsByAccountCode(oldCode)) {
        throw new AccountReferencedException(
            "Cannot modify account code - referenced by rules/scenarios");
    }
    // Proceed with update
}
```

**Alternatives considered**:
1. **Flag on Account entity**: Rejected - doesn't track which references exist
2. **Foreign key from other tables**: Rejected - tight coupling, circular dependency
3. **Event-driven counting**: Rejected - eventual consistency risk

**Best practices**:
- Index `account_code` for fast reference lookups
- Provide endpoint to query references: `GET /accounts/{code}/references`
- Document reference contract in OpenAPI for other modules
- Future: implement event-driven sync when rule/scenario modules created

---

## 5. API Design Patterns

### Decision: RESTful with Spring Data REST Principles

**Rationale**:
- Follows REST conventions: POST (create), GET (read), PUT (update), DELETE (delete)
- Spring Boot conventions for error handling and validation
- OpenAPI 3.0 for contract-first development
- Pagination via Spring Data's Pageable

**Key endpoints**:

| Method | Path | Purpose |
|--------|------|---------|
| POST | /accounts | Create account |
| GET | /accounts/{code} | Get account by code |
| GET | /accounts | List accounts (paginated) |
| GET | /accounts/tree | Get full tree structure |
| PUT | /accounts/{code} | Update account (name/desc only) |
| DELETE | /accounts/{code} | Delete account (if unreferenced) |
| POST | /accounts/mappings | Create ledger mapping |
| GET | /accounts/mappings/{code} | Get mapping for account |
| PUT | /accounts/mappings/{code} | Update mapping |
| POST | /accounts/import | Upload Excel/CSV file |
| GET | /accounts/import/{jobId} | Get import job status |
| GET | /accounts/{code}/references | List references (why immutable) |

**Error response format**:
```json
{
  "timestamp": "2026-01-28T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Duplicate account code: 1000",
  "path": "/accounts",
  "errorCode": "DUPLICATE_ACCOUNT_CODE"
}
```

**Alternatives considered**:
1. **GraphQL**: Rejected - REST sufficient for CRUD operations, GraphQL adds complexity
2. **gRPC**: Rejected - HTTP/JSON required for broader tool compatibility
3. **JSON-RPC**: Rejected - REST is industry standard

**Best practices**:
- Use `@RestControllerAdvice` for global exception handling
- Return 201 Created with Location header for POST
- Return 204 No Content for successful DELETE
- Use HTTP 409 Conflict for business rule violations
- Include request correlation IDs in responses for debugging

---

## 6. Database Schema Design

### Decision: Three Core Tables + Flyway Migrations

**Tables**:

1. **accounts**: Core account data
   - Primary key: `id` (BIGSERIAL)
   - Unique constraint: `code`
   - Self-reference: `parent_id`
   - Soft delete: NOT implementing initially (explicit requirement)
   - Audit columns: `created_at`, `updated_at`, `created_by`

2. **account_mappings**: Ledger mappings
   - Primary key: `id` (BIGSERIAL)
   - Foreign key: `account_code` → accounts.code
   - Target: `formance_ledger_account` (VARCHAR)
   - One-to-one relationship per account

3. **account_references**: Reference tracking
   - Primary key: `id` (BIGSERIAL)
   - Foreign key: `account_code` → accounts.code
   - Denormalized fields: `reference_source_id`, `reference_type`

4. **import_jobs**: Import history (optional for v1)
   - Primary key: `id` (BIGSERIAL)
   - Metadata: filename, status, timestamps, error details

**Rationale**:
- Normalized design prevents data duplication
- Flyway ensures reproducible schema changes
- Cascade rules handled in application layer (explicit control)

**Best practices**:
- Use Flyway versioned migrations: `V1__create_coa_tables.sql`
- Never modify existing migrations; create new ones for changes
- Include rollback scripts in comments
- Use `ON DELETE RESTRICT` for foreign keys to prevent accidental data loss

---

## 7. Testing Strategy

### Decision: 4-Layer Testing Pyramid

**Layers**:

1. **Unit tests** (60%): Service logic, validation, parsing
   - JUnit 5 + Mockito
   - Test business rules in isolation
   - Example: `AccountValidationServiceTest`, `FileParserServiceTest`

2. **Repository tests** (20%): Data access
   - `@DataJpaTest` with H2 in-memory DB
   - Test recursive queries, constraint validation
   - Example: `AccountRepositoryTest` (test tree queries)

3. **Integration tests** (15%): Full API flow
   - `@SpringBootTest` with Testcontainers PostgreSQL
   - Test end-to-end scenarios from spec
   - Example: `AccountIntegrationTest`, `ImportIntegrationTest`

4. **Contract tests** (5%): OpenAPI compliance
   - Spring Cloud Contract or RestAssured
   - Verify API responses match OpenAPI spec
   - Example: `CoaApiContractTest`

**Specific test scenarios** (from spec):
- User Story 1: Create hierarchy, duplicate code rejection
- User Story 2: Mapping CRUD operations
- User Story 3: Valid/invalid file imports
- User Story 4: Cross-scenario validation
- User Story 5: Reference protection

**Best practices**:
- Use Testcontainers for real PostgreSQL in CI/CD
- Test concurrent operations with `CompletableFuture`
- Include performance assertions: `assertTimeout(Duration.ofMillis(200), ...)`
- Generate code coverage reports (target: >80%)

---

## 8. Performance Optimization

### Decision: Strategic Indexing + Caching Considerations

**Database indexes**:
```sql
CREATE INDEX idx_accounts_parent ON accounts(parent_id);
CREATE INDEX idx_accounts_code ON accounts(code);  -- implicit via UNIQUE
CREATE INDEX idx_account_refs_code ON account_references(account_code);
CREATE INDEX idx_account_mappings_code ON account_mappings(account_code);
```

**Query optimization**:
- Use `JOIN FETCH` in JPA for parent-child relationships
- Implement DTO projections for API responses (avoid entity exposure)
- Batch insert during import (JDBC batch size: 50)

**Caching strategy** (future consideration):
- NOT implementing caching in v1 (premature optimization)
- If needed later: Spring Cache + Redis for tree structure
- Cache invalidation on any account modification

**Monitoring**:
- Spring Actuator metrics endpoint
- Log slow queries (>500ms) at WARN level
- Include response time in API logs

**Alternatives considered**:
1. **Eager loading**: Rejected - causes N+1 queries
2. **Second-level cache**: Rejected - complexity not justified initially
3. **Read replicas**: Rejected - single DB sufficient for design-time workload

**Best practices**:
- Use connection pooling (HikariCP - Spring Boot default)
- Configure reasonable timeout values
- Profile with JMeter/Gatling before production

---

## 9. Error Handling & Validation

### Decision: Bean Validation + Custom Exceptions

**Validation approach**:
```java
public class AccountDto {
    @NotBlank(message = "Account code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9.-]+$", message = "Code contains invalid characters")
    private String code;
    
    @NotBlank(message = "Account name is required")
    @Size(max = 255)
    private String name;
}
```

**Custom exception hierarchy**:
- `CoaException` (base)
  - `DuplicateAccountCodeException` → 409 Conflict
  - `AccountReferencedException` → 409 Conflict
  - `AccountNotFoundException` → 404 Not Found
  - `InvalidImportFileException` → 400 Bad Request
  - `CircularReferenceException` → 400 Bad Request

**Error response builder**:
- Use `@RestControllerAdvice` for global handling
- Map exception types to HTTP status codes
- Include error codes for programmatic handling
- Sanitize error messages (no stack traces in production)

**Best practices**:
- Validate at controller layer with `@Valid`
- Throw domain exceptions in service layer
- Return detailed validation errors for import files
- Log errors with context for debugging

---

## 10. OpenAPI Specification Best Practices

### Decision: Contract-First with SpringDoc

**Approach**:
1. Define OpenAPI spec manually in YAML
2. Place in `specs/001-coa-management/contracts/coa-api.yaml`
3. Symlink to `backend/src/main/resources/openapi/`
4. Use SpringDoc annotations to keep code in sync
5. Auto-generate Swagger UI at `/swagger-ui.html`

**Key spec elements**:
- All endpoints documented with examples
- Error responses defined with schemas
- Security schemes (placeholder for future auth)
- Rate limiting headers (future consideration)
- Deprecation warnings for breaking changes

**Versioning strategy**:
- API version in path: `/api/v1/accounts`
- Semantic versioning for major changes
- Maintain backward compatibility within major version

**Best practices**:
- Use `$ref` for reusable schemas
- Include `operationId` for client generation
- Document rate limits and pagination
- Provide request/response examples

---

## Technology Decisions Summary

| Area | Decision | Key Libraries/Tools |
|------|----------|---------------------|
| Language | Java 21 | - |
| Framework | Spring Boot 3.x | Spring Web, Spring Data JPA |
| Database | PostgreSQL 15+ | Flyway for migrations |
| Hierarchical Data | Adjacency List + Recursive CTE | Native PostgreSQL |
| Excel Import | Apache POI 5.x | Streaming API |
| CSV Import | OpenCSV 5.x | - |
| API Documentation | OpenAPI 3.0 | SpringDoc OpenAPI |
| Testing | JUnit 5, Testcontainers | Mockito, AssertJ |
| Concurrency | Optimistic Locking | JPA @Version |
| Build Tool | Maven or Gradle | Spring Boot plugin |
| Containerization | Docker | Multi-stage builds |

---

## Next Steps

Phase 0 research complete. Ready to proceed to **Phase 1**:
- Generate `data-model.md` with detailed entity definitions
- Generate `contracts/coa-api.yaml` with OpenAPI specification
- Generate `quickstart.md` with setup and usage instructions
- Update agent context with technology stack

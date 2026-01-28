# Quickstart Guide: Chart of Accounts Management

**Feature**: Chart of Accounts Management  
**Date**: 2026-01-28  
**API Version**: v1.0.0

This guide helps developers set up and use the Chart of Accounts Management API.

---

## Prerequisites

- **Java 21** installed
- **Docker** and **docker-compose** installed
- **Maven** or **Gradle** (for building)
- **PostgreSQL client** (optional, for database inspection)
- **curl** or **Postman** (for API testing)

---

## Quick Start (Docker)

### 1. Start the services

```bash
# From repository root
docker-compose up -d

# Check logs
docker-compose logs -f coa-service
```

### 2. Verify the API is running

```bash
# Health check
curl http://localhost:8080/actuator/health

# OpenAPI documentation
open http://localhost:8080/swagger-ui.html
```

### 3. Create your first account

```bash
# Create root account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "code": "1000",
    "name": "Assets",
    "description": "All company assets",
    "parentCode": null,
    "sharedAcrossScenarios": true
  }'

# Response:
# {
#   "id": 1,
#   "code": "1000",
#   "name": "Assets",
#   "description": "All company assets",
#   "parentCode": null,
#   "hasChildren": false,
#   "isReferenced": false,
#   "referenceCount": 0,
#   "sharedAcrossScenarios": true,
#   "version": 1,
#   "createdAt": "2026-01-28T10:00:00Z",
#   "updatedAt": "2026-01-28T10:00:00Z"
# }
```

---

## Local Development Setup

### 1. Start PostgreSQL

```bash
# Using docker-compose (recommended)
docker-compose up -d postgres

# Or use local PostgreSQL
# Ensure database 'coa_db' exists
```

### 2. Configure application

Create `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/coa_db
    username: coa_user
    password: coa_password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

logging:
  level:
    com.financial.coa: DEBUG
    org.hibernate.SQL: DEBUG
```

### 3. Run the application

```bash
# Using Maven
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Using Gradle
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# Using IDE
# Run main class: com.financial.coa.CoaApplication
# Active profile: local
```

### 4. Run database migrations

Migrations run automatically on startup via Flyway.

To view migration status:
```bash
./mvnw flyway:info
```

---

## Common Operations

### Account Management

#### Create hierarchical accounts

```bash
# Root account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code": "1000", "name": "Assets", "parentCode": null}'

# Child account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code": "1100", "name": "Current Assets", "parentCode": "1000"}'

# Grandchild account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code": "1110", "name": "Cash", "parentCode": "1100"}'
```

#### Retrieve account tree

```bash
# Full tree
curl http://localhost:8080/api/v1/accounts/tree

# Subtree from specific root
curl http://localhost:8080/api/v1/accounts/tree?rootCode=1000
```

#### Update account

```bash
# Get current version first
ACCOUNT=$(curl -s http://localhost:8080/api/v1/accounts/1000)
VERSION=$(echo $ACCOUNT | jq -r '.version')

# Update with version for optimistic locking
curl -X PUT http://localhost:8080/api/v1/accounts/1000 \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Total Assets\",
    \"description\": \"Updated description\",
    \"version\": $VERSION
  }"
```

#### Delete account

```bash
# Only works if no children and no references
curl -X DELETE http://localhost:8080/api/v1/accounts/1110

# If account has children or references, returns 409 Conflict
```

### Formance Ledger Mappings

#### Create mapping

```bash
curl -X POST http://localhost:8080/api/v1/accounts/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "accountCode": "1110",
    "formanceLedgerAccount": "assets:current:cash"
  }'
```

#### Retrieve mapping

```bash
curl http://localhost:8080/api/v1/accounts/mappings/1110
```

#### Update mapping

```bash
# Get current version
MAPPING=$(curl -s http://localhost:8080/api/v1/accounts/mappings/1110)
VERSION=$(echo $MAPPING | jq -r '.version')

curl -X PUT http://localhost:8080/api/v1/accounts/mappings/1110 \
  -H "Content-Type: application/json" \
  -d "{
    \"formanceLedgerAccount\": \"assets:bank:checking\",
    \"version\": $VERSION
  }"
```

### File Import

#### Import from Excel

```bash
curl -X POST http://localhost:8080/api/v1/accounts/import \
  -F "file=@chart_of_accounts.xlsx" \
  -F "validateOnly=false"

# Response:
# {
#   "id": 1,
#   "fileName": "chart_of_accounts.xlsx",
#   "fileFormat": "EXCEL",
#   "status": "PROCESSING",
#   "totalRecords": 0,
#   "processedRecords": 0,
#   "failedRecords": 0,
#   "createdAt": "2026-01-28T10:00:00Z"
# }
```

#### Check import status

```bash
curl http://localhost:8080/api/v1/accounts/import/1

# Completed response:
# {
#   "id": 1,
#   "status": "COMPLETED",
#   "totalRecords": 500,
#   "processedRecords": 500,
#   "failedRecords": 0,
#   "completedAt": "2026-01-28T10:00:05Z"
# }
```

#### Validate without importing

```bash
curl -X POST http://localhost:8080/api/v1/accounts/import \
  -F "file=@chart_of_accounts.xlsx" \
  -F "validateOnly=true"
```

### Reference Management

#### Create reference (mark account as used)

```bash
curl -X POST http://localhost:8080/api/v1/references \
  -H "Content-Type: application/json" \
  -d '{
    "accountCode": "1110",
    "referenceSourceId": "rule-123",
    "referenceType": "RULE",
    "referenceDescription": "Cash receipt accounting rule"
  }'
```

#### List references for account

```bash
curl http://localhost:8080/api/v1/accounts/1110/references
```

#### Delete reference (when rule/scenario deleted)

```bash
curl -X DELETE http://localhost:8080/api/v1/references/456
```

---

## Excel/CSV File Format

### Excel Format (.xlsx, .xls)

**Required columns** (case-sensitive):

| Column Name | Type | Required | Description |
|-------------|------|----------|-------------|
| Code | String | Yes | Unique account code |
| Name | String | Yes | Account name |
| Parent Code | String | No | Parent account code (blank for root) |
| Description | String | No | Optional description |

**Example Excel file**:

| Code | Name | Parent Code | Description |
|------|------|-------------|-------------|
| 1000 | Assets | | All company assets |
| 1100 | Current Assets | 1000 | Assets convertible within 1 year |
| 1110 | Cash | 1100 | Cash on hand and in bank |
| 1120 | Accounts Receivable | 1100 | Money owed by customers |
| 2000 | Liabilities | | All company liabilities |

### CSV Format

Same columns as Excel, comma-separated:

```csv
Code,Name,Parent Code,Description
1000,Assets,,All company assets
1100,Current Assets,1000,Assets convertible within 1 year
1110,Cash,1100,Cash on hand and in bank
```

**Import rules**:
- First row must be headers
- Code must be unique
- Parent Code must reference existing code in file
- No circular references allowed
- Parents are created before children automatically

---

## Testing

### Run all tests

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify -Pintegration-tests

# With coverage report
./mvnw clean test jacoco:report
open target/site/jacoco/index.html
```

### Run specific test class

```bash
./mvnw test -Dtest=AccountServiceTest
```

### Integration test with Testcontainers

```java
@SpringBootTest
@Testcontainers
class AccountIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void shouldCreateAccountHierarchy() {
        // Test implementation
    }
}
```

---

## API Documentation

### Swagger UI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Spec

Raw OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```

Download spec file:
```bash
curl http://localhost:8080/v3/api-docs > coa-api.yaml
```

---

## Database Access

### Connect to PostgreSQL

```bash
# Using docker-compose
docker-compose exec postgres psql -U coa_user -d coa_db

# Using local client
psql -h localhost -U coa_user -d coa_db
```

### Useful queries

```sql
-- List all accounts
SELECT code, name, parent_id, created_at 
FROM accounts 
ORDER BY code;

-- View account hierarchy
WITH RECURSIVE account_tree AS (
    SELECT id, code, name, parent_id, 0 AS level
    FROM accounts 
    WHERE parent_id IS NULL
    UNION ALL
    SELECT a.id, a.code, a.name, a.parent_id, at.level + 1
    FROM accounts a
    INNER JOIN account_tree at ON a.parent_id = at.id
)
SELECT REPEAT('  ', level) || code AS hierarchy, name, level
FROM account_tree
ORDER BY code;

-- Check referenced accounts
SELECT a.code, a.name, COUNT(r.id) AS reference_count
FROM accounts a
LEFT JOIN account_references r ON a.code = r.account_code
GROUP BY a.code, a.name
HAVING COUNT(r.id) > 0;

-- View import history
SELECT id, file_name, status, total_records, 
       processed_records, failed_records, created_at
FROM import_jobs
ORDER BY created_at DESC;
```

---

## Troubleshooting

### Cannot delete account

**Error**: `409 Conflict - Cannot delete account: has 3 child accounts`

**Solution**: Delete all child accounts first, or use cascade delete API (if implemented).

**Error**: `409 Conflict - Cannot delete account: referenced by 2 rules`

**Solution**: Remove references first via rule/scenario module, then delete account.

### Cannot modify account code

**Error**: `409 Conflict - Cannot modify account code - referenced by rules`

**Solution**: Account codes become immutable once referenced. Create new account with different code instead.

### Import validation failed

**Error**: `400 Bad Request - Duplicate account code at line 15`

**Solution**: Ensure all codes in file are unique. Check for duplicate codes in existing database.

**Error**: `400 Bad Request - Parent code '2000' does not exist at line 22`

**Solution**: Ensure parent accounts are defined before child accounts in file, or parent already exists in database.

### Optimistic locking failure

**Error**: `409 Conflict - Version mismatch`

**Solution**: Refetch the account to get latest version, then retry update with new version.

### Connection refused

**Error**: `Connection refused to localhost:5432`

**Solution**: Ensure PostgreSQL is running:
```bash
docker-compose ps
docker-compose up -d postgres
```

---

## Performance Tuning

### Database Indexes

All necessary indexes are created by Flyway migrations. Verify:

```sql
SELECT tablename, indexname, indexdef 
FROM pg_indexes 
WHERE schemaname = 'public';
```

### Connection Pooling

HikariCP is configured by default. Adjust in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### Query Performance

Monitor slow queries:

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

## Next Steps

1. **Generate tasks**: Run `/speckit.tasks` to create implementation task list
2. **Implement endpoints**: Follow task breakdown to implement REST controllers
3. **Write tests**: Implement unit and integration tests per task
4. **Deploy**: Build Docker image and deploy to environment

---

## Reference Documentation

- [Feature Specification](./spec.md)
- [Implementation Plan](./plan.md)
- [Technical Research](./research.md)
- [Data Model](./data-model.md)
- [OpenAPI Contract](./contracts/coa-api.yaml)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Formance Ledger Docs](https://docs.formance.com/)

---

## Support

For questions or issues:
- Check [GitHub Issues](https://github.com/your-org/financial-journal-maker/issues)
- Review API documentation at `/swagger-ui.html`
- Consult feature specification for requirements

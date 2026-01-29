# Chart of Accounts Management Service

A Spring Boot REST API for managing hierarchical chart of accounts with Formance Ledger integration.

## Features

- **Account CRUD**: Create, read, update, delete accounts with hierarchical structure
- **Tree Navigation**: Retrieve account tree with PostgreSQL recursive CTEs
- **Reference Protection**: Prevent modifications to accounts used in rules/scenarios
- **Formance Ledger Mappings**: Map COA accounts to Formance Ledger paths
- **Batch Import**: Import accounts from Excel (.xlsx, .xls) or CSV files
- **Cross-Scenario Validation**: Mark accounts as shared and enforce consistency

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### Run with Docker Compose

```bash
# From project root
docker-compose up -d

# Check logs
docker logs coa-service

# Access API documentation
open http://localhost:8080/swagger-ui.html
```

### Local Development

```bash
# Start database only
docker-compose up -d postgres

# Run application
cd backend
mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/accounts` | Create account |
| GET | `/api/v1/accounts/{code}` | Get account by code |
| GET | `/api/v1/accounts` | List accounts (paginated) |
| GET | `/api/v1/accounts/tree` | Get account hierarchy |
| PUT | `/api/v1/accounts/{code}` | Update account |
| DELETE | `/api/v1/accounts/{code}` | Delete account |
| GET | `/api/v1/accounts/{code}/references` | List references |
| POST | `/api/v1/accounts/mappings` | Create Formance mapping |
| POST | `/api/v1/accounts/import` | Import from file |

## Example Requests

```bash
# Create root account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code":"1000","name":"Assets","description":"All asset accounts"}'

# Create child account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"code":"1100","name":"Current Assets","parentCode":"1000"}'

# Get account tree
curl http://localhost:8080/api/v1/accounts/tree

# Import from CSV
curl -X POST http://localhost:8080/api/v1/accounts/import \
  -F "file=@samples/accounts.csv"
```

## Configuration

Key properties in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:coa_db}
    username: ${DB_USER:coa_user}
    password: ${DB_PASSWORD:coa_password}

app:
  import:
    max-file-size: 10MB
    max-records: 10000
```

## Database Schema

The service uses Flyway for database migrations. Schema includes:
- `accounts` - Hierarchical account structure
- `account_mappings` - Formance Ledger mappings
- `account_references` - Reference tracking
- `import_jobs` - Import operation history

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests (requires Docker)
mvn verify
```

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Related Documentation

- [Specification](../specs/001-coa-management/spec.md)
- [Data Model](../specs/001-coa-management/data-model.md)
- [API Contract](../specs/001-coa-management/contracts/coa-api.yaml)
- [Quickstart Guide](../specs/001-coa-management/quickstart.md)

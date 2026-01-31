# Financial Journal Maker Development Guidelines

Auto-generated from all feature plans. **Last updated**: 2026-01-31

## Active Technologies

- Java 21 + Spring Boot 3.x (001-coa-management, 002-accounting-rules)
- PostgreSQL (001-coa-management, 002-accounting-rules)
- Expression Parser with strict typing (002-accounting-rules)
- Numscript DSL Generator (002-accounting-rules)

## Project Structure

```text
backend/
├── src/main/java/com/financial/
│   ├── coa/                        # 001-coa-management
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── exception/
│   │   └── config/
│   └── rules/                      # 002-accounting-rules
│       ├── controller/
│       ├── service/
│       │   ├── AccountingRuleService.java
│       │   ├── ExpressionParser.java
│       │   ├── NumscriptGenerator.java
│       │   └── RuleSimulationService.java
│       ├── repository/
│       ├── domain/
│       ├── dto/
│       └── exception/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       ├── V1__create_coa_tables.sql
│       └── V2__create_rules_tables.sql
└── src/test/java/com/financial/
    ├── coa/
    └── rules/
```

## Commands

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Run integration tests
./mvnw verify -Pintegration-tests

# Run application locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Code quality checks
./mvnw checkstyle:check
./mvnw spotbugs:check

# Database migrations
./mvnw flyway:info
./mvnw flyway:migrate

# Docker
docker-compose up -d
docker-compose logs -f coa-service
```

## Code Style

Java 21: Follow Spring Boot conventions
- Use standard Spring package structure: controller, service, repository, domain
- Follow RESTful API design principles
- Use OpenAPI 3.0 annotations for API documentation
- Implement optimistic locking with @Version
- Use JPA/Hibernate for data access
- Write tests with JUnit 5 and Testcontainers

## Recent Changes

- 002-accounting-rules: Added Accounting Rules Management module
- 002-accounting-rules: Added Expression Parser with strict typing
- 002-accounting-rules: Added Numscript DSL Generator
- 002-accounting-rules: Added Rule Simulation Engine
- 001-coa-management: Added Java 21 + Spring Boot 3.x
- 001-coa-management: Added PostgreSQL database
- 001-coa-management: Added Chart of Accounts Management API module

## Constitution Compliance

This project follows the Financial Journal Maker Constitution (v1.0.0):

1. **Domain Design Assistant**: System helps design accounting processes, not execute transactions
2. **Hierarchical Consistency**: Product → Scenario → Type model with shared foundations
3. **AI-Human Collaboration**: AI proposes, humans verify, AI validates
4. **Numscript DSL Output**: Transaction flows expressed in Formance-compatible Numscript
5. **OpenAPI-First Backend**: All APIs defined in OpenAPI 3.0+ before implementation
6. **Containerized Deployment**: Docker-based deployment mandatory

## Technology Stack Requirements

- **Frontend**: Vue + Nuxt (latest stable)
- **Backend**: Spring Boot/Cloud (Java 21)
- **Data Access**: JPA (Hibernate)
- **API Spec**: OpenAPI 3.0+ (Swagger/SpringDoc)
- **Containerization**: Docker + docker-compose
- **AI Integration**: Third-party API with configurable model selection

## Quality Gates

Before merging any feature:
1. OpenAPI spec updated and validated
2. Any generated Numscript passes syntax validation
3. All Docker containers build successfully
4. `docker-compose up` runs without errors
5. Integration tests pass

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->

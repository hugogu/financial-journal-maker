# Implementation Plan: Product/Scenario/TransactionType Management

**Branch**: `003-product-scenario-types` | **Date**: 2026-01-31 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/003-product-scenario-types/spec.md`

## Summary

Implement the business domain concept management module consisting of Product, Scenario, and TransactionType entities. These form a 3-level hierarchy that organizes accounting rules by business context. Key capabilities include CRUD operations with lifecycle states (DRAFT → ACTIVE → ARCHIVED), hierarchical navigation, rule associations, and template cloning.

## Technical Context

**Language/Version**: Java 17 (aligned with existing backend)  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, PostgreSQL  
**Storage**: PostgreSQL 15.x (existing database)  
**Testing**: JUnit 5, Spring Boot Test, TestRestTemplate  
**Target Platform**: Docker container, Linux server  
**Project Type**: Web application (backend module)  
**Performance Goals**: Tree query < 500ms for 1000 records  
**Constraints**: No multi-tenancy, simplified version history  
**Scale/Scope**: Hundreds of products, thousands of scenarios/types

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Evidence |
|-----------|--------|----------|
| I. Domain Design Assistant | ✅ PASS | Module manages design metadata, not transactions |
| II. Hierarchical Consistency | ✅ PASS | Follows Product → Scenario → Type hierarchy |
| III. AI-Human Collaboration | ✅ PASS | Supports AI analysis via rich descriptions |
| IV. Numscript DSL Output | N/A | This module provides context, not DSL generation |
| V. OpenAPI-First Backend | ✅ PASS | OpenAPI 3.0 contract defined in contracts/ |
| VI. Containerized Deployment | ✅ PASS | Extends existing Docker setup |

**Re-check after Phase 1**: All gates still pass. Data model and API contracts align with constitution principles.

## Project Structure

### Documentation (this feature)

```text
specs/003-product-scenario-types/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0: Technical decisions
├── data-model.md        # Phase 1: Entity definitions
├── quickstart.md        # Phase 1: Usage examples
├── contracts/
│   └── openapi.yaml     # Phase 1: API contract
├── checklists/
│   └── requirements.md  # Quality checklist
└── tasks.md             # Phase 2: Implementation tasks (next step)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/financial/
│   ├── domain/                    # New module package
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   ├── ScenarioController.java
│   │   │   └── TransactionTypeController.java
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   ├── Scenario.java
│   │   │   ├── TransactionType.java
│   │   │   ├── TransactionTypeRule.java
│   │   │   └── EntityStatus.java
│   │   ├── dto/
│   │   │   ├── ProductCreateRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   ├── ScenarioCreateRequest.java
│   │   │   ├── ... (other DTOs)
│   │   ├── repository/
│   │   │   ├── ProductRepository.java
│   │   │   ├── ScenarioRepository.java
│   │   │   ├── TransactionTypeRepository.java
│   │   │   └── TransactionTypeRuleRepository.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── ScenarioService.java
│   │   │   ├── TransactionTypeService.java
│   │   │   └── HierarchyService.java
│   │   └── exception/
│   │       └── DomainException.java
│   └── coa/                       # Existing COA module
│   └── rules/                     # Existing Rules module
├── src/main/resources/
│   └── db/migration/
│       └── V003__create_product_scenario_types.sql
└── src/test/java/com/financial/
    └── domain/
        └── controller/
            ├── ProductControllerIntegrationTest.java
            ├── ScenarioControllerIntegrationTest.java
            └── TransactionTypeControllerIntegrationTest.java
```

**Structure Decision**: Follows existing modular package structure (`coa`, `rules`). New `domain` package for Product/Scenario/TransactionType aligns with domain-driven organization.

## Complexity Tracking

> No constitution violations requiring justification.

## Generated Artifacts

| Artifact | Description | Status |
|----------|-------------|--------|
| research.md | Technical decisions and rationale | ✅ Complete |
| data-model.md | Entity definitions, relationships, migrations | ✅ Complete |
| contracts/openapi.yaml | REST API specification | ✅ Complete |
| quickstart.md | Usage examples and workflows | ✅ Complete |
| plan.md | This implementation plan | ✅ Complete |

## Next Steps

1. Run `/speckit.tasks` to generate detailed implementation tasks
2. Create database migration `V003__create_product_scenario_types.sql`
3. Implement domain entities (Product, Scenario, TransactionType)
4. Implement repositories and services
5. Implement REST controllers
6. Add integration tests
7. Verify against OpenAPI contract

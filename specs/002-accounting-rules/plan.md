# Implementation Plan: Accounting Rules Management

**Branch**: `002-accounting-rules` | **Date**: 2026-01-31 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-accounting-rules/spec.md`

## Summary

The Accounting Rules Management module enables financial controllers to define rules that translate business events into journal entries. Rules are framework-agnostic with pluggable output generation (initial: Numscript DSL for Formance Ledger). Key capabilities include:
- Rule CRUD with validation-gated lifecycle (draft → active requires validation)
- Entry templates with strictly-typed expressions and COA account references
- Trigger conditions with composable AND/OR logic
- Numscript DSL generation and syntax validation
- Rule simulation with sample data

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.x)
**Primary Dependencies**: Spring Boot Web, Spring Data JPA, SpringDoc OpenAPI, Lombok, Jakarta Validation
**Storage**: PostgreSQL 15.x with Flyway migrations
**Testing**: JUnit 5, Testcontainers, MockMvc
**Target Platform**: Linux server (Docker container)
**Project Type**: Web application (API-only backend, extends existing COA module)
**Performance Goals**: Rule simulation <2s for 10 entry lines; API responses <200ms p95
**Constraints**: Unlimited version history (no auto-purge), optimistic locking with version field
**Scale/Scope**: Initial: hundreds of rules, thousands of versions; production: 10K+ rules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Domain Design Assistant | ✅ PASS | Rules are design artifacts for downstream systems; no actual ledger operations |
| II. Hierarchical Consistency | ✅ PASS | Rules reusable across products/scenarios; integrates with shared COA |
| III. AI-Human Collaboration | ✅ PASS | Users design rules; system validates and suggests |
| IV. Numscript DSL Output | ✅ PASS | Numscript generation is core feature (FR-016 through FR-020) |
| V. OpenAPI-First Backend | ✅ PASS | API contracts will be defined in OpenAPI 3.0 |
| VI. Containerized Deployment | ✅ PASS | Extends existing docker-compose setup |

**Gate Result**: PASSED - No violations requiring justification.

## Project Structure

### Documentation (this feature)

```text
specs/002-accounting-rules/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── accounting-rules-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/financial/
│   └── rules/                    # New module for accounting rules
│       ├── domain/               # Entities: AccountingRule, EntryTemplate, etc.
│       ├── repository/           # JPA repositories
│       ├── dto/                  # Request/Response DTOs
│       ├── service/              # Business logic, validation, generation
│       │   ├── AccountingRuleService.java
│       │   ├── ExpressionParser.java
│       │   ├── NumscriptGenerator.java
│       │   └── RuleSimulationService.java
│       ├── controller/           # REST controllers
│       └── exception/            # Domain-specific exceptions
├── src/main/resources/
│   └── db/migration/
│       └── V2__create_rules_tables.sql
└── src/test/java/com/financial/rules/
    ├── unit/
    ├── integration/
    └── contract/
```

**Structure Decision**: Extends existing backend with new `rules` package parallel to `coa` package. Shares infrastructure (database, docker-compose) with COA module.

## Complexity Tracking

> No violations - table not required.

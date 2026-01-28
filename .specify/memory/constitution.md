<!--
=============================================================================
SYNC IMPACT REPORT
=============================================================================
Version change: N/A (initial) → 1.0.0
Modified principles: N/A (initial creation)
Added sections:
  - Core Principles (6 principles)
  - Technology Stack
  - Development Workflow
  - Governance
Removed sections: N/A
Templates status:
  - .specify/templates/plan-template.md: ✅ reviewed (no updates required)
  - .specify/templates/spec-template.md: ✅ reviewed (no updates required)
  - .specify/templates/tasks-template.md: ✅ reviewed (no updates required)
Follow-up TODOs: None
=============================================================================
-->

# Financial Journal Maker Constitution

## Core Principles

### I. Domain Design Assistant (NON-NEGOTIABLE)

This system is a **domain design assistant**, NOT an accounting system or transaction system.

- System MUST focus on helping users design accounting processes, chart of accounts, and bookkeeping rules
- System MUST NOT implement actual ledger operations, transaction processing, or fund transfers
- All outputs are design artifacts (schemas, rules, Numscript DSL) intended for downstream systems
- AI assists with analysis and suggestions; humans make final design decisions

**Rationale**: Prevents scope creep and maintains clear system boundaries.

### II. Hierarchical Consistency

Design follows a layered model: **Product → Scenario → Type** with shared foundations.

- Lower-level chart of accounts and bookkeeping rules MUST be reusable across products/scenarios
- Order → Transaction → Accounting three-layer model MUST be maintained per product/scenario/type
- Subsequent design phases MUST respect previously established account structures and rules
- AI MUST validate that new designs do not conflict with existing shared components

**Rationale**: Ensures consistent accounting treatment across business lines.

### III. AI-Human Collaboration

AI proposes, humans verify and modify, AI validates.

- AI MUST provide initial analysis based on user's business scenario description
- Users MUST be able to modify any AI-generated design artifacts
- AI MUST validate user modifications for logical consistency and completeness
- AI MUST suggest next steps based on current design state and user actions
- System MUST support importing existing chart of accounts from files

**Rationale**: Combines AI efficiency with human domain expertise and oversight.

### IV. Numscript DSL Output

All financial transaction flow designs MUST be expressed in Formance Ledger compatible Numscript DSL.

- Generated Numscript MUST be syntactically valid per Formance specification
- Numscript outputs MUST accurately reflect the designed bookkeeping rules
- System MUST provide Numscript validation before export
- Documentation MUST reference Numscript DSL specification for users

**Rationale**: Ensures outputs are directly usable with Formance Ledger infrastructure.

### V. OpenAPI-First Backend

All backend APIs MUST be OpenAPI 3.0+ compliant.

- API contracts MUST be defined in OpenAPI specification before implementation
- Generated documentation MUST be available at runtime (Swagger UI or equivalent)
- Breaking API changes MUST follow semantic versioning
- Frontend-backend contract MUST be validated against OpenAPI spec

**Rationale**: Enables clear contracts, auto-generated clients, and API documentation.

### VI. Containerized Deployment

Docker-based deployment is mandatory.

- Application MUST be deployable via single `docker-compose up` command for local development
- All services (frontend, backend, database) MUST have Dockerfile definitions
- Environment configuration MUST be externalized via environment variables
- Third-party AI API integration MUST support configurable endpoints and model selection

**Rationale**: Ensures reproducible environments and simplified onboarding.

## Technology Stack

**Mandatory technologies for this project:**

| Layer | Technology | Version/Notes |
|-------|------------|---------------|
| Frontend | Vue + Nuxt | Latest stable |
| Backend | Spring Boot/Cloud | Java 21 |
| Data Access | JPA (Hibernate) | - |
| API Spec | OpenAPI 3.0+ | Swagger/SpringDoc |
| Containerization | Docker + docker-compose | Local & production |
| AI Integration | Third-party API | Configurable LLM model selection |

**First version scope exclusions:**
- User account management (deferred to v2)

## Development Workflow

**Quality gates before merge:**

1. **API Contract**: OpenAPI spec updated and validated
2. **Numscript Validity**: Any generated DSL passes syntax validation
3. **Docker Build**: All containers build successfully
4. **Integration Test**: `docker-compose up` runs without errors

**Code organization:**

- Backend: Standard Spring Boot package structure (`controller`, `service`, `repository`, `domain`)
- Frontend: Nuxt conventions (`pages`, `components`, `composables`, `stores`)
- Shared: OpenAPI specs in `/api-specs/`, Numscript examples in `/numscript-examples/`

## Governance

- This constitution supersedes all other development practices for this project
- Amendments require: (1) documented rationale, (2) review of dependent templates, (3) version increment
- All PRs MUST verify compliance with principles before merge
- Complexity beyond these principles MUST be justified in PR description

**Version**: 1.0.0 | **Ratified**: 2026-01-28 | **Last Amended**: 2026-01-28

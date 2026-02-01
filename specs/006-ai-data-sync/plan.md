# Implementation Plan: AI-System Data Bidirectional Sync

**Branch**: `006-ai-data-sync` | **Date**: 2026-02-01 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-ai-data-sync/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Enable bidirectional real-time data synchronization between AI conversation sessions and system entities (Product, Scenario, Account, BookingRule, Numscript). AI will automatically load relevant system data as context when users discuss design topics, and user-confirmed AI suggestions will be automatically persisted to the database with full conflict detection, atomic transactions, and provenance tracking.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.x)  
**Primary Dependencies**: Spring Boot, Spring Data JPA, Spring AI, Hibernate  
**Storage**: PostgreSQL (existing database with Product, Scenario, Account, AccountingRule tables)  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers  
**Target Platform**: Linux server (containerized via Docker)
**Project Type**: Web application (Vue/Nuxt frontend + Spring Boot backend)  
**Performance Goals**: <500ms for data sync operations, support 100 concurrent AI sessions  
**Constraints**: Atomic transactions required, zero data integrity violations, conversation history must be preserved  
**Scale/Scope**: ~10 new backend classes, 5-8 REST endpoints, extend existing AI conversation infrastructure

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. Domain Design Assistant
**Status**: PASS  
**Rationale**: Feature maintains system boundary as design assistant. AI reads design artifacts (products, scenarios, accounts, rules) and creates/updates design artifacts. No actual ledger operations or fund transfers involved.

### ✅ II. Hierarchical Consistency
**Status**: PASS  
**Rationale**: Feature respects existing Product → Scenario → TransactionType hierarchy. Conflict detection (FR-015 to FR-019) ensures new AI-generated designs don't violate existing relationships. Shared accounts and rules remain reusable.

### ✅ III. AI-Human Collaboration
**Status**: PASS  
**Rationale**: Core feature requirement. AI proposes (data read), user confirms (FR-007), system persists. User retains full control over what gets created. Aligns perfectly with constitution principle.

### ✅ IV. Numscript DSL Output
**Status**: PASS  
**Rationale**: FR-012 requires auto-generating Numscript when booking rules are created via AI. Existing NumscriptGenerator service will be reused. No changes to Numscript validation logic needed.

### ✅ V. OpenAPI-First Backend
**Status**: PASS  
**Rationale**: New REST endpoints for AI data sync will follow existing OpenAPI patterns. API contracts will be defined in Phase 1 before implementation.

### ✅ VI. Containerized Deployment
**Status**: PASS  
**Rationale**: No infrastructure changes required. Feature extends existing backend services which are already containerized via docker-compose.

## Project Structure

### Documentation (this feature)

```text
specs/006-ai-data-sync/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/financial/
│   ├── ai/
│   │   ├── domain/           # Existing: AnalysisSession, SessionMessage
│   │   ├── service/          # Existing: AIConversationService, SystemDataService
│   │   ├── sync/             # NEW: Data sync feature package
│   │   │   ├── domain/       # DataSyncContext, SyncOperation entities
│   │   │   ├── dto/          # Sync request/response DTOs
│   │   │   ├── service/      # DataSyncService, ConflictDetectionService
│   │   │   ├── repository/   # DataSyncContext, SyncOperation repositories
│   │   │   └── controller/   # REST endpoints for sync operations
│   │   └── repository/       # Existing: SessionRepository, MessageRepository
│   ├── domain/
│   │   ├── domain/           # Existing: Product, Scenario, TransactionType
│   │   ├── service/          # Existing: ProductService, ScenarioService
│   │   └── repository/       # Existing: ProductRepository, ScenarioRepository
│   ├── coa/
│   │   ├── domain/           # Existing: Account
│   │   └── service/          # Existing: AccountService
│   └── rules/
│       ├── domain/           # Existing: AccountingRule, EntryTemplate
│       └── service/          # Existing: AccountingRuleService, NumscriptGenerator
└── src/test/java/com/financial/ai/sync/
    ├── service/              # Unit tests for sync services
    └── controller/           # Integration tests for sync endpoints

frontend/
├── components/
│   └── ai/
│       ├── ConversationPanel.vue      # Existing: AI chat interface
│       └── SyncStatusIndicator.vue    # NEW: Visual sync indicators
├── composables/
│   └── useAISync.ts                   # NEW: Sync state management
└── stores/
    └── aiSyncStore.ts                 # NEW: Sync operation store
```

**Structure Decision**: Web application structure. Backend extends existing `com.financial.ai` package with new `sync` subpackage to maintain cohesion with AI conversation features. Frontend adds sync UI components to existing AI interface. No new top-level modules required.

## Complexity Tracking

> **No violations** - All constitution checks passed. No complexity justification needed.

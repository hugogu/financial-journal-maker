# Implementation Plan: AI Analysis Session

**Branch**: `004-ai-analysis-session` | **Date**: 2026-01-31 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-ai-analysis-session/spec.md`

## Summary

Implement an AI-powered analysis session module that enables analysts to design accounting processes through conversation. The system guides users through a hierarchical design flow (Product → Scenario → TransactionType → Accounting), integrates with existing system data, and exports finalized designs as COA entries, accounting rules, and Numscript. Includes admin configuration for LLM providers and custom prompts.

## Technical Context

**Language/Version**: Java 21 (Backend), TypeScript (Frontend with Vue/Nuxt)
**Primary Dependencies**: Spring Boot 3.x, Spring AI, Vue 3, Nuxt 3, WebSocket/SSE for streaming
**Storage**: PostgreSQL (via JPA/Hibernate)
**Testing**: JUnit 5 + Spring Boot Test (backend), Vitest (frontend)
**Target Platform**: Docker containers, browser-based frontend
**Project Type**: Web application (frontend + backend)
**Performance Goals**: AI first byte < 3 seconds (95th percentile), streaming responses
**Constraints**: Max 5 concurrent sessions per analyst, manual LLM failover only
**Scale/Scope**: Single-tenant deployment, indefinite session retention

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Domain Design Assistant | ✅ PASS | Feature focuses on design assistance via AI conversation, outputs design artifacts only |
| II. Hierarchical Consistency | ✅ PASS | Enforces Product → Scenario → Type hierarchy, validates against existing structures |
| III. AI-Human Collaboration | ✅ PASS | AI proposes, user modifies, AI validates - core workflow of this feature |
| IV. Numscript DSL Output | ✅ PASS | Export includes Numscript generation from confirmed designs |
| V. OpenAPI-First Backend | ✅ PASS | All APIs will be OpenAPI 3.0+ compliant with SpringDoc |
| VI. Containerized Deployment | ✅ PASS | Will integrate with existing docker-compose setup |

**v1 Scope Exclusions Applied**: Authorization deferred to v2 per constitution

## Project Structure

### Documentation (this feature)

```text
specs/004-ai-analysis-session/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (OpenAPI specs)
│   └── ai-session-api.yaml
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/financial/
│   ├── ai/                          # NEW: AI session module
│   │   ├── controller/
│   │   │   ├── SessionController.java
│   │   │   ├── AIConfigController.java
│   │   │   └── PromptController.java
│   │   ├── service/
│   │   │   ├── SessionService.java
│   │   │   ├── AIConversationService.java
│   │   │   ├── DesignExportService.java
│   │   │   └── PromptService.java
│   │   ├── domain/
│   │   │   ├── AnalysisSession.java
│   │   │   ├── SessionMessage.java
│   │   │   ├── DesignDecision.java
│   │   │   ├── AIConfiguration.java
│   │   │   ├── PromptTemplate.java
│   │   │   └── ExportArtifact.java
│   │   ├── repository/
│   │   │   ├── SessionRepository.java
│   │   │   ├── MessageRepository.java
│   │   │   ├── DecisionRepository.java
│   │   │   ├── AIConfigRepository.java
│   │   │   └── PromptRepository.java
│   │   └── dto/
│   │       ├── SessionRequest.java
│   │       ├── SessionResponse.java
│   │       ├── MessageRequest.java
│   │       ├── MessageResponse.java
│   │       ├── AIConfigRequest.java
│   │       └── ExportResponse.java
│   ├── domain/                      # EXISTING: Product/Scenario/TransactionType
│   └── coa/                         # EXISTING: Chart of Accounts
└── src/main/resources/
    └── db/migration/
        └── V004__create_ai_session_tables.sql

frontend/                            # NEW: Vue/Nuxt frontend
├── pages/
│   ├── analysis/
│   │   ├── index.vue               # Session list
│   │   └── [id].vue                # Session conversation view
│   └── admin/
│       ├── ai-config.vue           # AI provider configuration
│       └── prompts.vue             # Prompt template management
├── components/
│   ├── session/
│   │   ├── ChatInterface.vue
│   │   ├── MessageBubble.vue
│   │   ├── DesignPanel.vue
│   │   └── ExportDialog.vue
│   └── admin/
│       ├── ProviderForm.vue
│       └── PromptEditor.vue
├── composables/
│   ├── useSession.ts
│   ├── useAIStream.ts
│   └── useExport.ts
└── stores/
    ├── session.ts
    └── aiConfig.ts
```

**Structure Decision**: Web application structure with existing Spring Boot backend extended with new `ai` package, and new Nuxt frontend for analyst/admin interfaces. Frontend communicates via REST API with SSE for streaming AI responses.

## Complexity Tracking

No constitution violations requiring justification. All principles pass.

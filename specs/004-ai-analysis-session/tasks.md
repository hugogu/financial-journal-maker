# Tasks: AI Analysis Session

**Input**: Design documents from `/specs/004-ai-analysis-session/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ai-session-api.yaml

**Tests**: Integration tests included for backend functionality.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/financial/ai/`
- **Frontend**: `frontend/pages/`, `frontend/components/`, `frontend/composables/`, `frontend/stores/`
- **Migrations**: `backend/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependencies, and basic structure

- [x] T001 Add Spring AI dependency to backend/pom.xml
- [x] T002 [P] Create ai package structure in backend/src/main/java/com/financial/ai/ (controller, service, domain, repository, dto subpackages)
- [x] T003 [P] Initialize Nuxt 3 frontend project in frontend/ directory
- [x] T004 [P] Configure frontend dependencies (Pinia, PrimeVue or similar UI library) in frontend/package.json
- [x] T005 [P] Add SSE support configuration in backend/src/main/java/com/financial/config/WebConfig.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Create database migration V004__create_ai_session_tables.sql in backend/src/main/resources/db/migration/
- [x] T007 [P] Create SessionStatus enum in backend/src/main/java/com/financial/ai/domain/SessionStatus.java
- [x] T008 [P] Create DesignPhase enum in backend/src/main/java/com/financial/ai/domain/DesignPhase.java
- [x] T009 [P] Create MessageRole enum in backend/src/main/java/com/financial/ai/domain/MessageRole.java
- [x] T010 [P] Create ExportType enum in backend/src/main/java/com/financial/ai/domain/ExportType.java
- [x] T011 Create AnalysisSession entity in backend/src/main/java/com/financial/ai/domain/AnalysisSession.java
- [x] T012 [P] Create SessionMessage entity in backend/src/main/java/com/financial/ai/domain/SessionMessage.java
- [x] T013 [P] Create DesignDecision entity in backend/src/main/java/com/financial/ai/domain/DesignDecision.java
- [x] T014 [P] Create AIConfiguration entity in backend/src/main/java/com/financial/ai/domain/AIConfiguration.java
- [x] T015 [P] Create PromptTemplate entity in backend/src/main/java/com/financial/ai/domain/PromptTemplate.java
- [x] T016 [P] Create ExportArtifact entity in backend/src/main/java/com/financial/ai/domain/ExportArtifact.java
- [x] T017 Create SessionRepository in backend/src/main/java/com/financial/ai/repository/SessionRepository.java
- [x] T018 [P] Create MessageRepository in backend/src/main/java/com/financial/ai/repository/MessageRepository.java
- [x] T019 [P] Create DecisionRepository in backend/src/main/java/com/financial/ai/repository/DecisionRepository.java
- [x] T020 [P] Create AIConfigRepository in backend/src/main/java/com/financial/ai/repository/AIConfigRepository.java
- [x] T021 [P] Create PromptRepository in backend/src/main/java/com/financial/ai/repository/PromptRepository.java
- [x] T022 [P] Create ExportArtifactRepository in backend/src/main/java/com/financial/ai/repository/ExportArtifactRepository.java
- [x] T023 [P] Create session DTOs (SessionCreateRequest, SessionUpdateRequest, SessionResponse, SessionDetailResponse) in backend/src/main/java/com/financial/ai/dto/
- [x] T024 [P] Create message DTOs (MessageRequest, MessageResponse) in backend/src/main/java/com/financial/ai/dto/
- [x] T025 [P] Create decision DTOs (DecisionRequest, DecisionResponse) in backend/src/main/java/com/financial/ai/dto/
- [x] T026 [P] Create AI exception classes (SessionNotFoundException, MaxSessionsExceededException, InvalidSessionStateException) in backend/src/main/java/com/financial/ai/exception/
- [x] T027 Add AI exception handlers to GlobalExceptionHandler in backend/src/main/java/com/financial/coa/exception/GlobalExceptionHandler.java
- [x] T028 [P] Create frontend layout and navigation structure in frontend/layouts/default.vue
- [x] T029 [P] Create Pinia session store skeleton in frontend/stores/session.ts

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Analyst Creates and Conducts AI Analysis Session (Priority: P1) 🎯 MVP

**Goal**: Enable analysts to create sessions, describe business scenarios, receive AI suggestions, and confirm designs through Product → Scenario → TransactionType → Accounting hierarchy

**Independent Test**: Create a session, enter a business description, receive AI suggestions, confirm design at each level

### Implementation for User Story 1

- [x] T030 [US1] Implement SessionService core methods (create, get, update) in backend/src/main/java/com/financial/ai/service/SessionService.java
- [x] T031 [US1] Add concurrent session limit validation (max 5) to SessionService in backend/src/main/java/com/financial/ai/service/SessionService.java
- [x] T032 [US1] Create LLMClientProvider for configurable AI client in backend/src/main/java/com/financial/ai/service/LLMClientProvider.java
- [x] T033 [US1] Implement AIConversationService with streaming support in backend/src/main/java/com/financial/ai/service/AIConversationService.java
- [x] T034 [US1] Implement design phase progression logic in AIConversationService (Product → Scenario → Type → Accounting)
- [x] T035 [US1] Implement DecisionService for confirm/reject decisions in backend/src/main/java/com/financial/ai/service/DecisionService.java
- [x] T036 [US1] Create SessionController with CRUD endpoints in backend/src/main/java/com/financial/ai/controller/SessionController.java
- [x] T037 [US1] Add SSE streaming endpoint for AI responses in SessionController (/sessions/{id}/messages/stream)
- [x] T038 [US1] Add decision endpoints to SessionController (/sessions/{id}/decisions)
- [x] T039 [P] [US1] Add OpenAPI annotations to SessionController
- [x] T040 [P] [US1] Create useSession composable in frontend/composables/useSession.ts
- [x] T041 [P] [US1] Create useAIStream composable for SSE handling in frontend/composables/useAIStream.ts
- [x] T042 [US1] Create ChatInterface component in frontend/components/session/ChatInterface.vue
- [x] T043 [P] [US1] Create MessageBubble component in frontend/components/session/MessageBubble.vue
- [x] T044 [P] [US1] Create DesignPanel component for confirmed decisions in frontend/components/session/DesignPanel.vue
- [x] T045 [US1] Create session conversation page in frontend/pages/analysis/[id].vue
- [x] T046 [US1] Write integration test for session creation and AI conversation in backend/src/test/java/com/financial/ai/controller/SessionControllerIntegrationTest.java

**Checkpoint**: User Story 1 complete - analysts can create sessions and have AI-guided design conversations

---

## Phase 4: User Story 2 - Session Lifecycle Management (Priority: P1)

**Goal**: Enable pause/resume/complete/archive session lifecycle with full context preservation

**Independent Test**: Create session, add content, pause, close browser, resume, verify context preserved

### Implementation for User Story 2

- [x] T047 [US2] Implement session lifecycle methods in SessionService (pause, resume, complete, archive)
- [ ] T048 [US2] Add auto-save functionality with configurable interval to SessionService (deferred - optional)
- [x] T049 [US2] Add lifecycle state validation (prevent invalid transitions)
- [x] T050 [US2] Add lifecycle endpoints to SessionController (/pause, /resume, /complete, /archive)
- [x] T051 [P] [US2] Create session list page with status filtering in frontend/pages/analysis/index.vue
- [x] T052 [P] [US2] Add session status badges and action buttons to session list
- [ ] T053 [US2] Implement auto-save in frontend with interval timer (deferred - optional)
- [x] T054 [US2] Write integration test for session lifecycle transitions in backend/src/test/java/com/financial/ai/controller/SessionLifecycleIntegrationTest.java

**Checkpoint**: User Story 2 complete - full session lifecycle management working

---

## Phase 5: User Story 3 - Design Export (Priority: P2)

**Goal**: Export completed designs as COA entries, accounting rules, and Numscript

**Independent Test**: Complete a session, export each artifact type, verify content matches design

### Implementation for User Story 3

- [x] T055 [US3] Create DesignExportService in backend/src/main/java/com/financial/ai/service/DesignExportService.java
- [x] T056 [US3] Implement COA export generation from confirmed decisions
- [x] T057 [US3] Implement accounting rules export generation
- [x] T058 [US3] Implement Numscript export using existing NumscriptGenerator service
- [x] T059 [US3] Add conflict detection and force overwrite support to DesignExportService
- [x] T060 [US3] Create export DTOs (ExportResponse, ExportConflictResponse) in backend/src/main/java/com/financial/ai/dto/
- [x] T061 [US3] Add export endpoints to SessionController (/sessions/{id}/export/{type})
- [x] T062 [P] [US3] Create useExport composable in frontend/composables/useExport.ts
- [x] T063 [P] [US3] Create ExportDialog component in frontend/components/session/ExportDialog.vue
- [x] T064 [US3] Integrate export dialog into session page
- [x] T065 [US3] Write integration test for export functionality in backend/src/test/java/com/financial/ai/controller/ExportIntegrationTest.java

**Checkpoint**: User Story 3 complete - design export to COA/rules/Numscript working

---

## Phase 6: User Story 4 - Admin Configures AI Models and API Keys (Priority: P2)

**Goal**: Admin can configure multiple LLM providers, API keys, and select active model

**Independent Test**: Configure API key, select different model, create session, verify new model used

### Implementation for User Story 4

- [x] T066 [US4] Create AIConfigService in backend/src/main/java/com/financial/ai/service/AIConfigService.java
- [x] T067 [US4] Implement API key encryption/decryption in AIConfigService
- [x] T068 [US4] Implement provider activation (only one active at a time)
- [x] T069 [US4] Add configuration validation and connectivity test method
- [x] T070 [US4] Create AI config DTOs (AIConfigRequest, AIConfigResponse, AIConfigTestResponse) in backend/src/main/java/com/financial/ai/dto/
- [x] T071 [US4] Create AIConfigController in backend/src/main/java/com/financial/ai/controller/AIConfigController.java
- [x] T072 [P] [US4] Add OpenAPI annotations to AIConfigController
- [x] T073 [P] [US4] Create aiConfig Pinia store in frontend/stores/aiConfig.ts
- [x] T074 [P] [US4] Create ProviderForm component in frontend/components/admin/ProviderForm.vue
- [x] T075 [US4] Create AI configuration admin page in frontend/pages/admin/ai-config.vue
- [x] T076 [US4] Write integration test for AI configuration in backend/src/test/java/com/financial/ai/controller/AIConfigControllerIntegrationTest.java

**Checkpoint**: User Story 4 complete - admin can manage LLM provider configurations

---

## Phase 7: User Story 5 - Admin Manages Custom Prompts (Priority: P3)

**Goal**: Admin can customize prompts per design phase with version history and rollback

**Independent Test**: Modify prompt, start new session, verify AI uses customized prompt

### Implementation for User Story 5

- [ ] T077 [US5] Create PromptService in backend/src/main/java/com/financial/ai/service/PromptService.java
- [ ] T078 [US5] Implement prompt versioning and rollback in PromptService
- [ ] T079 [US5] Implement prompt activation (one active per phase)
- [ ] T080 [US5] Create prompt DTOs (PromptRequest, PromptResponse) in backend/src/main/java/com/financial/ai/dto/
- [ ] T081 [US5] Create PromptController in backend/src/main/java/com/financial/ai/controller/PromptController.java
- [ ] T082 [P] [US5] Add OpenAPI annotations to PromptController
- [ ] T083 [P] [US5] Create PromptEditor component in frontend/components/admin/PromptEditor.vue
- [ ] T084 [US5] Create prompts admin page in frontend/pages/admin/prompts.vue
- [ ] T085 [US5] Integrate prompt templates into AIConversationService
- [ ] T086 [US5] Write integration test for prompt management in backend/src/test/java/com/financial/ai/controller/PromptControllerIntegrationTest.java

**Checkpoint**: User Story 5 complete - admin can manage custom prompts with versioning

---

## Phase 8: User Story 6 - AI Integrates Existing System Data (Priority: P2)

**Goal**: AI retrieves and incorporates existing Products/Scenarios/TransactionTypes during analysis

**Independent Test**: With existing Products in system, start session about related topic, verify AI references existing structures

### Implementation for User Story 6

- [ ] T087 [US6] Create SystemDataService to query existing domain entities in backend/src/main/java/com/financial/ai/service/SystemDataService.java
- [ ] T088 [US6] Implement product/scenario/transactionType retrieval methods
- [ ] T089 [US6] Implement accounting rules retrieval for AI context
- [ ] T090 [US6] Integrate SystemDataService into AIConversationService for context enrichment
- [ ] T091 [US6] Add existing data summary to AI prompts ({{existingProducts}}, etc.)
- [ ] T092 [US6] Support linking decisions to existing entities (linkedEntityId)
- [ ] T093 [US6] Write integration test for system data integration in backend/src/test/java/com/financial/ai/controller/SystemDataIntegrationTest.java

**Checkpoint**: User Story 6 complete - AI references existing system data in suggestions

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T094 [P] Update OpenAPI/Swagger annotations on all controllers
- [ ] T095 [P] Add comprehensive logging to all services
- [ ] T096 Run full integration test suite and fix any failures
- [ ] T097 Validate against quickstart.md examples
- [ ] T098 Update README-STARTUP.md with new API endpoints
- [ ] T099 [P] Add frontend error handling and loading states
- [ ] T100 [P] Update docker-compose.yml for frontend service
- [ ] T101 Final code cleanup and documentation review

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-8)**: All depend on Foundational phase completion
  - US1 and US2 are both P1 priority - can proceed in parallel
  - US3, US4, US6 are P2 priority - can proceed after P1 or in parallel
  - US5 is P3 priority - lowest priority
- **Polish (Phase 9)**: Depends on all desired user stories being complete

### User Story Dependencies

| Story | Priority | Dependencies | Can Parallelize With |
|-------|----------|--------------|---------------------|
| US1 | P1 | Foundational only | US2 |
| US2 | P1 | Foundational only | US1 |
| US3 | P2 | US1 (needs completed sessions) | US4, US6 |
| US4 | P2 | Foundational only | US3, US6 |
| US5 | P3 | US4 (needs AI config) | - |
| US6 | P2 | US1 (needs conversation service) | US3, US4 |

### Within Each User Story

- Models/entities before services
- Services before controllers
- Backend before frontend
- Core implementation before integration tests

### Parallel Opportunities

**Phase 2 (Foundational)**:
- All enums (T007-T010) in parallel
- All entities (T011-T016) in parallel after enums
- All repositories (T017-T022) in parallel after entities
- All DTOs (T023-T025) in parallel
- Exception classes and frontend setup in parallel

**User Stories**:
- US1 and US2 can run in parallel (both P1, independent)
- US3, US4, US6 can run in parallel (all P2, mostly independent)
- Within each story: components marked [P] can parallelize

---

## Parallel Example: Phase 2 Foundational

```bash
# Batch 1: All enums in parallel
T007: SessionStatus enum
T008: DesignPhase enum
T009: MessageRole enum
T010: ExportType enum

# Batch 2: All entities in parallel
T011: AnalysisSession entity
T012: SessionMessage entity
T013: DesignDecision entity
T014: AIConfiguration entity
T015: PromptTemplate entity
T016: ExportArtifact entity

# Batch 3: All repositories in parallel
T017-T022: All repositories

# Batch 4: All DTOs and exceptions in parallel
T023-T026: DTOs and exceptions
```

## Parallel Example: User Story 1

```bash
# Backend services (sequential due to dependencies)
T030-T035: Services

# Backend controller and frontend composables (can parallelize)
T036-T039: Controller
T040-T041: Composables (parallel)

# Frontend components (can parallelize)
T042: ChatInterface
T043: MessageBubble (parallel)
T044: DesignPanel (parallel)
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Core AI conversation)
4. Complete Phase 4: User Story 2 (Session lifecycle)
5. **STOP and VALIDATE**: Test sessions and lifecycle independently
6. Deploy/demo if ready - **This is the MVP!**

### Incremental Delivery

1. **MVP** (Phase 1-4): Setup + Foundation + US1 + US2 → Core analyst workflow
2. **+Export** (Phase 5): Add US3 → Designs become actionable
3. **+Admin** (Phase 6-7): Add US4 + US5 → Full admin capabilities
4. **+Integration** (Phase 8): Add US6 → AI leverages existing data
5. **Polish** (Phase 9): Cross-cutting improvements

### Suggested MVP Scope

**Minimum Viable Product includes**:
- User Story 1: AI-guided design sessions
- User Story 2: Session lifecycle management

**Total MVP Tasks**: T001-T054 (54 tasks)

---

## Task Summary

| Phase | Description | Task Count |
|-------|-------------|------------|
| 1 | Setup | 5 |
| 2 | Foundational | 24 |
| 3 | US1 - AI Analysis Session | 17 |
| 4 | US2 - Session Lifecycle | 8 |
| 5 | US3 - Design Export | 11 |
| 6 | US4 - AI Configuration | 11 |
| 7 | US5 - Custom Prompts | 10 |
| 8 | US6 - System Data Integration | 7 |
| 9 | Polish | 8 |
| **Total** | | **101** |

---

## Notes

- [P] tasks = different files, no dependencies within same phase
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Authorization is deferred to v2 per project constitution

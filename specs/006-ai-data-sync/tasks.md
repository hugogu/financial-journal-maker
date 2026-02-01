# Implementation Tasks: AI-System Data Bidirectional Sync

**Feature**: 006-ai-data-sync  
**Branch**: `006-ai-data-sync`  
**Created**: 2026-02-01

## Overview

This document provides a complete task breakdown for implementing bidirectional data synchronization between AI conversation sessions and system entities. Tasks are organized by user story to enable independent implementation and testing.

**Tech Stack**: Java 21, Spring Boot 3.x, Spring Data JPA, PostgreSQL, Vue 3, Nuxt  
**Estimated Total**: 62 tasks across 7 phases

---

## Implementation Strategy

### MVP Scope (Minimum Viable Product)
**User Story 1 (P1)** represents the MVP - AI reading system data for context. This delivers immediate value and can be deployed independently.

### Incremental Delivery
- **Phase 3 (US1)**: Deploy first - AI context loading
- **Phase 4 (US2)**: Deploy second - Complete bidirectional sync
- **Phase 5 (US3)**: Deploy third - Add conflict detection
- **Phase 6-7**: Polish and additional features

### Parallel Execution Opportunities
Tasks marked with **[P]** can be executed in parallel with other [P] tasks in the same phase, as they work on different files or have no dependencies.

---

## Phase 1: Setup & Infrastructure

**Goal**: Prepare development environment and database schema

### Database Migration

- [ ] T001 Create Flyway migration script for new sync tables in backend/src/main/resources/db/migration/V{N}__add_ai_sync_tables.sql
- [ ] T002 Add data_sync_contexts table with JSONB columns per data-model.md schema
- [ ] T003 Add sync_operations table with enum constraints per data-model.md schema
- [ ] T004 Add conflict_resolutions table with JSONB resolution_options per data-model.md schema
- [ ] T005 Add AI provenance columns (ai_created, ai_session_id, ai_modified_at) to products table
- [ ] T006 Add AI provenance columns to scenarios table
- [ ] T007 Add AI provenance columns to accounts table
- [ ] T008 Add AI provenance columns to accounting_rules table
- [ ] T009 Add AI provenance columns to transaction_types table
- [ ] T010 Create indexes per data-model.md (idx_sync_context_last_sync, idx_sync_op_session, idx_sync_op_entity, idx_conflict_session, idx_conflict_resolution)
- [ ] T011 Run migration on local dev environment and verify schema with \dt and \d commands

### Package Structure

- [ ] T012 [P] Create backend/src/main/java/com/financial/ai/sync package structure (domain/, dto/, service/, repository/, controller/, exception/)
- [ ] T013 [P] Create backend/src/test/java/com/financial/ai/sync package structure (service/, controller/)

---

## Phase 2: Foundational Components

**Goal**: Build shared infrastructure needed by all user stories

### Domain Entities

- [ ] T014 [P] Create OperationType enum in backend/src/main/java/com/financial/ai/sync/domain/OperationType.java (READ, CREATE, UPDATE, DELETE)
- [ ] T015 [P] Create SyncStatus enum in backend/src/main/java/com/financial/ai/sync/domain/SyncStatus.java (PENDING, SUCCESS, FAILED, ROLLED_BACK)
- [ ] T016 [P] Create ConflictType enum in backend/src/main/java/com/financial/ai/sync/domain/ConflictType.java (DUPLICATE_CODE, OVERLAPPING_RULE, CONSTRAINT_VIOLATION)
- [ ] T017 Create DataSyncContext entity in backend/src/main/java/com/financial/ai/sync/domain/DataSyncContext.java with @Type(JsonType.class) for JSONB fields
- [ ] T018 Create SyncOperation entity in backend/src/main/java/com/financial/ai/sync/domain/SyncOperation.java with relationships to DataSyncContext
- [ ] T019 Create ConflictResolution entity in backend/src/main/java/com/financial/ai/sync/domain/ConflictResolution.java with JSONB resolution options

### Repositories

- [ ] T020 [P] Create DataSyncContextRepository in backend/src/main/java/com/financial/ai/sync/repository/DataSyncContextRepository.java extending JpaRepository
- [ ] T021 [P] Create SyncOperationRepository in backend/src/main/java/com/financial/ai/sync/repository/SyncOperationRepository.java with custom query methods
- [ ] T022 [P] Create ConflictResolutionRepository in backend/src/main/java/com/financial/ai/sync/repository/ConflictResolutionRepository.java

### Exception Classes

- [ ] T023 [P] Create ConflictException in backend/src/main/java/com/financial/ai/sync/exception/ConflictException.java with conflict details
- [ ] T024 [P] Create SyncFailedException in backend/src/main/java/com/financial/ai/sync/exception/SyncFailedException.java

---

## Phase 3: User Story 1 - AI Reads System Data (P1)

**Goal**: Enable AI to automatically load relevant system entities for conversation context

**Independent Test**: Create product with accounts/rules, start AI conversation, verify AI acknowledges existing data

**Story Dependencies**: None (foundational)

### DTOs for Context Loading

- [ ] T025 [P] [US1] Create LoadEntitiesRequest DTO in backend/src/main/java/com/financial/ai/sync/dto/LoadEntitiesRequest.java with @Valid annotations per api-spec.yaml
- [ ] T026 [P] [US1] Create LoadEntitiesResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/LoadEntitiesResponse.java
- [ ] T027 [P] [US1] Create SyncContextResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/SyncContextResponse.java with statistics object

### Context Management Service

- [ ] T028 [US1] Create ContextManagementService in backend/src/main/java/com/financial/ai/sync/service/ContextManagementService.java
- [ ] T029 [US1] Implement initializeSyncContext(sessionId) method to create DataSyncContext for new AI sessions
- [ ] T030 [US1] Implement getSyncContext(sessionId) method returning SyncContextResponse with loaded/created entity maps
- [ ] T031 [US1] Implement loadEntitiesForAI(request) method that queries ProductRepository, AccountRepository, AccountingRuleRepository based on filters
- [ ] T032 [US1] Implement recordLoadedEntities(sessionId, entityType, entityIds) to update DataSyncContext.loadedEntities JSONB field
- [ ] T033 [US1] Create SyncOperation records with operationType=READ for audit trail

### SystemDataService Enhancement

- [ ] T034 [US1] Add @Cacheable annotation to SystemDataService.buildContextForPhase() in backend/src/main/java/com/financial/ai/service/SystemDataService.java
- [ ] T035 [US1] Create CacheConfig class in backend/src/main/java/com/financial/config/CacheConfig.java with @EnableCaching and ConcurrentMapCacheManager
- [ ] T036 [US1] Add invalidateCache() method with @CacheEvict to SystemDataService

### REST Endpoints

- [ ] T037 [US1] Create DataSyncController in backend/src/main/java/com/financial/ai/sync/controller/DataSyncController.java with @RestController
- [ ] T038 [US1] Implement POST /api/ai/sync/context/{sessionId} endpoint to initialize sync context
- [ ] T039 [US1] Implement GET /api/ai/sync/context/{sessionId} endpoint to retrieve sync context
- [ ] T040 [US1] Implement POST /api/ai/sync/load endpoint to load entities for AI with @Valid request body
- [ ] T041 [US1] Add OpenAPI annotations (@Operation, @ApiResponse) to all endpoints per api-spec.yaml

### Integration

- [ ] T042 [US1] Update AIConversationService.buildSystemPrompt() in backend/src/main/java/com/financial/ai/service/AIConversationService.java to call ContextManagementService.loadEntitiesForAI()
- [ ] T043 [US1] Modify enrichPromptWithSystemData() to use cached SystemDataService results

---

## Phase 4: User Story 2 - AI Suggestions Auto-Persist (P1)

**Goal**: Automatically create/update system entities when user confirms AI suggestions

**Independent Test**: AI suggests account, user confirms, verify account appears in system immediately

**Story Dependencies**: Requires US1 (context loading) to be complete

### DTOs for Sync Operations

- [ ] T044 [P] [US2] Create EntitySuggestion DTO in backend/src/main/java/com/financial/ai/sync/dto/EntitySuggestion.java with entityType enum and data Map
- [ ] T045 [P] [US2] Create SyncSuggestionsRequest DTO in backend/src/main/java/com/financial/ai/sync/dto/SyncSuggestionsRequest.java with List<EntitySuggestion>
- [ ] T046 [P] [US2] Create SyncSuggestionsResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/SyncSuggestionsResponse.java
- [ ] T047 [P] [US2] Create SyncResult DTO in backend/src/main/java/com/financial/ai/sync/dto/SyncResult.java with status and entityUrl fields

### Entity Factory

- [ ] T048 [US2] Create EntityFactory utility in backend/src/main/java/com/financial/ai/sync/service/EntityFactory.java
- [ ] T049 [US2] Implement toProductRequest(EntitySuggestion) method converting suggestion data to ProductCreateRequest
- [ ] T050 [US2] Implement toScenarioRequest(EntitySuggestion) method converting to ScenarioCreateRequest
- [ ] T051 [US2] Implement toAccountRequest(EntitySuggestion) method converting to AccountCreateRequest
- [ ] T052 [US2] Implement toRuleRequest(EntitySuggestion) method converting to RuleCreateRequest
- [ ] T053 [US2] Implement toTransactionTypeRequest(EntitySuggestion) method converting to TransactionTypeCreateRequest

### Data Sync Service

- [ ] T054 [US2] Create DataSyncService in backend/src/main/java/com/financial/ai/sync/service/DataSyncService.java with @Transactional
- [ ] T055 [US2] Inject ProductService, ScenarioService, AccountService, AccountingRuleService, NumscriptGenerator
- [ ] T056 [US2] Implement syncSuggestions(request) method with atomic transaction handling
- [ ] T057 [US2] Implement createOrUpdateEntity(suggestion) switch statement delegating to appropriate service
- [ ] T058 [US2] Implement createRuleWithNumscript(suggestion) calling AccountingRuleService then NumscriptGenerator
- [ ] T059 [US2] Implement recordOperation(context, suggestion, entity, status) creating SyncOperation records
- [ ] T060 [US2] Implement updateEntityProvenance(entity, sessionId) setting ai_created=true and ai_session_id
- [ ] T061 [US2] Update DataSyncContext.createdEntities JSONB field after successful entity creation

### REST Endpoint

- [ ] T062 [US2] Implement POST /api/ai/sync/suggestions endpoint in DataSyncController calling DataSyncService.syncSuggestions()
- [ ] T063 [US2] Add exception handling for validation errors (400), conflicts (409), and sync failures (500)

---

## Phase 5: User Story 3 - Conflict Detection (P2)

**Goal**: Detect and present resolution options for duplicate/conflicting data

**Independent Test**: AI suggests existing account code, verify system detects conflict and offers resolution options

**Story Dependencies**: Requires US2 (auto-persist) to be complete

### DTOs for Conflicts

- [ ] T064 [P] [US3] Create ConflictResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/ConflictResponse.java per api-spec.yaml
- [ ] T065 [P] [US3] Create ConflictResolutionResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/ConflictResolutionResponse.java
- [ ] T066 [P] [US3] Create ResolveConflictRequest DTO in backend/src/main/java/com/financial/ai/sync/dto/ResolveConflictRequest.java

### Conflict Detection Service

- [ ] T067 [US3] Create ConflictDetectionService in backend/src/main/java/com/financial/ai/sync/service/ConflictDetectionService.java
- [ ] T068 [US3] Implement detectProductConflicts(request) checking ProductRepository.existsByCode()
- [ ] T069 [US3] Implement detectScenarioConflicts(request) checking ScenarioRepository.existsByProductIdAndCode()
- [ ] T070 [US3] Implement detectAccountConflicts(request) checking AccountRepository.existsByCode()
- [ ] T071 [US3] Implement detectRuleConflicts(request) with custom JPQL query for overlapping transaction type filters
- [ ] T072 [US3] Implement recordConflict(context, suggestion, conflictType, existingEntity) creating ConflictResolution record
- [ ] T073 [US3] Throw ConflictException with resolution options when conflict detected

### Integration with DataSyncService

- [ ] T074 [US3] Update DataSyncService.syncSuggestions() to call ConflictDetectionService before entity creation
- [ ] T075 [US3] Add try-catch for ConflictException, record conflict, and rethrow with HTTP 409 response

### Conflict Resolution Endpoints

- [ ] T076 [US3] Implement GET /api/ai/sync/conflicts/{sessionId} endpoint returning list of ConflictResolutionResponse
- [ ] T077 [US3] Implement POST /api/ai/sync/conflicts/{conflictId}/resolve endpoint accepting ResolveConflictRequest
- [ ] T078 [US3] Implement resolveConflict(conflictId, resolution) method in DataSyncService handling USE_EXISTING, CREATE_NEW, UPDATE_EXISTING
- [ ] T079 [US3] Update ConflictResolution record with chosenResolution and resolvedAt timestamp after user choice

---

## Phase 6: User Story 4 - Visual Sync Indicators (P3)

**Goal**: Show visual feedback when AI loads/writes data

**Independent Test**: Trigger data load, verify indicator shows "📖 Loaded: Product XYZ..."

**Story Dependencies**: Requires US2 (auto-persist) to be complete

### Frontend Store

- [ ] T080 [P] [US4] Create aiSyncStore.ts in frontend/stores/aiSyncStore.ts using Pinia
- [ ] T081 [P] [US4] Define state interface with isLoading, loadingMessage, lastSync, error fields
- [ ] T082 [P] [US4] Implement setLoading(isLoading, message) action
- [ ] T083 [P] [US4] Implement setSuccess(syncResult) action
- [ ] T084 [P] [US4] Implement setError(errorMessage) action

### Frontend Composable

- [ ] T085 [US4] Create useAISync.ts composable in frontend/composables/useAISync.ts
- [ ] T086 [US4] Implement syncSuggestion(sessionId, suggestion) function calling /api/ai/sync/suggestions
- [ ] T087 [US4] Add error handling for HTTP 409 conflicts showing modal and retrying with resolution
- [ ] T088 [US4] Implement loadContext(sessionId) function calling /api/ai/sync/context/{sessionId}
- [ ] T089 [US4] Return reactive syncState computed property from store

### UI Component

- [ ] T090 [US4] Create SyncStatusIndicator.vue component in frontend/components/ai/SyncStatusIndicator.vue
- [ ] T091 [US4] Add loading state UI with spinner and "📖 Loading: {message}" text
- [ ] T092 [US4] Add success state UI with "✅ Created: {entityType} {entityCode}" and clickable link to entityUrl
- [ ] T093 [US4] Add error state UI with "❌ Failed: {errorMessage}" and retry button
- [ ] T094 [US4] Integrate SyncStatusIndicator into existing ConversationPanel.vue component

---

## Phase 7: User Story 5 - Provenance Tracking (P3)

**Goal**: Track which entities were created/modified via AI with audit trail

**Independent Test**: Create account via AI, view account details, verify shows "Created via AI conversation [link]"

**Story Dependencies**: Requires US2 (auto-persist) to be complete

### Backend Endpoints

- [ ] T095 [P] [US5] Implement GET /api/ai/sync/operations/{sessionId} endpoint returning list of SyncOperationResponse
- [ ] T096 [P] [US5] Add query parameters for operationType and status filtering
- [ ] T097 [P] [US5] Create SyncOperationResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/SyncOperationResponse.java

### Rollback Functionality

- [ ] T098 [US5] Create RollbackResponse DTO in backend/src/main/java/com/financial/ai/sync/dto/RollbackResponse.java
- [ ] T099 [US5] Implement rollbackSession(sessionId) method in DataSyncService
- [ ] T100 [US5] Query DataSyncContext.createdEntities and delete each entity via appropriate service
- [ ] T101 [US5] Update SyncOperation records to status=ROLLED_BACK
- [ ] T102 [US5] Implement POST /api/ai/sync/rollback/{sessionId} endpoint in DataSyncController

### Frontend Provenance Display

- [ ] T103 [P] [US5] Add "AI Created" badge to entity list views (products, accounts, rules) when ai_created=true
- [ ] T104 [P] [US5] Add "Source: AI Conversation [link]" to entity detail views with link to conversation
- [ ] T105 [P] [US5] Create filter option "Show only AI-created" in entity list views

---

## Phase 8: Testing & Polish

**Goal**: Ensure quality and handle edge cases

### Unit Tests

- [ ] T106 [P] Create ConflictDetectionServiceTest in backend/src/test/java/com/financial/ai/sync/service/ConflictDetectionServiceTest.java
- [ ] T107 [P] Test detectProductConflicts() with existing and non-existing product codes
- [ ] T108 [P] Test detectRuleConflicts() with overlapping transaction type filters
- [ ] T109 [P] Create DataSyncServiceTest in backend/src/test/java/com/financial/ai/sync/service/DataSyncServiceTest.java
- [ ] T110 [P] Test syncSuggestions() creates entities and records operations
- [ ] T111 [P] Test atomic transaction rollback on partial failure
- [ ] T112 [P] Test AI provenance fields are set correctly (ai_created, ai_session_id)

### Integration Tests

- [ ] T113 [P] Create DataSyncControllerIntegrationTest in backend/src/test/java/com/financial/ai/sync/controller/DataSyncControllerIntegrationTest.java using @SpringBootTest
- [ ] T114 [P] Test POST /api/ai/sync/suggestions endpoint with valid request returns 200
- [ ] T115 [P] Test conflict detection returns 409 with ConflictResponse
- [ ] T116 [P] Test rollback endpoint deletes AI-created entities

### Edge Case Handling

- [ ] T117 Update DataSyncService to handle constraint violations (e.g., account code already in use) with clear error messages
- [ ] T118 Add validation for entity dependencies (e.g., rule references non-existent accounts) before creation
- [ ] T119 Implement optimistic locking retry logic for concurrent modifications with exponential backoff
- [ ] T120 Add session state validation (only allow rollback for ACTIVE sessions)

### Documentation & Deployment

- [ ] T121 [P] Update OpenAPI spec in api-specs/ directory with generated endpoints
- [ ] T122 [P] Add API documentation examples to Swagger UI
- [ ] T123 [P] Create integration test scenarios in quickstart.md
- [ ] T124 Verify all migrations run successfully on staging environment
- [ ] T125 Run performance benchmarks to confirm <500ms sync operations
- [ ] T126 Deploy Phase 3 (US1) to production as MVP

---

## Dependencies & Execution Order

### Story Dependencies
```
Setup (Phase 1-2)
    ↓
US1 (Phase 3) - AI Reads Data [MVP - Deploy First]
    ↓
US2 (Phase 4) - Auto-Persist [Deploy Second]
    ↓
US3 (Phase 5) - Conflict Detection [Deploy Third]
    ↓
US4 (Phase 6) - Visual Indicators [Parallel with US5]
    ↓
US5 (Phase 7) - Provenance Tracking [Parallel with US4]
    ↓
Testing & Polish (Phase 8)
```

### Critical Path
1. Database migration (T001-T011) - **BLOCKING**
2. Domain entities (T014-T019) - **BLOCKING**
3. Repositories (T020-T022) - **BLOCKING**
4. US1 Context Loading (T025-T043) - **MVP**
5. US2 Auto-Persist (T044-T063) - **Core Value**
6. US3 Conflict Detection (T064-T079) - **Data Integrity**
7. US4 & US5 in parallel - **Polish**

### Parallel Execution Examples

**Phase 1 (Setup)**: T005-T009 can run in parallel (different tables)

**Phase 2 (Foundational)**: T014-T016 (enums), T020-T022 (repositories), T023-T024 (exceptions) all parallelizable

**Phase 3 (US1)**: T025-T027 (DTOs) can run in parallel, then T028-T033 (service), then T037-T041 (controller)

**Phase 4 (US2)**: T044-T047 (DTOs) parallel, T048-T053 (factory) parallel, then T054-T061 (service)

**Phase 6 & 7**: US4 and US5 can be developed in parallel by different team members

**Phase 8 (Testing)**: T106-T116 all tests can run in parallel

---

## Task Summary

- **Total Tasks**: 126
- **Setup & Infrastructure**: 13 tasks
- **Foundational**: 11 tasks
- **User Story 1 (P1)**: 19 tasks
- **User Story 2 (P1)**: 20 tasks
- **User Story 3 (P2)**: 16 tasks
- **User Story 4 (P3)**: 15 tasks
- **User Story 5 (P3)**: 11 tasks
- **Testing & Polish**: 21 tasks

**Parallelizable Tasks**: 45 tasks marked with [P]

**Estimated Timeline**:
- MVP (US1): 5-7 days
- Core Feature (US1+US2): 10-12 days
- Full Feature (US1-US5): 18-22 days
- With Testing & Polish: 25-30 days

---

## Verification Checklist

After completing all tasks, verify:

- [ ] All database migrations applied successfully
- [ ] All OpenAPI endpoints documented and tested
- [ ] Integration tests passing (>95% coverage for sync services)
- [ ] Performance benchmarks met (<500ms sync operations)
- [ ] Conflict detection prevents 100% of duplicate entities
- [ ] AI provenance fields populated correctly for all AI-created entities
- [ ] Cache invalidation working (SystemDataService updates after sync)
- [ ] Rollback functionality tested and working
- [ ] Frontend indicators showing sync status correctly
- [ ] No data integrity violations in production testing

---

## Next Steps

1. Review task breakdown with team
2. Assign tasks to developers
3. Create GitHub issues from tasks (use task IDs as issue numbers)
4. Begin Phase 1 (database migration)
5. Deploy MVP (US1) after Phase 3 completion
6. Iterate based on user feedback

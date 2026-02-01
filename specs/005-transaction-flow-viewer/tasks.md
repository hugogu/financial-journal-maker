# Tasks: Transaction Flow Viewer

**Input**: Design documents from `/specs/005-transaction-flow-viewer/`  
**Prerequisites**: plan.md ✓, spec.md ✓, data-model.md ✓, contracts/ ✓, research.md ✓, quickstart.md ✓

**Tests**: No explicit test requirements in spec. Tests are NOT included in this task list.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/financial/`
- **Frontend**: `frontend/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependencies, and base structure

- [x] T001 Install frontend dependencies (@vue-flow/core, @vue-flow/background, dagre, prismjs) in frontend/package.json
- [x] T002 [P] Create backend package structure for transactionflow module in backend/src/main/java/com/financial/transactionflow/
- [x] T003 [P] Create frontend directory structure for flows and preview components in frontend/components/flows/ and frontend/components/preview/
- [x] T004 [P] Create Numscript Prism.js language definition plugin in frontend/plugins/prism-numscript.ts

---

## Phase 2: Foundational (Backend DTOs & Base Services)

**Purpose**: Core DTOs and services that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 [P] Create AccountType and AccountState enums in backend/src/main/java/com/financial/transactionflow/dto/AccountType.java and AccountState.java
- [x] T006 [P] Create FlowType enum (CASH, INFO) in backend/src/main/java/com/financial/transactionflow/dto/FlowType.java
- [x] T007 [P] Create Position record in backend/src/main/java/com/financial/transactionflow/dto/Position.java
- [x] T008 [P] Create AccountNodeDto in backend/src/main/java/com/financial/transactionflow/dto/AccountNodeDto.java
- [x] T009 [P] Create JournalEntryDisplayDto in backend/src/main/java/com/financial/transactionflow/dto/JournalEntryDisplayDto.java
- [x] T010 [P] Create FlowConnectionDto in backend/src/main/java/com/financial/transactionflow/dto/FlowConnectionDto.java
- [x] T011 [P] Create ProductSummary DTO in backend/src/main/java/com/financial/transactionflow/dto/ProductSummary.java
- [x] T012 [P] Create ScenarioSummary DTO in backend/src/main/java/com/financial/transactionflow/dto/ScenarioSummary.java
- [x] T013 [P] Create TransactionFlowSummary DTO in backend/src/main/java/com/financial/transactionflow/dto/TransactionFlowSummary.java
- [x] T014 Create TransactionFlowView DTO (depends on T008-T010) in backend/src/main/java/com/financial/transactionflow/dto/TransactionFlowView.java
- [x] T015 Create base TransactionFlowService with DesignDecision repository injection in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T016 [P] Create useTransactionFlows composable with base API methods in frontend/composables/useTransactionFlows.ts

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Browse Transaction Flows (Priority: P1) 🎯 MVP

**Goal**: Users can browse Products → Scenarios → Transaction Types hierarchy and see all transaction flows

**Independent Test**: Navigate to /flows, see product list, drill into scenarios, view transaction type list with search/filter

### Implementation for User Story 1

- [x] T017 [US1] Implement listProducts query in TransactionFlowService extracting from DesignDecision in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T018 [US1] Implement getProduct and listScenarios queries in TransactionFlowService in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T019 [US1] Implement listAllTransactionFlows with filters in TransactionFlowService in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T020 [US1] Create TransactionFlowController with products endpoints (list, get, scenarios) in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T021 [US1] Add transaction-flows list endpoint to TransactionFlowController in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T022 [P] [US1] Create ProductScenarioTree component with expand/collapse in frontend/components/flows/ProductScenarioTree.vue
- [x] T023 [P] [US1] Create TransactionFlowList component with table and summary counts in frontend/components/flows/TransactionFlowList.vue
- [x] T024 [US1] Extend useTransactionFlows with products and scenarios API calls in frontend/composables/useTransactionFlows.ts
- [x] T025 [US1] Create Transaction Flow Browser page with tree + list layout in frontend/pages/flows/index.vue
- [x] T026 [US1] Add search/filter functionality to Browser page in frontend/pages/flows/index.vue
- [x] T027 [US1] Add navigation link to flows in main navigation in frontend/layouts/default.vue or frontend/components/AppNavigation.vue

**Checkpoint**: User Story 1 complete - users can browse all transaction flows hierarchically

---

## Phase 4: User Story 2 - View Transaction Details (Priority: P1)

**Goal**: Users can view accounts and journal entries for a selected transaction type

**Independent Test**: Click any transaction type, see accounts table and journal entries grouped by trigger event

### Implementation for User Story 2

- [x] T028 [US2] Implement getTransactionFlow in TransactionFlowService extracting full details in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T029 [US2] Add getTransactionFlow endpoint to TransactionFlowController in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T030 [P] [US2] Create AccountsTable component showing code, name, type, state in frontend/components/flows/AccountsTable.vue
- [x] T031 [P] [US2] Create JournalEntriesTable component with DR/CR, grouping by trigger event in frontend/components/flows/JournalEntriesTable.vue
- [x] T032 [US2] Extend useTransactionFlows with getTransactionFlow API call in frontend/composables/useTransactionFlows.ts
- [x] T033 [US2] Create Transaction Flow Detail page with tabs structure in frontend/pages/flows/[code].vue
- [x] T034 [US2] Implement Accounts tab content in detail page in frontend/pages/flows/[code].vue
- [x] T035 [US2] Implement Journal Entries tab content in detail page in frontend/pages/flows/[code].vue

**Checkpoint**: User Story 2 complete - users can view full accounting structure for any transaction

---

## Phase 5: User Story 3 - Numscript View (Priority: P1)

**Goal**: Users can view syntax-highlighted Numscript DSL and copy to clipboard

**Independent Test**: Open Numscript tab, see highlighted code, click Copy button, paste elsewhere

### Implementation for User Story 3

- [x] T036 [P] [US3] Create NumscriptViewDto in backend/src/main/java/com/financial/transactionflow/dto/NumscriptViewDto.java
- [x] T037 [US3] Implement getNumscript in TransactionFlowService (read from ExportArtifact or generate) in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T038 [US3] Add getTransactionNumscript endpoint to TransactionFlowController in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T039 [US3] Create NumscriptViewer component with Prism highlighting and copy button in frontend/components/flows/NumscriptViewer.vue
- [x] T040 [US3] Extend useTransactionFlows with getNumscript API call in frontend/composables/useTransactionFlows.ts
- [x] T041 [US3] Implement Numscript tab in detail page using NumscriptViewer in frontend/pages/flows/[code].vue
- [x] T042 [US3] Add validation error display to NumscriptViewer when numscriptValid is false in frontend/components/flows/NumscriptViewer.vue

**Checkpoint**: User Story 3 complete - users can view and copy Numscript for any transaction

---

## Phase 6: User Story 4 - Real-time Preview (Priority: P1)

**Goal**: During AI session, side panel shows current design state with real-time updates

**Independent Test**: Start AI session, describe transaction, preview panel updates showing accounts/entries as AI responds

### Implementation for User Story 4

- [x] T043 [P] [US4] Create DesignPreviewDto in backend/src/main/java/com/financial/transactionflow/dto/DesignPreviewDto.java
- [x] T044 [US4] Create PreviewService to extract preview state from session in backend/src/main/java/com/financial/transactionflow/service/PreviewService.java
- [x] T045 [US4] Implement SSE stream for preview updates in PreviewService in backend/src/main/java/com/financial/transactionflow/service/PreviewService.java
- [x] T046 [US4] Create PreviewController with getSessionPreview and streamSessionPreview endpoints in backend/src/main/java/com/financial/transactionflow/controller/PreviewController.java
- [x] T047 [P] [US4] Create AccountsList component for preview panel in frontend/components/preview/AccountsList.vue
- [x] T048 [P] [US4] Create EntriesSummary component for preview panel in frontend/components/preview/EntriesSummary.vue
- [ ] T049 [P] [US4] Create MiniFlowDiagram component (simplified flow preview) in frontend/components/preview/MiniFlowDiagram.vue
- [x] T050 [US4] Create DesignPreviewPanel component with collapsible layout in frontend/components/preview/DesignPreviewPanel.vue
- [x] T051 [US4] Create useDesignPreview composable with SSE connection in frontend/composables/useDesignPreview.ts
- [ ] T052 [US4] Integrate DesignPreviewPanel into AI session page with resizable panel in frontend/pages/analysis/[id].vue
- [ ] T053 [US4] Add panel size persistence using localStorage in frontend/composables/useDesignPreview.ts
- [x] T054 [US4] Style confirmed vs tentative elements differently in preview components in frontend/components/preview/AccountsList.vue and EntriesSummary.vue

**Checkpoint**: User Story 4 complete - analysts see real-time preview during AI conversations

---

## Phase 7: User Story 5 - Flow Diagram (Priority: P2)

**Goal**: Interactive node-based diagram showing cash/info flows with color-coded accounts

**Independent Test**: Open Flow Diagram tab, see nodes colored by type, arrows showing flows, zoom/pan works

### Implementation for User Story 5

- [x] T055 [P] [US5] Create FlowDiagramData DTO in backend/src/main/java/com/financial/transactionflow/dto/FlowDiagramData.java
- [x] T056 [US5] Create FlowDiagramService with dagre-based layout computation in backend/src/main/java/com/financial/transactionflow/service/FlowDiagramService.java
- [x] T057 [US5] Add getFlowDiagram endpoint to TransactionFlowController in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T058 [US5] Create AccountNode Vue Flow custom node component with type-based colors in frontend/components/flows/AccountNode.vue
- [x] T059 [US5] Create FlowDiagram component using Vue Flow with dagre layout in frontend/components/flows/FlowDiagram.vue
- [x] T060 [US5] Add solid/dashed edge styles for CASH/INFO flows in FlowDiagram in frontend/components/flows/FlowDiagram.vue
- [x] T061 [US5] Add zoom controls and pan functionality to FlowDiagram in frontend/components/flows/FlowDiagram.vue
- [x] T062 [US5] Add hover tooltips showing flow details on edges in frontend/components/flows/FlowDiagram.vue
- [x] T063 [US5] Extend useTransactionFlows with getFlowDiagram API call in frontend/composables/useTransactionFlows.ts
- [x] T064 [US5] Implement Flow Diagram tab in detail page in frontend/pages/flows/[code].vue
- [x] T065 [US5] Add account state visual distinction (solid/dotted/dashed borders) in AccountNode in frontend/components/flows/AccountNode.vue

**Checkpoint**: User Story 5 complete - users can visualize transaction flows graphically

---

## Phase 8: User Story 6 - Timeline View (Priority: P3)

**Goal**: Timeline diagram showing when accounting events occur (T+0, T+1, etc.)

**Independent Test**: View transaction with multi-day settlement, see timeline with T+0/T+1 markers

### Implementation for User Story 6

- [x] T066 [P] [US6] Create TransactionTimelineDto and TimelineEventDto in backend/src/main/java/com/financial/transactionflow/dto/TransactionTimelineDto.java
- [x] T067 [US6] Implement getTransactionTimeline in TransactionFlowService in backend/src/main/java/com/financial/transactionflow/service/TransactionFlowService.java
- [x] T068 [US6] Add getTransactionTimeline endpoint to TransactionFlowController in backend/src/main/java/com/financial/transactionflow/controller/TransactionFlowController.java
- [x] T069 [US6] Create TransactionTimeline component with horizontal time axis in frontend/components/flows/TransactionTimeline.vue
- [x] T070 [US6] Add click-to-highlight linking timeline events to journal entries in TransactionTimeline in frontend/components/flows/TransactionTimeline.vue
- [x] T071 [US6] Extend useTransactionFlows with getTimeline API call in frontend/composables/useTransactionFlows.ts
- [ ] T072 [US6] Implement Timeline tab in detail page (conditionally shown for multi-timing transactions) in frontend/pages/flows/[code].vue

**Checkpoint**: User Story 6 complete - users can understand settlement timing

---

## Phase 9: User Story 7 - Source Session Link (Priority: P3)

**Goal**: Navigate from transaction view back to the AI session that created it

**Independent Test**: View transaction, click "View Source Session", navigate to archived session

### Implementation for User Story 7

- [x] T073 [US7] Add source session link UI to transaction detail header in frontend/pages/flows/[code].vue
- [x] T074 [US7] Ensure sourceSessionId is included in TransactionFlowView response in backend/src/main/java/com/financial/transactionflow/dto/TransactionFlowView.java
- [x] T075 [US7] Implement navigation to /analysis/{sessionId} with read-only mode indicator in frontend/pages/flows/[code].vue

**Checkpoint**: User Story 7 complete - full traceability to design rationale

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements affecting multiple user stories

- [ ] T076 [P] Add loading states and error handling to all frontend pages in frontend/pages/flows/
- [ ] T077 [P] Add empty state displays (no data placeholders) to list and detail components in frontend/components/flows/
- [ ] T078 Handle edge case: transaction with no entries (show placeholder with source session link) in frontend/pages/flows/[code].vue
- [ ] T079 Handle edge case: Numscript validation errors (inline display) in frontend/components/flows/NumscriptViewer.vue
- [ ] T080 Handle edge case: complex diagrams >10 accounts (add collapse/expand groups) in frontend/components/flows/FlowDiagram.vue
- [x] T081 [P] Add OpenAPI documentation annotations to all controller endpoints in backend/src/main/java/com/financial/transactionflow/controller/
- [ ] T082 Run quickstart.md validation checklist to verify all functionality

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - US1-US4 are all P1 priority but have some dependencies (see below)
  - US5-US7 can proceed after foundational
- **Polish (Phase 10)**: Depends on all user stories being complete

### User Story Dependencies

| Story | Can Start After | Notes |
|-------|-----------------|-------|
| US1 (Browse) | Foundational | No dependencies on other stories |
| US2 (Details) | Foundational | Shares detail page with US1 navigation |
| US3 (Numscript) | Foundational | Can develop in parallel, integrates into US2 detail page |
| US4 (Preview) | Foundational | Independent of browsing features, integrates with 004 |
| US5 (Diagram) | Foundational | Can develop in parallel, integrates into US2 detail page |
| US6 (Timeline) | Foundational | Low priority, can be deferred |
| US7 (Link) | US2 | Simple addition to detail page |

### Within Each User Story

1. Backend DTOs first (if any new ones)
2. Backend service methods
3. Backend controller endpoints
4. Frontend composable extensions
5. Frontend components
6. Frontend page integration

### Parallel Opportunities

**Phase 1 (Setup)**:
```
T001, T002, T003, T004 - all can run in parallel
```

**Phase 2 (Foundational)**:
```
T005, T006, T007, T008, T009, T010, T011, T012, T013 - all DTOs in parallel
Then: T014, T015, T016
```

**Phase 3-9 (User Stories)**:
```
After Foundational completes:
- Developer A: US1 (Browse) → US7 (Link)
- Developer B: US2 (Details) → US3 (Numscript)
- Developer C: US4 (Preview)
- Developer D: US5 (Diagram) → US6 (Timeline)
```

---

## Parallel Example: User Story 5 (Flow Diagram)

```bash
# These can run in parallel:
T055: Create FlowDiagramData DTO
T058: Create AccountNode component

# After DTOs ready, these can run in parallel:
T059: Create FlowDiagram component
T063: Extend useTransactionFlows composable

# Then sequential:
T056 → T057 → T060 → T061 → T062 → T064 → T065
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1 (Browse)
4. Complete Phase 4: User Story 2 (Details)
5. **STOP and VALIDATE**: Can browse and view transaction details
6. Deploy/demo - users can browse accounting designs

### Incremental Delivery

| Increment | Stories | Value Delivered |
|-----------|---------|-----------------|
| MVP | US1 + US2 | Browse and view accounting structures |
| +Numscript | US3 | Developers can access executable code |
| +Preview | US4 | Real-time feedback during AI sessions |
| +Diagram | US5 | Visual understanding of flows |
| +Timeline | US6 | Settlement timing visibility |
| +Link | US7 | Full audit trail |

### Recommended Sequence for Solo Developer

1. Phase 1 → Phase 2 (foundation)
2. US1 → US2 (core browsing - MVP)
3. US3 (Numscript - high value add)
4. US4 (Preview - critical for AI workflow)
5. US5 (Diagram - visual impact)
6. US6, US7 (lower priority)
7. Phase 10 (polish)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- This feature is READ-ONLY - no database migrations required
- Integrates with existing 004-ai-analysis-session DesignDecision/ExportArtifact entities
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently

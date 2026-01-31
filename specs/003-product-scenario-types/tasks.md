# Tasks: Product/Scenario/TransactionType Management

**Input**: Design documents from `/specs/003-product-scenario-types/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/openapi.yaml ✅

**Tests**: Integration tests included (following existing project pattern from AccountingRule module)

**Organization**: Tasks grouped by user story (US1-US5) to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4, US5)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/financial/domain/`
- **Tests**: `backend/src/test/java/com/financial/domain/`
- **Migrations**: `backend/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and shared components

- [ ] T001 Create database migration `backend/src/main/resources/db/migration/V003__create_product_scenario_types.sql`
- [ ] T002 [P] Create EntityStatus enum in `backend/src/main/java/com/financial/domain/domain/EntityStatus.java`
- [ ] T003 [P] Create domain exception classes in `backend/src/main/java/com/financial/domain/exception/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities and repositories that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 [P] Create Product entity in `backend/src/main/java/com/financial/domain/domain/Product.java`
- [ ] T005 [P] Create Scenario entity in `backend/src/main/java/com/financial/domain/domain/Scenario.java`
- [ ] T006 [P] Create TransactionType entity in `backend/src/main/java/com/financial/domain/domain/TransactionType.java`
- [ ] T007 [P] Create TransactionTypeRule entity in `backend/src/main/java/com/financial/domain/domain/TransactionTypeRule.java`
- [ ] T008 [P] Create ProductRepository in `backend/src/main/java/com/financial/domain/repository/ProductRepository.java`
- [ ] T009 [P] Create ScenarioRepository in `backend/src/main/java/com/financial/domain/repository/ScenarioRepository.java`
- [ ] T010 [P] Create TransactionTypeRepository in `backend/src/main/java/com/financial/domain/repository/TransactionTypeRepository.java`
- [ ] T011 [P] Create TransactionTypeRuleRepository in `backend/src/main/java/com/financial/domain/repository/TransactionTypeRuleRepository.java`
- [ ] T012 Update CoaApplication.java to scan `com.financial.domain` package

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - 创建和管理产品 (Priority: P1) 🎯 MVP

**Goal**: CRUD operations for Product entity with lifecycle state management

**Independent Test**: Create, view, update, delete products; activate/archive/restore lifecycle

### DTOs for User Story 1

- [ ] T013 [P] [US1] Create ProductCreateRequest DTO in `backend/src/main/java/com/financial/domain/dto/ProductCreateRequest.java`
- [ ] T014 [P] [US1] Create ProductUpdateRequest DTO in `backend/src/main/java/com/financial/domain/dto/ProductUpdateRequest.java`
- [ ] T015 [P] [US1] Create ProductResponse DTO in `backend/src/main/java/com/financial/domain/dto/ProductResponse.java`

### Service for User Story 1

- [ ] T016 [US1] Implement ProductService in `backend/src/main/java/com/financial/domain/service/ProductService.java`
  - createProduct, getProduct, getProductByCode, listProducts
  - updateProduct, deleteProduct (with child check)
  - activateProduct, archiveProduct, restoreProduct

### Controller for User Story 1

- [ ] T017 [US1] Implement ProductController in `backend/src/main/java/com/financial/domain/controller/ProductController.java`
  - POST /products, GET /products, GET /products/{id}, GET /products/code/{code}
  - PUT /products/{id}, DELETE /products/{id}
  - POST /products/{id}/activate, /archive, /restore

### Tests for User Story 1

- [ ] T018 [US1] Create ProductControllerIntegrationTest in `backend/src/test/java/com/financial/domain/controller/ProductControllerIntegrationTest.java`

**Checkpoint**: Product CRUD with lifecycle fully functional and testable

---

## Phase 4: User Story 2 - 创建和管理场景 (Priority: P1)

**Goal**: CRUD operations for Scenario entity under Product

**Independent Test**: Create scenarios under products, view, update, delete with lifecycle

### DTOs for User Story 2

- [ ] T019 [P] [US2] Create ScenarioCreateRequest DTO in `backend/src/main/java/com/financial/domain/dto/ScenarioCreateRequest.java`
- [ ] T020 [P] [US2] Create ScenarioUpdateRequest DTO in `backend/src/main/java/com/financial/domain/dto/ScenarioUpdateRequest.java`
- [ ] T021 [P] [US2] Create ScenarioResponse DTO in `backend/src/main/java/com/financial/domain/dto/ScenarioResponse.java`

### Service for User Story 2

- [ ] T022 [US2] Implement ScenarioService in `backend/src/main/java/com/financial/domain/service/ScenarioService.java`
  - createScenario (check parent not ARCHIVED), getScenario, listScenarios
  - updateScenario, deleteScenario (with child check)
  - activateScenario, archiveScenario, restoreScenario

### Controller for User Story 2

- [ ] T023 [US2] Implement ScenarioController in `backend/src/main/java/com/financial/domain/controller/ScenarioController.java`
  - POST /scenarios, GET /scenarios, GET /scenarios/{id}
  - PUT /scenarios/{id}, DELETE /scenarios/{id}
  - POST /scenarios/{id}/activate, /archive, /restore

### Tests for User Story 2

- [ ] T024 [US2] Create ScenarioControllerIntegrationTest in `backend/src/test/java/com/financial/domain/controller/ScenarioControllerIntegrationTest.java`

**Checkpoint**: Scenario CRUD with lifecycle fully functional

---

## Phase 5: User Story 3 - 创建和管理交易类型 (Priority: P1)

**Goal**: CRUD for TransactionType with rule association capability

**Independent Test**: Create types under scenarios, associate/dissociate rules, lifecycle management

### DTOs for User Story 3

- [ ] T025 [P] [US3] Create TransactionTypeCreateRequest DTO in `backend/src/main/java/com/financial/domain/dto/TransactionTypeCreateRequest.java`
- [ ] T026 [P] [US3] Create TransactionTypeUpdateRequest DTO in `backend/src/main/java/com/financial/domain/dto/TransactionTypeUpdateRequest.java`
- [ ] T027 [P] [US3] Create TransactionTypeResponse DTO in `backend/src/main/java/com/financial/domain/dto/TransactionTypeResponse.java`
- [ ] T028 [P] [US3] Create RuleAssociationRequest DTO in `backend/src/main/java/com/financial/domain/dto/RuleAssociationRequest.java`
- [ ] T029 [P] [US3] Create RuleAssociation DTO in `backend/src/main/java/com/financial/domain/dto/RuleAssociation.java`

### Service for User Story 3

- [ ] T030 [US3] Implement TransactionTypeService in `backend/src/main/java/com/financial/domain/service/TransactionTypeService.java`
  - createTransactionType (check parent not ARCHIVED)
  - getTransactionType, listTransactionTypes
  - updateTransactionType, deleteTransactionType (cascade remove associations)
  - activateTransactionType, archiveTransactionType, restoreTransactionType
  - addRuleAssociation (validate rule status not ARCHIVED)
  - removeRuleAssociation, getRuleAssociations

### Controller for User Story 3

- [ ] T031 [US3] Implement TransactionTypeController in `backend/src/main/java/com/financial/domain/controller/TransactionTypeController.java`
  - POST /transaction-types, GET /transaction-types, GET /transaction-types/{id}
  - PUT /transaction-types/{id}, DELETE /transaction-types/{id}
  - POST /transaction-types/{id}/activate, /archive, /restore
  - GET /transaction-types/{id}/rules, POST /transaction-types/{id}/rules
  - DELETE /transaction-types/{id}/rules/{ruleId}

### Tests for User Story 3

- [ ] T032 [US3] Create TransactionTypeControllerIntegrationTest in `backend/src/test/java/com/financial/domain/controller/TransactionTypeControllerIntegrationTest.java`

**Checkpoint**: TransactionType CRUD with rule associations fully functional

---

## Phase 6: User Story 4 - 层级导航和关联查询 (Priority: P2)

**Goal**: Hierarchical tree view and aggregate queries for rules/accounts

**Independent Test**: Get product tree, query aggregated rules and accounts per hierarchy level

### DTOs for User Story 4

- [ ] T033 [P] [US4] Create ProductTreeResponse DTO in `backend/src/main/java/com/financial/domain/dto/ProductTreeResponse.java`
- [ ] T034 [P] [US4] Create ScenarioTreeNode DTO in `backend/src/main/java/com/financial/domain/dto/ScenarioTreeNode.java`
- [ ] T035 [P] [US4] Create TransactionTypeTreeNode DTO in `backend/src/main/java/com/financial/domain/dto/TransactionTypeTreeNode.java`
- [ ] T036 [P] [US4] Create RuleSummary DTO in `backend/src/main/java/com/financial/domain/dto/RuleSummary.java`
- [ ] T037 [P] [US4] Create AccountSummary DTO in `backend/src/main/java/com/financial/domain/dto/AccountSummary.java`

### Service for User Story 4

- [ ] T038 [US4] Implement HierarchyService in `backend/src/main/java/com/financial/domain/service/HierarchyService.java`
  - getProductTree(productId) - returns full hierarchy
  - getProductRules(productId) - aggregate rules from all types
  - getProductAccounts(productId) - aggregate accounts from rules
  - getScenarioRules(scenarioId)
  - getScenarioAccounts(scenarioId)

### Controller Additions for User Story 4

- [ ] T039 [US4] Add tree and aggregate endpoints to ProductController
  - GET /products/{id}/tree
  - GET /products/{id}/rules
  - GET /products/{id}/accounts

- [ ] T040 [US4] Add aggregate endpoints to ScenarioController
  - GET /scenarios/{id}/rules
  - GET /scenarios/{id}/accounts

### Tests for User Story 4

- [ ] T041 [US4] Add hierarchy tests to existing integration tests

**Checkpoint**: Hierarchical navigation and aggregate queries functional

---

## Phase 7: User Story 5 - 模板复用 (Priority: P3)

**Goal**: Clone products and scenarios with structure (not rule associations)

**Independent Test**: Clone product with all children, clone scenario to same/different product

### DTOs for User Story 5

- [ ] T042 [P] [US5] Create CloneRequest DTO in `backend/src/main/java/com/financial/domain/dto/CloneRequest.java`
- [ ] T043 [P] [US5] Create ScenarioCloneRequest DTO in `backend/src/main/java/com/financial/domain/dto/ScenarioCloneRequest.java`

### Service for User Story 5

- [ ] T044 [US5] Add cloneProduct to ProductService
  - Deep copy product → scenarios → types
  - Generate unique code with -copy-N suffix
  - Do NOT copy rule associations

- [ ] T045 [US5] Add cloneScenario to ScenarioService
  - Copy scenario → types to same or different product
  - Generate unique code with -copy-N suffix
  - Do NOT copy rule associations

### Controller Additions for User Story 5

- [ ] T046 [US5] Add clone endpoints to ProductController
  - POST /products/{id}/clone

- [ ] T047 [US5] Add clone endpoints to ScenarioController
  - POST /scenarios/{id}/clone

### Tests for User Story 5

- [ ] T048 [US5] Add clone tests to integration tests

**Checkpoint**: Template cloning fully functional

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final integration and quality improvements

- [ ] T049 [P] Update OpenAPI/Swagger annotations on all controllers
- [ ] T050 [P] Add comprehensive logging to all services
- [ ] T051 Run full integration test suite and fix any failures
- [ ] T052 Validate against quickstart.md examples
- [ ] T053 Update README-STARTUP.md with new API endpoints

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) ────────────────────────────────────┐
                                                    │
Phase 2 (Foundational) ─────────────────────────────┤
                                                    │
         ┌──────────────────────────────────────────┴──────────────────────────────────────┐
         │                                                                                 │
         ▼                              ▼                              ▼                   │
Phase 3 (US1: Product)    Phase 4 (US2: Scenario)    Phase 5 (US3: TransactionType)       │
         │                              │                              │                   │
         └──────────────────────────────┴──────────────────────────────┘                   │
                                        │                                                  │
                                        ▼                                                  │
                          Phase 6 (US4: Hierarchy) ────────────────────────────────────────┤
                                        │                                                  │
                                        ▼                                                  │
                          Phase 7 (US5: Clone) ────────────────────────────────────────────┘
                                        │
                                        ▼
                          Phase 8 (Polish)
```

### User Story Dependencies

| Story | Can Start After | Dependencies |
|-------|-----------------|--------------|
| US1 (Product) | Phase 2 | None - MVP entry point |
| US2 (Scenario) | Phase 2 | Can run parallel to US1, but needs Product for testing |
| US3 (TransactionType) | Phase 2 | Can run parallel, needs Scenario for testing |
| US4 (Hierarchy) | US1, US2, US3 | Needs all entities to query hierarchies |
| US5 (Clone) | US1, US2, US3 | Needs all entities to clone |

### Parallel Opportunities

**Within Phase 2 (Foundational)**:
- T004-T011 (all entities and repositories) can run in parallel

**Within Phase 3-5 (Core User Stories)**:
- All DTO tasks within a story can run in parallel
- US1, US2, US3 can be developed in parallel by different developers

**Within Phase 6-7**:
- All DTO tasks can run in parallel

---

## Parallel Example: Phase 2 (Foundational)

```bash
# All these can run simultaneously:
Task T004: Create Product entity
Task T005: Create Scenario entity
Task T006: Create TransactionType entity
Task T007: Create TransactionTypeRule entity
Task T008: Create ProductRepository
Task T009: Create ScenarioRepository
Task T010: Create TransactionTypeRepository
Task T011: Create TransactionTypeRuleRepository
```

## Parallel Example: User Story 1

```bash
# Launch all DTOs together:
Task T013: ProductCreateRequest DTO
Task T014: ProductUpdateRequest DTO
Task T015: ProductResponse DTO

# Then sequentially:
Task T016: ProductService (depends on DTOs)
Task T017: ProductController (depends on Service)
Task T018: Integration Tests (depends on Controller)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (migration, enums, exceptions)
2. Complete Phase 2: Foundational (entities, repositories)
3. Complete Phase 3: User Story 1 (Product CRUD)
4. **STOP and VALIDATE**: Test Product API independently
5. Deploy/demo if ready - basic product management works

### Incremental Delivery

| Milestone | Stories Included | Value Delivered |
|-----------|------------------|-----------------|
| MVP | US1 | Product management |
| v0.2 | US1 + US2 | Product + Scenario management |
| v0.3 | US1 + US2 + US3 | Full entity hierarchy with rule associations |
| v0.4 | + US4 | Hierarchical navigation and queries |
| v1.0 | + US5 | Template cloning for efficiency |

### Suggested Execution Order (Single Developer)

1. T001-T003 (Setup) → T004-T012 (Foundational) → **Checkpoint: DB ready**
2. T013-T018 (US1: Product) → **Checkpoint: MVP ready**
3. T019-T024 (US2: Scenario) → **Checkpoint: Two-level hierarchy**
4. T025-T032 (US3: TransactionType) → **Checkpoint: Full hierarchy**
5. T033-T041 (US4: Hierarchy) → **Checkpoint: Navigation ready**
6. T042-T048 (US5: Clone) → **Checkpoint: Reuse ready**
7. T049-T053 (Polish) → **Checkpoint: Production ready**

---

## Task Summary

| Phase | Tasks | Parallel | Description |
|-------|-------|----------|-------------|
| Phase 1 | 3 | 2 | Setup |
| Phase 2 | 9 | 8 | Foundational |
| Phase 3 (US1) | 6 | 3 | Product CRUD |
| Phase 4 (US2) | 6 | 3 | Scenario CRUD |
| Phase 5 (US3) | 8 | 5 | TransactionType + Rules |
| Phase 6 (US4) | 9 | 5 | Hierarchy Navigation |
| Phase 7 (US5) | 7 | 2 | Clone/Copy |
| Phase 8 | 5 | 2 | Polish |
| **Total** | **53** | **30** | |

---

## Notes

- All [P] tasks can run in parallel (different files, no dependencies)
- [Story] label maps task to specific user story for traceability
- Each user story checkpoint validates independent functionality
- Commit after each task or logical group
- Follow existing patterns from `com.financial.rules` and `com.financial.coa` packages

# Tasks: Accounting Rules Management

**Input**: Design documents from `/specs/002-accounting-rules/`  
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/ ✓

**Tests**: Not explicitly requested in spec. Tests are NOT included in this task list.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Task Count Summary

| Phase | Tasks | User Story |
|-------|-------|------------|
| Phase 1: Setup | 5 | - |
| Phase 2: Foundational | 8 | - |
| Phase 3: US1+US2 MVP | 16 | Rule CRUD + Entry Templates |
| Phase 4: US3 | 6 | Trigger Conditions |
| Phase 5: US4+US5 | 8 | Numscript Generation + Validation |
| Phase 6: US6 | 6 | Rule Simulation |
| Phase 7: US7 | 5 | Cross-Scenario Reuse |
| Phase 8: Polish | 8 | - |
| **Total** | **62** | |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization for rules module

- [x] T001 Create rules module package structure in backend/src/main/java/com/financial/rules/ with subpackages: domain, repository, dto, service, controller, exception
- [x] T002 [P] Create Flyway migration V2__create_rules_tables.sql in backend/src/main/resources/db/migration/ with schema from data-model.md
- [x] T003 [P] Create RulesException base class in backend/src/main/java/com/financial/rules/exception/RulesException.java
- [x] T004 [P] Create domain-specific exceptions in backend/src/main/java/com/financial/rules/exception/: RuleNotFoundException, RuleValidationException, ExpressionParseException, InvalidStateTransitionException
- [x] T005 [P] Add rules exception handling to GlobalExceptionHandler in backend/src/main/java/com/financial/coa/exception/GlobalExceptionHandler.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

### Domain Models (Core Entities)

- [x] T006 Create AccountingRule entity in backend/src/main/java/com/financial/rules/domain/AccountingRule.java with fields: id, code, name, description, status (enum), sharedAcrossScenarios, currentVersion, version (optimistic), timestamps, audit fields
- [x] T007 [P] Create RuleStatus enum in backend/src/main/java/com/financial/rules/domain/RuleStatus.java with values: DRAFT, ACTIVE, ARCHIVED
- [x] T008 [P] Create AccountingRuleVersion entity in backend/src/main/java/com/financial/rules/domain/AccountingRuleVersion.java with fields: id, ruleId, versionNumber, snapshotJson, changeDescription, createdAt, createdBy
- [x] T009 [P] Create EntryTemplate entity in backend/src/main/java/com/financial/rules/domain/EntryTemplate.java with fields: id, ruleId (unique), description, variableSchemaJson, timestamps
- [x] T010 [P] Create EntryLine entity in backend/src/main/java/com/financial/rules/domain/EntryLine.java with fields: id, templateId, sequenceNumber, accountCode, entryType (enum), amountExpression, memoTemplate, timestamps
- [x] T011 [P] Create EntryType enum in backend/src/main/java/com/financial/rules/domain/EntryType.java with values: DEBIT, CREDIT
- [x] T012 [P] Create TriggerCondition entity in backend/src/main/java/com/financial/rules/domain/TriggerCondition.java with fields: id, ruleId, conditionJson, description, timestamps

### Repositories

- [x] T013 Create AccountingRuleRepository in backend/src/main/java/com/financial/rules/repository/AccountingRuleRepository.java with methods: findByCode, findByStatus, existsByCode, findBySharedAcrossScenariosTrue

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1+2 - Rule CRUD + Entry Templates (Priority: P1) 🎯 MVP

**Goal**: Create, read, update, delete accounting rules with entry templates containing debit/credit lines and amount expressions

**Independent Test**: Create a rule with entry template containing multiple lines with expressions, verify CRUD operations work, verify version tracking on updates

### Repositories for US1+US2

- [x] T014 [P] [US1] Create AccountingRuleVersionRepository in backend/src/main/java/com/financial/rules/repository/AccountingRuleVersionRepository.java with methods: findByRuleIdOrderByVersionNumberDesc, findByRuleIdAndVersionNumber
- [x] T015 [P] [US2] Create EntryTemplateRepository in backend/src/main/java/com/financial/rules/repository/EntryTemplateRepository.java with methods: findByRuleId
- [x] T016 [P] [US2] Create EntryLineRepository in backend/src/main/java/com/financial/rules/repository/EntryLineRepository.java with methods: findByTemplateIdOrderBySequenceNumber, deleteByTemplateId

### DTOs for US1+US2

- [x] T017 [P] [US1] Create RuleCreateRequest DTO in backend/src/main/java/com/financial/rules/dto/RuleCreateRequest.java with validation annotations
- [x] T018 [P] [US1] Create RuleUpdateRequest DTO in backend/src/main/java/com/financial/rules/dto/RuleUpdateRequest.java with version field for optimistic locking
- [x] T019 [P] [US1] Create RuleResponse DTO in backend/src/main/java/com/financial/rules/dto/RuleResponse.java
- [x] T020 [P] [US1] Create RuleSummaryResponse DTO in backend/src/main/java/com/financial/rules/dto/RuleSummaryResponse.java for list views
- [x] T021 [P] [US2] Create EntryTemplateRequest DTO in backend/src/main/java/com/financial/rules/dto/EntryTemplateRequest.java
- [x] T022 [P] [US2] Create EntryLineRequest DTO in backend/src/main/java/com/financial/rules/dto/EntryLineRequest.java
- [x] T023 [P] [US2] Create VariableDefinition DTO in backend/src/main/java/com/financial/rules/dto/VariableDefinition.java with fields: name, type (enum), currency, description
- [x] T024 [P] [US2] Create ExpressionType enum in backend/src/main/java/com/financial/rules/dto/ExpressionType.java with values: DECIMAL, MONEY, BOOLEAN, STRING

### Services for US1+US2

- [x] T025 [US2] Create ExpressionParser service in backend/src/main/java/com/financial/rules/service/ExpressionParser.java with methods: parse, validate, getType, extractVariables (implements strict typing per research.md)
- [x] T026 [US1] Create AccountingRuleService in backend/src/main/java/com/financial/rules/service/AccountingRuleService.java with methods: createRule, getRule, updateRule, deleteRule, listRules, cloneRule
- [x] T027 [US1] Implement version history methods in AccountingRuleService: createVersion (snapshot), listVersions, getVersion, rollbackToVersion

### Controllers for US1+US2

- [x] T028 [US1] Create AccountingRuleController in backend/src/main/java/com/financial/rules/controller/AccountingRuleController.java with endpoints: POST /rules, GET /rules, GET /rules/{id}, PUT /rules/{id}, DELETE /rules/{id}
- [x] T029 [US1] Add OpenAPI annotations to AccountingRuleController with examples from contracts/accounting-rules-api.yaml

**Checkpoint**: At this point, basic rule CRUD with entry templates should work. Users can create rules with debit/credit lines and amount expressions.

---

## Phase 4: User Story 3 - Trigger Conditions (Priority: P2)

**Goal**: Define trigger conditions that specify when rules should be applied, with composable AND/OR logic

**Independent Test**: Create rules with various trigger conditions (event type, amount thresholds), verify conditions are stored and retrieved correctly, verify human-readable display

### Repository for US3

- [ ] T030 [P] [US3] Create TriggerConditionRepository in backend/src/main/java/com/financial/rules/repository/TriggerConditionRepository.java with methods: findByRuleId, deleteByRuleId

### DTOs for US3

- [ ] T031 [P] [US3] Create TriggerConditionRequest DTO in backend/src/main/java/com/financial/rules/dto/TriggerConditionRequest.java
- [ ] T032 [P] [US3] Create TriggerConditionResponse DTO in backend/src/main/java/com/financial/rules/dto/TriggerConditionResponse.java with humanReadable field

### Services for US3

- [ ] T033 [US3] Create TriggerConditionService in backend/src/main/java/com/financial/rules/service/TriggerConditionService.java with methods: saveConditions, getConditions, validateConditionJson, toHumanReadable
- [ ] T034 [US3] Create ConditionEvaluator in backend/src/main/java/com/financial/rules/service/ConditionEvaluator.java with method: evaluate(condition, eventData) for use in simulation

### Controller Updates for US3

- [ ] T035 [US3] Update AccountingRuleService to handle trigger conditions in create/update operations, integrating TriggerConditionService

**Checkpoint**: Rules can now have trigger conditions with AND/OR logic. Conditions display in human-readable format.

---

## Phase 5: User Story 4+5 - Numscript Generation + Validation (Priority: P2)

**Goal**: Generate Formance Ledger Numscript DSL from rules and validate syntax

**Independent Test**: Create a complete rule with entry template, generate Numscript, verify output is valid Numscript syntax, verify validation catches errors

### DTOs for US4+US5

- [ ] T036 [P] [US4] Create GenerationResponse DTO in backend/src/main/java/com/financial/rules/dto/GenerationResponse.java with fields: numscript, validationResult
- [ ] T037 [P] [US5] Create ValidationResult DTO in backend/src/main/java/com/financial/rules/dto/ValidationResult.java with fields: valid, errors, warnings
- [ ] T038 [P] [US5] Create ExpressionValidationResponse DTO in backend/src/main/java/com/financial/rules/dto/ExpressionValidationResponse.java

### Services for US4+US5

- [ ] T039 [US4] Create NumscriptGenerator service in backend/src/main/java/com/financial/rules/service/NumscriptGenerator.java implementing OutputGenerator interface with methods: generate(rule), translateExpression, mapAccountToFormancePath
- [ ] T040 [US4] Integrate NumscriptGenerator with COA module's AccountMappingService to resolve Formance account paths
- [ ] T041 [US5] Create NumscriptValidator service in backend/src/main/java/com/financial/rules/service/NumscriptValidator.java with methods: validate(numscript), validateSyntax, validateAccountReferences

### Controller Endpoints for US4+US5

- [ ] T042 [US4] Add POST /rules/{id}/generate endpoint to AccountingRuleController
- [ ] T043 [US5] Add POST /rules/validate-expression endpoint to AccountingRuleController for expression syntax validation

**Checkpoint**: Rules can generate valid Numscript DSL. Generated output passes syntax validation.

---

## Phase 6: User Story 6 - Rule Simulation (Priority: P3)

**Goal**: Simulate rule execution with sample data to verify expected journal entries

**Independent Test**: Provide sample event data to a rule, verify simulation shows resolved account names and calculated amounts, verify trigger condition evaluation

### DTOs for US6

- [ ] T044 [P] [US6] Create SimulationRequest DTO in backend/src/main/java/com/financial/rules/dto/SimulationRequest.java with eventData field
- [ ] T045 [P] [US6] Create SimulationResponse DTO in backend/src/main/java/com/financial/rules/dto/SimulationResponse.java with fields: wouldFire, reasonNotFired, entries, totalDebits, totalCredits, isBalanced, warnings, errors
- [ ] T046 [P] [US6] Create SimulatedEntry DTO in backend/src/main/java/com/financial/rules/dto/SimulatedEntry.java with fields: accountCode, accountName, entryType, amount, currency, memo

### Services for US6

- [ ] T047 [US6] Create RuleSimulationService in backend/src/main/java/com/financial/rules/service/RuleSimulationService.java with methods: simulate(rule, eventData), evaluateTriggers, evaluateExpressions, resolveAccounts
- [ ] T048 [US6] Integrate RuleSimulationService with COA module's AccountService to resolve account names

### Controller Endpoints for US6

- [ ] T049 [US6] Add POST /rules/{id}/simulate endpoint to AccountingRuleController

**Checkpoint**: Users can test rules with sample data before activation. Simulation shows exactly what entries would be generated.

---

## Phase 7: User Story 7 - Cross-Scenario Reuse (Priority: P3)

**Goal**: Mark rules as shared across scenarios, track references, show impact analysis

**Independent Test**: Mark rule as shared, reference it from multiple scenarios, verify modification shows impact warning, verify clone creates independent copy

### Services for US7

- [ ] T050 [US7] Create RuleReferenceService in backend/src/main/java/com/financial/rules/service/RuleReferenceService.java with methods: addReference(ruleId, scenarioId), removeReference, getReferences, hasReferences
- [ ] T051 [US7] Add impact analysis to AccountingRuleService: getImpactAnalysis(ruleId) returns list of affected scenarios
- [ ] T052 [US7] Update AccountingRuleService.deleteRule to check references and prevent deletion if referenced

### Controller Updates for US7

- [ ] T053 [US7] Add GET /rules/{id}/references endpoint to AccountingRuleController
- [ ] T054 [US7] Add response header or field for impact warnings when updating shared rules

**Checkpoint**: Shared rules are protected from breaking changes. Impact analysis shows all affected scenarios.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and production readiness

### Rule Lifecycle Management

- [ ] T055 [P] Add POST /rules/{id}/activate endpoint to AccountingRuleController with validation-gated state transition
- [ ] T056 [P] Add POST /rules/{id}/archive endpoint to AccountingRuleController
- [ ] T057 [P] Add POST /rules/{id}/restore endpoint to AccountingRuleController
- [ ] T058 [P] Add POST /rules/{id}/clone endpoint to AccountingRuleController

### Version Management

- [ ] T059 [P] Add GET /rules/{id}/versions endpoint to AccountingRuleController
- [ ] T060 [P] Add GET /rules/{id}/versions/{versionNumber} endpoint to AccountingRuleController
- [ ] T061 [P] Add POST /rules/{id}/rollback/{versionNumber} endpoint to AccountingRuleController

### Documentation & Validation

- [ ] T062 Validate all endpoints against quickstart.md examples and contracts/accounting-rules-api.yaml

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **US1+US2 MVP (Phase 3)**: Depends on Foundational - This is the MVP
- **US3 (Phase 4)**: Depends on Foundational, can run parallel with Phase 3 after T013
- **US4+US5 (Phase 5)**: Depends on Phase 3 (needs entry templates)
- **US6 (Phase 6)**: Depends on Phase 3+4 (needs rules and conditions)
- **US7 (Phase 7)**: Depends on Phase 3 (extends rule management)
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1+2 (P1)**: MVP - Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 3 (P2)**: Can start after Foundational - Independent from US1+2 but typically tested after
- **User Story 4+5 (P2)**: Depends on US1+2 (needs entry templates to generate Numscript)
- **User Story 6 (P3)**: Depends on US1+2+3 (needs rules, templates, and conditions)
- **User Story 7 (P3)**: Depends on US1+2 (extends existing rule management)

### Within Each User Story

- Repositories can be created in parallel
- DTOs can be created in parallel
- Services after repositories
- Controllers after services
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1**: T002, T003, T004, T005 can run in parallel after T001  
**Phase 2**: T007-T012 can run in parallel after T006; T013 after T006  
**Phase 3**: T014-T024 (repos/DTOs) can run in parallel; T025 before T026; T028-T029 after T026-T027  
**Phase 4**: T030-T032 parallel; T033-T034 after repos; T035 last  
**Phase 5**: T036-T038 parallel; T039-T041 after DTOs; T042-T043 after services  
**Phase 6**: T044-T046 parallel; T047-T048 after DTOs; T049 after services  
**Phase 7**: T050-T052 sequential; T053-T054 after services  
**Phase 8**: T055-T061 can all run in parallel; T062 last

---

## Parallel Example: Phase 3 (MVP)

```bash
# Launch all repositories in parallel:
T014: AccountingRuleVersionRepository
T015: EntryTemplateRepository
T016: EntryLineRepository

# Launch all DTOs in parallel:
T017-T024: All request/response DTOs

# After DTOs complete, services:
T025: ExpressionParser (required for T026)
T026: AccountingRuleService
T027: Version history methods

# After services complete, controllers:
T028: AccountingRuleController
T029: OpenAPI annotations
```

---

## Implementation Strategy

### MVP First (Phase 1-3 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1+2 (Rule CRUD + Entry Templates)
4. **STOP and VALIDATE**: Test rule creation with expressions
5. Deploy/demo if ready - users can now create and manage rules!

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1+US2 → Test independently → Deploy (MVP!)
3. Add US3 → Test trigger conditions → Deploy
4. Add US4+US5 → Test Numscript generation → Deploy
5. Add US6 → Test simulation → Deploy
6. Add US7 → Test cross-scenario → Deploy
7. Polish phase → Final validation → Production release

### Suggested MVP Scope

**Minimum Viable Product**: Phase 1 + Phase 2 + Phase 3 (Tasks T001-T029)

This delivers:
- Rule CRUD operations
- Entry templates with debit/credit lines
- Amount expressions with variables
- Version tracking on updates
- Basic validation

Users can create and manage accounting rules immediately. Trigger conditions, Numscript generation, simulation, and cross-scenario features can be added incrementally.

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- COA module integration required for: account validation (US2), Formance mappings (US4), account name resolution (US6)

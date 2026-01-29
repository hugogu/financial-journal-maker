---
description: "Implementation tasks for Chart of Accounts Management"
---

# Tasks: Chart of Accounts Management

**Input**: Design documents from `/specs/001-coa-management/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/coa-api.yaml

**Tests**: Tests are NOT included in this task list as they were not explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/financial/coa/`
- **Resources**: `backend/src/main/resources/`
- **Tests**: `backend/src/test/java/com/financial/coa/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic Spring Boot structure

- [x] T001 Create backend/ directory structure per plan.md with Maven/Gradle build configuration
- [x] T002 Initialize Spring Boot 3.x project with dependencies: Spring Web, Spring Data JPA, PostgreSQL, Flyway, SpringDoc OpenAPI, Apache POI, OpenCSV in backend/pom.xml or backend/build.gradle
- [x] T003 [P] Configure application.yml in backend/src/main/resources/ with datasource, JPA, Flyway, and server settings
- [x] T004 [P] Create OpenAPI configuration class in backend/src/main/java/com/financial/coa/config/OpenApiConfig.java
- [x] T005 [P] Create base package structure: controller/, service/, repository/, domain/, dto/, exception/, config/ under backend/src/main/java/com/financial/coa/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Create Flyway migration V1__create_coa_tables.sql in backend/src/main/resources/db/migration/ with accounts, account_mappings, account_references, import_jobs tables
- [x] T007 [P] Create base exception classes in backend/src/main/java/com/financial/coa/exception/: CoaException.java (base), DuplicateAccountCodeException.java, AccountReferencedException.java, AccountNotFoundException.java, InvalidImportFileException.java, CircularReferenceException.java
- [x] T008 [P] Create global exception handler @RestControllerAdvice in backend/src/main/java/com/financial/coa/exception/GlobalExceptionHandler.java with error response mapping
- [x] T009 [P] Create ErrorResponse DTO in backend/src/main/java/com/financial/coa/dto/ErrorResponse.java with timestamp, status, error, message, path, errorCode fields
- [x] T010 [P] Configure logging in backend/src/main/resources/logback-spring.xml with appropriate log levels for debugging
- [x] T011 Create Docker Compose configuration in docker-compose.yml with PostgreSQL service (coa_db database)
- [x] T012 [P] Create Dockerfile for Spring Boot application in backend/Dockerfile with multi-stage build

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 + 5 - Core Account Management (Priority: P1) 🎯 MVP

**Combined Goal**: Enable creation, reading, updating, and deletion of hierarchical chart of accounts with reference protection. This combines US1 (basic CRUD) and US5 (immutability constraints) as they are tightly coupled.

**Independent Test**: Create a multi-level account hierarchy (Assets → Current Assets → Cash), mark an account as referenced, verify code immutability is enforced, then delete unreferenced accounts successfully.

### Domain Models for US1 + US5

- [x] T013 [P] [US1] Create Account entity in backend/src/main/java/com/financial/coa/domain/Account.java with fields: id, code (unique), name, description, parentId (self-reference), sharedAcrossScenarios, version (@Version), createdAt, updatedAt, createdBy
- [x] T014 [P] [US5] Create AccountReference entity in backend/src/main/java/com/financial/coa/domain/AccountReference.java with fields: id, accountCode, referenceSourceId, referenceType (enum: RULE/SCENARIO), referenceDescription, createdAt

### Repositories for US1 + US5

- [x] T015 [P] [US1] Create AccountRepository interface in backend/src/main/java/com/financial/coa/repository/AccountRepository.java extending JpaRepository with custom query methods: findByCode, findByParentId, existsByCode
- [x] T016 [P] [US5] Create AccountReferenceRepository interface in backend/src/main/java/com/financial/coa/repository/AccountReferenceRepository.java with methods: existsByAccountCode, findByAccountCode, countByAccountCode

### DTOs for US1 + US5

- [x] T017 [P] [US1] Create AccountCreateRequest DTO in backend/src/main/java/com/financial/coa/dto/AccountCreateRequest.java with Bean Validation annotations (@NotBlank, @Pattern, @Size)
- [x] T018 [P] [US1] Create AccountUpdateRequest DTO in backend/src/main/java/com/financial/coa/dto/AccountUpdateRequest.java with version field for optimistic locking
- [x] T019 [P] [US1] Create AccountResponse DTO in backend/src/main/java/com/financial/coa/dto/AccountResponse.java with all account fields plus hasChildren, isReferenced, referenceCount
- [x] T020 [P] [US1] Create AccountTreeNode DTO in backend/src/main/java/com/financial/coa/dto/AccountTreeNode.java for recursive tree structure with children list
- [x] T021 [P] [US5] Create AccountReferenceResponse DTO in backend/src/main/java/com/financial/coa/dto/AccountReferenceResponse.java
- [x] T022 [P] [US5] Create ReferenceCreateRequest DTO in backend/src/main/java/com/financial/coa/dto/ReferenceCreateRequest.java

### Services for US1 + US5

- [x] T023 [US1] Create AccountValidationService in backend/src/main/java/com/financial/coa/service/AccountValidationService.java with methods: validateUniqueCode, validateParentExists, validateNoCircularReference, validateAccountCodeFormat
- [x] T024 [US1] [US5] Create AccountService in backend/src/main/java/com/financial/coa/service/AccountService.java with CRUD operations, tree retrieval using recursive CTE, reference checking before updates/deletes (depends on T015, T016, T023)
- [x] T025 [US5] Implement reference protection logic in AccountService: check references before code modification, check references before deletion, return reference details in error messages

### Controllers for US1 + US5

- [x] T026 [US1] Create AccountController in backend/src/main/java/com/financial/coa/controller/AccountController.java with endpoints: POST /accounts, GET /accounts/{code}, GET /accounts (paginated), GET /accounts/tree, PUT /accounts/{code}, DELETE /accounts/{code}
- [x] T027 [US5] Add reference management endpoints to AccountController: GET /accounts/{code}/references, POST /references, DELETE /references/{referenceId}
- [x] T028 [US1] [US5] Add OpenAPI annotations (@Operation, @ApiResponse) to all AccountController methods with examples from contracts/coa-api.yaml

**Checkpoint**: At this point, core account management with reference protection should be fully functional and testable independently

---

## Phase 4: User Story 2 - Formance Ledger Mappings (Priority: P2)

**Goal**: Enable mapping between chart of accounts codes and Formance Ledger account paths

**Independent Test**: Create mappings for several accounts, query mappings, update mappings, verify one-to-one relationship is enforced

### Domain Models for US2

- [x] T029 [P] [US2] Create AccountMapping entity in backend/src/main/java/com/financial/coa/domain/AccountMapping.java with fields: id, accountCode (unique FK to Account), formanceLedgerAccount, version, createdAt, updatedAt

### Repositories for US2

- [x] T030 [P] [US2] Create AccountMappingRepository interface in backend/src/main/java/com/financial/coa/repository/AccountMappingRepository.java with methods: findByAccountCode, existsByAccountCode, deleteByAccountCode

### DTOs for US2

- [x] T031 [P] [US2] Create MappingCreateRequest DTO in backend/src/main/java/com/financial/coa/dto/MappingCreateRequest.java with accountCode and formanceLedgerAccount fields
- [x] T032 [P] [US2] Create MappingUpdateRequest DTO in backend/src/main/java/com/financial/coa/dto/MappingUpdateRequest.java with version field
- [x] T033 [P] [US2] Create MappingResponse DTO in backend/src/main/java/com/financial/coa/dto/MappingResponse.java

### Services for US2

- [x] T034 [US2] Create AccountMappingService in backend/src/main/java/com/financial/coa/service/AccountMappingService.java with methods: createMapping, getMapping, updateMapping, deleteMapping, validateAccountExists (depends on T030)

### Controllers for US2

- [x] T035 [US2] Create AccountMappingController in backend/src/main/java/com/financial/coa/controller/AccountMappingController.java with endpoints: POST /accounts/mappings, GET /accounts/mappings/{code}, PUT /accounts/mappings/{code}, DELETE /accounts/mappings/{code}
- [x] T036 [US2] Add OpenAPI annotations to AccountMappingController methods with examples from contracts/coa-api.yaml

**Checkpoint**: At this point, User Stories 1, 2, and 5 should all work independently

---

## Phase 5: User Story 3 - File Import (Priority: P2)

**Goal**: Enable batch import of chart of accounts from Excel/CSV files with validation

**Independent Test**: Upload sample Excel/CSV files with valid and invalid structures, verify valid files create correct hierarchies and invalid files provide detailed error messages

### Domain Models for US3

- [x] T037 [P] [US3] Create ImportJob entity in backend/src/main/java/com/financial/coa/domain/ImportJob.java with fields: id, fileName, fileFormat (enum: EXCEL/CSV), status (enum: PENDING/PROCESSING/COMPLETED/FAILED), totalRecords, processedRecords, failedRecords, errorDetails (JSON), startedAt, completedAt, createdAt, createdBy

### Repositories for US3

- [x] T038 [P] [US3] Create ImportJobRepository interface in backend/src/main/java/com/financial/coa/repository/ImportJobRepository.java

### DTOs for US3

- [x] T039 [P] [US3] Create ImportRequestDto in backend/src/main/java/com/financial/coa/dto/ImportRequestDto.java with fileName, validateOnly fields
- [x] T040 [P] [US3] Create ImportJobResponse DTO in backend/src/main/java/com/financial/coa/dto/ImportJobResponse.java
- [x] T041 [P] [US3] Create ImportErrorResponse DTO in backend/src/main/java/com/financial/coa/dto/ImportErrorResponse.java extending ErrorResponse with validationErrors list
- [x] T042 [P] [US3] Create internal ImportRecord class in backend/src/main/java/com/financial/coa/service/ImportRecord.java to represent parsed file row

### Services for US3

- [x] T043 [P] [US3] Create FileParserService in backend/src/main/java/com/financial/coa/service/FileParserService.java with methods: parseExcel (using Apache POI streaming), parseCsv (using OpenCSV), detectFileFormat
- [x] T044 [US3] Create AccountImportService in backend/src/main/java/com/financial/coa/service/AccountImportService.java with methods: validateImportFile, performImport, createImportJob, updateImportJobStatus (depends on T043, T024)
- [x] T045 [US3] Implement validation logic in AccountImportService: checkDuplicateCodes, checkCircularReferences, checkMissingParents, validateFileStructure, createAccountsInDependencyOrder
- [x] T046 [US3] Add transaction management to AccountImportService.performImport with @Transactional and proper rollback on validation failure

### Controllers for US3

- [x] T047 [US3] Create AccountImportController in backend/src/main/java/com/financial/coa/controller/AccountImportController.java with endpoints: POST /accounts/import (multipart/form-data), GET /accounts/import/{jobId}
- [x] T048 [US3] Add file upload handling in AccountImportController with multipart file processing, size limit validation (10MB), and format detection
- [x] T049 [US3] Add OpenAPI annotations to AccountImportController with multipart/form-data schema from contracts/coa-api.yaml

**Checkpoint**: All P1 and P2 user stories should now be independently functional

---

## Phase 6: User Story 4 - Cross-Scenario Validation (Priority: P3)

**Goal**: Enable accounts to be marked as shared across scenarios and enforce consistency rules

**Independent Test**: Mark accounts as shared, attempt modifications, verify cross-scenario usage tracking works correctly

### Service Updates for US4

- [x] T050 [US4] Add cross-scenario validation methods to AccountService in backend/src/main/java/com/financial/coa/service/AccountService.java: validateSharedAccountModification, trackScenarioUsage, listAccountScenarios
- [x] T051 [US4] Update AccountService.updateAccount to check sharedAcrossScenarios flag and enforce immutability rules for shared accounts
- [x] T052 [US4] Update AccountService.deleteAccount to prevent deletion of accounts used in multiple scenarios with detailed error message

### Controller Updates for US4

- [x] T053 [US4] Add query endpoint GET /accounts?shared=true to AccountController for filtering shared accounts
- [x] T054 [US4] Update error responses in AccountController to include scenario usage information when operations are blocked

**Checkpoint**: All user stories (P1, P2, P3) should now be independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and production readiness

- [x] T055 [P] Add comprehensive logging to all service methods with appropriate log levels (INFO for operations, DEBUG for details, ERROR for exceptions)
- [x] T056 [P] Add Spring Actuator health checks in backend/pom.xml and configure endpoints in application.yml
- [x] T057 [P] Create README.md in backend/ with setup instructions, referencing quickstart.md
- [x] T058 [P] Add API documentation generation configuration to expose /swagger-ui.html and /v3/api-docs endpoints
- [x] T059 [P] Configure HikariCP connection pool settings in application.yml for optimal performance
- [x] T060 [P] Add database indexes verification script to check all indexes from data-model.md are created by Flyway
- [x] T061 [P] Create sample Excel/CSV files in backend/src/main/resources/samples/ for testing import functionality
- [x] T062 Validate quickstart.md instructions by following each curl example and verifying responses match documented format
- [x] T063 [P] Add .gitignore entries for backend/ (target/, .mvn/, *.log files)
- [x] T064 [P] Update docker-compose.yml to include Spring Boot service with proper environment variables and health checks
- [x] T065 Code review and refactoring: ensure all classes follow Spring Boot conventions, proper exception handling, and consistent naming

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1+5 (Phase 3)**: Depends on Foundational phase completion - This is the MVP
- **User Story 2 (Phase 4)**: Depends on Foundational phase completion - Can run in parallel with Phase 3 but typically sequential
- **User Story 3 (Phase 5)**: Depends on Foundational phase completion - Can run in parallel with Phase 3/4 but typically sequential
- **User Story 4 (Phase 6)**: Depends on Phase 3 completion (extends AccountService)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 + 5 (P1)**: MVP - Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independent from US1 but practically tested after US1 exists
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Uses US1's AccountService for account creation
- **User Story 4 (P3)**: Depends on US1 being complete - Extends existing account management logic

### Within Each User Story

- Domain models and DTOs can be created in parallel
- Repositories after domain models
- Services after repositories and validation utilities
- Controllers after services
- OpenAPI annotations after controller methods exist
- Story complete before moving to next priority

### Parallel Opportunities

- **Phase 1**: T003, T004, T005 can run in parallel after T002
- **Phase 2**: T007, T008, T009, T010, T012 can all run in parallel after T006
- **Phase 3 (US1+5)**: 
  - T013, T014 (domain models) in parallel
  - T015, T016 (repositories) in parallel after models
  - T017-T022 (DTOs) all in parallel
  - After services complete: T028 can be done in parallel with next phase if desired
- **Phase 4 (US2)**: 
  - T029, T030, T031-T033 can run in parallel
  - T036 independently after T035
- **Phase 5 (US3)**:
  - T037, T038, T039-T042 can run in parallel
  - T043 can start in parallel with other setup tasks
- **Phase 7**: Most tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1 + 5 (MVP)

```bash
# After Foundational Phase completes, launch in parallel:

# Domain models (can work on simultaneously):
Task T013: "Create Account entity in backend/src/main/java/com/financial/coa/domain/Account.java"
Task T014: "Create AccountReference entity in backend/src/main/java/com/financial/coa/domain/AccountReference.java"

# Repositories (after models done):
Task T015: "Create AccountRepository in backend/src/main/java/com/financial/coa/repository/AccountRepository.java"
Task T016: "Create AccountReferenceRepository in backend/src/main/java/com/financial/coa/repository/AccountReferenceRepository.java"

# All DTOs (can work on simultaneously):
Task T017-T022: All DTO creation tasks can run in parallel
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 5 Only)

1. Complete Phase 1: Setup (T001-T005)
2. Complete Phase 2: Foundational (T006-T012) - CRITICAL: blocks all stories
3. Complete Phase 3: User Story 1 + 5 (T013-T028) - Core account management
4. **STOP and VALIDATE**: Test account CRUD with reference protection independently
5. Complete Phase 7: Polish tasks needed for production (T055-T065)
6. Deploy/demo if ready

**This gives you a functional chart of accounts API with immutability protection.**

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 + 5 → Test independently → **Deploy/Demo (MVP!)**
3. Add User Story 2 → Test mapping functionality → Deploy/Demo
4. Add User Story 3 → Test file imports → Deploy/Demo  
5. Add User Story 4 → Test cross-scenario validation → Deploy/Demo
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup (Phase 1) + Foundational (Phase 2) together
2. Once Foundational is done:
   - Developer A: User Story 1 + 5 (T013-T028) - Priority work
   - Developer B: User Story 2 (T029-T036) - Can start if Developer A finishes domain layer
   - Developer C: User Story 3 (T037-T049) - Can start in parallel, uses US1 services
3. Developer D or A: User Story 4 (T050-T054) - Depends on US1 completion
4. All developers: Polish phase (T055-T065) - Many parallel opportunities

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- US1 and US5 are combined because reference protection is core to account integrity
- File paths use backend/ prefix as specified in plan.md structure
- Tests not included as they were not requested in the specification
- OpenAPI spec at contracts/coa-api.yaml provides exact endpoint signatures and examples
- All validation rules from data-model.md must be implemented in service layer
- Constitution compliance checked: all tasks produce design artifacts, no transaction execution

## Task Count Summary

- **Phase 1 (Setup)**: 5 tasks
- **Phase 2 (Foundational)**: 7 tasks
- **Phase 3 (US1+5 - P1 MVP)**: 16 tasks
- **Phase 4 (US2 - P2)**: 8 tasks
- **Phase 5 (US3 - P2)**: 13 tasks
- **Phase 6 (US4 - P3)**: 5 tasks
- **Phase 7 (Polish)**: 11 tasks

**Total: 65 tasks**

**Parallel opportunities**: 28 tasks marked [P] can run in parallel with others in same phase
**MVP scope**: Phases 1-3 (28 tasks) deliver fully functional account management with reference protection

# Feature Specification: Chart of Accounts Management

**Feature Branch**: `001-coa-management`  
**Created**: 2026-01-28  
**Status**: Draft  
**Input**: User description: "会计科目管理是整个系统的基础模块，负责维护科目体系的增删改查，支持从外部文件导入现有科目。核心能力包括：树形结构的科目体系管理、科目与 Formance Ledger 账户的映射关系、Excel/CSV 文件批量导入、科目复用性校验（跨产品/场景）。核心约束是：底层科目设计必须可在不同产品/场景中共享、科目编码一旦被引用，不可删除或修改编码、科目设计是辅助工具输出，不直接操作真实账本。它是整个AI辅助记账规则设计平台的底层数据核心，需要能以纯API的方式对外提供服务。"

## User Scenarios & Testing

### User Story 1 - Create and Manage Chart of Accounts Structure (Priority: P1)

A financial accountant needs to set up a new chart of accounts for a business scenario. They create parent accounts (like "Assets", "Liabilities") and child accounts (like "Cash", "Accounts Receivable") in a hierarchical tree structure. Each account has a unique code and name that follows accounting conventions.

**Why this priority**: This is the foundation - without the ability to create and organize accounts, no other functionality can work. This delivers immediate value by allowing users to define their accounting structure.

**Independent Test**: Can be fully tested by creating a multi-level account hierarchy (e.g., Assets → Current Assets → Cash) and verifying that parent-child relationships are maintained, codes are unique, and the tree structure can be navigated.

**Acceptance Scenarios**:

1. **Given** no accounts exist, **When** user creates a root account "Assets" with code "1000", **Then** the account is created and can be retrieved as a root node
2. **Given** root account "Assets" exists, **When** user creates child account "Current Assets" with code "1100" under "Assets", **Then** the child account is linked to parent and appears in the tree structure
3. **Given** an account with code "1000" exists, **When** user attempts to create another account with code "1000", **Then** the system rejects the request with error indicating duplicate code
4. **Given** a child account exists under a parent, **When** user updates the child account's name, **Then** the name is updated while maintaining the parent-child relationship
5. **Given** an account has no child accounts and has never been referenced, **When** user deletes the account, **Then** the account is removed from the system

---

### User Story 2 - Map Accounts to Formance Ledger (Priority: P2)

A system administrator needs to connect the designed chart of accounts to actual ledger accounts in Formance Ledger. They specify which Formance Ledger account corresponds to each account code in the chart of accounts, establishing the bridge between design and execution.

**Why this priority**: This enables the system to translate accounting designs into actual ledger operations. Without this mapping, the designed accounts cannot be used in real accounting scenarios.

**Independent Test**: Can be tested by creating account mappings, verifying that each COA account can be linked to one Formance Ledger account, and confirming that mappings can be queried and updated.

**Acceptance Scenarios**:

1. **Given** a chart of accounts exists, **When** user creates a mapping between COA account "1000" and Formance Ledger account "assets:cash", **Then** the mapping is stored and retrievable
2. **Given** a mapping exists for account "1000", **When** user queries the mapping for "1000", **Then** the system returns "assets:cash" as the mapped ledger account
3. **Given** a mapping exists for account "1000", **When** user updates the mapping to point to "assets:checking", **Then** the new mapping replaces the old one
4. **Given** multiple accounts with mappings exist, **When** user queries all mappings, **Then** the system returns all account-to-ledger mappings

---

### User Story 3 - Import Chart of Accounts from External Files (Priority: P2)

An accountant has an existing chart of accounts in Excel or CSV format from another system. They upload the file and the system automatically creates the account hierarchy, validates the structure, and reports any issues before finalizing the import.

**Why this priority**: This significantly reduces setup time and prevents errors when migrating existing accounting structures. It's essential for real-world adoption where users already have established charts of accounts.

**Independent Test**: Can be tested by uploading sample Excel/CSV files with valid and invalid structures, verifying that valid files create correct hierarchies and invalid files provide clear error messages.

**Acceptance Scenarios**:

1. **Given** a valid Excel file with columns [Code, Name, Parent Code, Description], **When** user uploads the file, **Then** the system creates all accounts with correct parent-child relationships
2. **Given** an Excel file with duplicate account codes, **When** user uploads the file, **Then** the system rejects the import with error listing duplicate codes
3. **Given** an Excel file where a child references a non-existent parent code, **When** user uploads the file, **Then** the system rejects the import with error indicating missing parent accounts
4. **Given** a CSV file with valid account structure, **When** user uploads the file, **Then** the system creates all accounts same as Excel format
5. **Given** a file with 500 accounts, **When** user uploads the file, **Then** the import completes within 10 seconds and all accounts are created

---

### User Story 4 - Validate Account Reusability Across Scenarios (Priority: P3)

A financial operations manager needs to reuse a chart of accounts across multiple business products or scenarios. They check whether an account can be safely used in a new context and the system validates compatibility, ensuring that shared accounts maintain consistent definitions.

**Why this priority**: This enables scalability and consistency across the platform. While important for multi-tenant or multi-product usage, the core module can function without this initially.

**Independent Test**: Can be tested by marking accounts as shared, attempting to use them in different scenarios, and verifying that the system enforces consistency rules.

**Acceptance Scenarios**:

1. **Given** an account is marked as "shared across scenarios", **When** user attempts to modify the account code, **Then** the system prevents the modification with error message
2. **Given** an account is referenced in scenario A, **When** user attempts to use the same account in scenario B, **Then** the system allows the usage and marks the account as multi-scenario
3. **Given** an account is used in multiple scenarios, **When** user attempts to delete the account, **Then** the system prevents deletion and lists all scenarios where it's used

---

### User Story 5 - Protect Referenced Accounts from Breaking Changes (Priority: P1)

Once an account is referenced by any accounting rule or transaction, that account's code becomes immutable. Users attempting to modify the code or delete the account receive clear error messages explaining which rules or scenarios reference the account.

**Why this priority**: This is a critical constraint that prevents breaking existing accounting rules. Without this protection, the entire system's integrity could be compromised.

**Independent Test**: Can be tested by creating an account, marking it as "referenced", then attempting to modify its code or delete it, verifying that all destructive operations are blocked.

**Acceptance Scenarios**:

1. **Given** an account has been referenced by an accounting rule, **When** user attempts to change the account code, **Then** the system rejects the change with error indicating the account is referenced
2. **Given** an account has been referenced, **When** user attempts to delete the account, **Then** the system rejects the deletion and lists all references
3. **Given** an account has not been referenced, **When** user changes the account code, **Then** the change is allowed
4. **Given** an account becomes referenced, **When** system marks the account as immutable, **Then** the account's status is updated and all future modification attempts are blocked

---

### Edge Cases

- What happens when a user uploads a file with circular parent-child references (e.g., Account A is parent of B, B is parent of C, C is parent of A)?
- How does the system handle accounts with very long codes (>50 characters) or special characters in codes?
- What happens when a parent account is attempted to be deleted but has child accounts?
- How does the system handle concurrent updates to the same account from multiple users?
- What happens when importing a file that contains accounts that already exist in the system (merge or reject)?
- How does the system handle accounts with duplicate names but different codes?
- What happens when the Formance Ledger connection fails during a mapping operation?
- How does the system handle file uploads that exceed size limits (e.g., 10MB+ files)?

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow users to create accounts with unique codes, names, and optional descriptions
- **FR-002**: System MUST maintain a hierarchical tree structure where each account can have zero or more child accounts
- **FR-003**: System MUST enforce unique account codes across the entire chart of accounts
- **FR-004**: System MUST allow users to update account names and descriptions without changing codes
- **FR-005**: System MUST prevent deletion of accounts that have child accounts
- **FR-006**: System MUST prevent deletion of accounts that have been referenced by any accounting rule or scenario
- **FR-007**: System MUST prevent modification of account codes once the account has been referenced
- **FR-008**: System MUST allow users to create mappings between chart of accounts and Formance Ledger accounts
- **FR-009**: System MUST allow users to update existing account-to-ledger mappings
- **FR-010**: System MUST allow users to query mappings by account code
- **FR-011**: System MUST support importing chart of accounts from Excel files with columns: Code, Name, Parent Code, Description
- **FR-012**: System MUST support importing chart of accounts from CSV files with same structure as Excel
- **FR-013**: System MUST validate file structure before import and reject invalid files with clear error messages
- **FR-014**: System MUST detect and reject imports containing duplicate account codes
- **FR-015**: System MUST detect and reject imports containing circular parent-child references
- **FR-016**: System MUST detect and reject imports where child accounts reference non-existent parent codes
- **FR-017**: System MUST allow accounts to be marked as "shared across scenarios"
- **FR-018**: System MUST track which scenarios reference which accounts
- **FR-019**: System MUST prevent breaking changes (code modification or deletion) to accounts used in multiple scenarios
- **FR-020**: System MUST provide all functionality via RESTful API endpoints
- **FR-021**: System MUST return structured error responses with error codes and messages for all API failures
- **FR-022**: System MUST support pagination when retrieving large account lists
- **FR-023**: System MUST support filtering accounts by parent code or hierarchy level
- **FR-024**: System MUST persist all account data and mappings durably
- **FR-025**: System MUST log all account creation, modification, and deletion operations with timestamps and user information

### Key Entities

- **Account**: Represents a single account in the chart of accounts. Key attributes include unique code, name, description, parent account reference, creation timestamp, last modified timestamp, reference count (how many rules/scenarios use it), and shared flag (whether it can be used across scenarios).

- **Account Mapping**: Represents the relationship between a chart of accounts code and a Formance Ledger account identifier. Key attributes include source account code, target Formance Ledger account path, creation timestamp, and last modified timestamp.

- **Import Job**: Represents a batch import operation from external files. Key attributes include file name, file format (Excel/CSV), status (pending/processing/completed/failed), total accounts to import, accounts successfully imported, error messages, and timestamps.

- **Account Reference**: Represents a usage of an account by an accounting rule or scenario. Key attributes include account code, reference source (rule ID or scenario ID), reference type (rule/scenario), and reference timestamp.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can create a complete chart of accounts with 100+ accounts in hierarchical structure within 30 minutes via API
- **SC-002**: Users can import an existing chart of accounts from Excel/CSV file containing 500 accounts within 10 seconds
- **SC-003**: System correctly validates and rejects 100% of invalid import files (duplicate codes, circular references, missing parents) with clear error messages
- **SC-004**: System prevents 100% of breaking changes to referenced accounts (code modification or deletion)
- **SC-005**: API response time for account CRUD operations is under 200ms for 95% of requests
- **SC-006**: API response time for account tree retrieval (up to 1000 accounts) is under 500ms
- **SC-007**: System successfully maintains data integrity with zero data loss during concurrent operations from multiple API clients
- **SC-008**: All API endpoints return standardized error responses with appropriate HTTP status codes
- **SC-009**: System supports at least 10,000 accounts in a single chart of accounts without performance degradation
- **SC-010**: Account mappings can be queried and updated with 100% consistency (no stale mappings)

## Assumptions

- Users interacting with this module have basic understanding of accounting concepts and chart of accounts structure
- The Formance Ledger system is accessible and provides stable API endpoints for account operations
- Account codes follow a standard format (alphanumeric with optional delimiters like dots or hyphens)
- Excel/CSV import files follow a consistent structure with standard column headers
- The system will be accessed programmatically via API, not through a web UI (UI is out of scope for this module)
- Multiple users may access the API concurrently, requiring proper concurrency control
- Account reference tracking will be managed by the system when rules or scenarios are created (integration with other modules)

## Dependencies

- **Formance Ledger**: External dependency for actual ledger account operations. This module designs accounts but does not execute transactions.
- **File Processing Library**: Required for parsing Excel and CSV files during import operations
- **Database System**: Required for persistent storage of accounts, mappings, and metadata
- **Authentication/Authorization System**: Required to control API access and track which user performs which operation

## Out of Scope

- User interface (web or mobile) - this module is API-only
- Actual ledger transaction posting or execution
- Accounting rule design or management (separate module)
- Financial reporting or analytics
- Multi-currency support
- Audit trail visualization (audit logs are recorded but visualization is out of scope)
- Account archival or soft-delete functionality beyond basic delete protection
- Integration with external accounting systems beyond Excel/CSV import
- Real-time collaboration features (e.g., seeing other users' changes live)
- Account template library or pre-built charts of accounts

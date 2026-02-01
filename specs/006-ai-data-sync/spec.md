# Feature Specification: AI-System Data Bidirectional Sync

**Feature Branch**: `006-ai-data-sync`  
**Created**: 2026-02-01  
**Status**: Draft  
**Input**: User description: "AI与系统数据的双向实时联动"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - AI Reads Existing System Data for Context (Priority: P1)

As a user analyzing financial transactions, when I start an AI conversation about a specific product or scenario, the AI should automatically have access to all relevant existing data (products, scenarios, accounts, booking rules) so that its suggestions are informed by current system state and avoid redundant or conflicting recommendations.

**Why this priority**: This is the foundation for intelligent AI assistance. Without context awareness, AI suggestions will be disconnected from reality and potentially create duplicates or conflicts. This delivers immediate value by making AI responses more accurate and relevant.

**Independent Test**: Can be fully tested by creating a product with specific accounts and booking rules, then starting an AI conversation about that product and verifying the AI acknowledges the existing data in its responses.

**Acceptance Scenarios**:

1. **Given** a product "Credit Card" exists with accounts "Assets:Receivables" and "Revenue:InterestIncome", **When** user starts AI conversation asking "How should I handle credit card transactions?", **Then** AI response references the existing accounts and suggests building upon them rather than creating new ones
2. **Given** a booking rule exists for "Payment Processing", **When** user asks AI to design a similar rule, **Then** AI identifies the existing rule and offers to extend it or create a complementary rule
3. **Given** multiple scenarios exist for a product, **When** user discusses adding a new scenario, **Then** AI lists existing scenarios and asks how the new one differs

---

### User Story 2 - AI Suggestions Auto-Persist to System (Priority: P1)

As a user confirming AI-generated recommendations, when I approve a suggested product, scenario, account, or booking rule in the conversation, the system should automatically create or update the corresponding data entities without requiring me to manually re-enter information in a separate interface.

**Why this priority**: This eliminates the context-switching problem and ensures AI suggestions directly translate to system changes. It's equally critical as context reading because it completes the bidirectional flow and delivers the core time-saving benefit.

**Independent Test**: Can be fully tested by having AI suggest a new account structure, user confirming it, and verifying the accounts appear in the system's account list immediately.

**Acceptance Scenarios**:

1. **Given** AI suggests creating account "Assets:CardReceivables" with code "1120", **When** user confirms the suggestion, **Then** system creates the account and shows confirmation with a link to view it
2. **Given** AI recommends a new booking rule for transaction type "CardPayment", **When** user approves, **Then** system creates the rule entity, generates the Numscript, and associates it with the product
3. **Given** AI proposes updates to an existing scenario, **When** user confirms changes, **Then** system updates the scenario and tracks the modification source as this AI conversation

---

### User Story 3 - Duplicate and Conflict Detection (Priority: P2)

As a user receiving AI suggestions, when the AI proposes creating data that already exists or conflicts with existing rules, the system should detect this automatically and present options to merge, update, or create as new with clear differentiation.

**Why this priority**: Prevents data integrity issues but is secondary to basic sync functionality. Users need the basic read/write flow working first, then can layer on conflict prevention.

**Independent Test**: Can be tested independently by having AI suggest an account that already exists, verifying the system detects it and offers to reuse the existing account instead of creating a duplicate.

**Acceptance Scenarios**:

1. **Given** account "Assets:Cash" already exists, **When** AI suggests creating "Assets:Cash", **Then** system shows warning and offers options: "Use existing", "Create with different code", "Cancel"
2. **Given** a booking rule exists for "Deposit" transactions, **When** AI suggests a rule with overlapping transaction types, **Then** system highlights the conflict and suggests modifying the existing rule or creating a more specific rule
3. **Given** scenario names must be unique per product, **When** AI suggests a scenario name that exists, **Then** system prompts for a different name or to update the existing scenario

---

### User Story 4 - Visual Sync Status Indicators (Priority: P3)

As a user interacting with AI, I want to see visual indicators showing when AI is reading system data and when data is being written back, so I understand what information AI has access to and can verify changes are being persisted correctly.

**Why this priority**: Enhances user confidence and transparency but is not essential for core functionality. The sync can work invisibly, and this is a UX polish layer.

**Independent Test**: Can be tested by triggering a data read operation and verifying an indicator appears showing "AI accessed: Product XYZ, 3 accounts, 2 rules", then confirming a write operation and seeing "Created: Account Assets:NewAccount".

**Acceptance Scenarios**:

1. **Given** user asks about a product, **When** AI loads product data, **Then** conversation shows indicator "📖 Loaded: Product 'Credit Card' (5 accounts, 3 scenarios, 8 rules)"
2. **Given** user confirms a suggestion, **When** system persists the data, **Then** conversation shows "✅ Created: Account 'Assets:Fees' (code: 1150)" with clickable link
3. **Given** sync operation fails, **When** error occurs, **Then** conversation shows "❌ Failed to create: [entity name] - [reason]" with retry option

---

### User Story 5 - Change Provenance Tracking (Priority: P3)

As a system administrator reviewing data history, I want to see which data entities were created or modified through AI conversations, including links back to the specific conversation and timestamp, so I can audit AI-driven changes and understand the reasoning behind them.

**Why this priority**: Important for governance and debugging but not critical for initial rollout. Users need the sync working first, then can add audit trail capabilities.

**Independent Test**: Can be tested by creating an account via AI, then viewing the account's metadata and verifying it shows "Created via AI conversation [link] on [date]".

**Acceptance Scenarios**:

1. **Given** an account was created through AI conversation, **When** user views account details, **Then** metadata shows "Source: AI Conversation [ID] on 2026-02-01 11:30 AM"
2. **Given** a booking rule was modified via AI, **When** user views rule history, **Then** changelog includes entry "Modified by AI (Conversation [ID]): Updated transaction type filter"
3. **Given** user wants to review AI-created data, **When** filtering data by source, **Then** system shows all entities created/modified via AI with conversation links

---

### Edge Cases

- What happens when AI attempts to create data that violates system constraints (e.g., account code already in use)?
- How does system handle partial failure (e.g., account created successfully but associated rule creation fails)?
- What if user confirms multiple AI suggestions rapidly in succession?
- How does system handle concurrent modifications (user editing data manually while AI tries to update same entity)?
- What if conversation is deleted - do AI-created entities remain or get flagged?
- How does system handle AI suggesting data that requires dependencies not yet created (e.g., rule referencing non-existent accounts)?
- What happens when AI conversation is restored from history - can user still see what data was synced?

## Requirements *(mandatory)*

### Functional Requirements

#### Data Reading (System → AI)

- **FR-001**: System MUST automatically load relevant products when user mentions a product by name in AI conversation
- **FR-002**: System MUST load all accounts associated with a product when that product's context is active in AI conversation
- **FR-003**: System MUST load existing booking rules when user discusses transaction rule design
- **FR-004**: System MUST load scenario definitions when user discusses workflow scenarios for a product
- **FR-005**: System MUST load Numscript templates when AI is generating new booking rule scripts
- **FR-006**: AI MUST acknowledge loaded data in responses (e.g., "I see you have 3 existing accounts for this product...")

#### Data Writing (AI → System)

- **FR-007**: System MUST provide mechanism for user to explicitly confirm AI suggestions before persisting data
- **FR-008**: System MUST create new Product entities when user confirms AI-suggested product designs
- **FR-009**: System MUST create new Scenario entities when user confirms AI-suggested scenarios
- **FR-010**: System MUST create new Account entities when user confirms AI-suggested account structures
- **FR-011**: System MUST create new BookingRule entities when user confirms AI-suggested transaction rules
- **FR-012**: System MUST auto-generate and persist Numscript when booking rules are created via AI
- **FR-013**: System MUST support updating existing entities when AI suggests modifications and user confirms
- **FR-014**: System MUST validate all AI-generated data against system constraints before persisting

#### Conflict Detection

- **FR-015**: System MUST detect when AI suggests creating an entity with identifier that already exists
- **FR-016**: System MUST detect when AI-suggested booking rules have overlapping transaction type filters with existing rules
- **FR-017**: System MUST detect when suggested account codes conflict with existing account codes
- **FR-018**: System MUST present conflict resolution options to user before proceeding with data creation
- **FR-019**: System MUST prevent duplicate entity creation without explicit user override

#### Synchronization Management

- **FR-020**: System MUST maintain association between AI conversation and data entities created/modified through that conversation
- **FR-021**: System MUST record timestamp and conversation ID for all AI-driven data changes
- **FR-022**: System MUST support atomic transactions (all related entities created together or none)
- **FR-023**: System MUST provide rollback capability if user requests to undo AI-created changes within the same conversation session

#### User Experience

- **FR-024**: System MUST provide visual indicators when AI is loading system data
- **FR-025**: System MUST show confirmation messages with entity details after successful data creation
- **FR-026**: System MUST provide direct navigation links from conversation to created/modified entities
- **FR-027**: System MUST display clear error messages when data operations fail with actionable resolution steps

### Key Entities

- **DataSyncContext**: Represents the connection between an AI conversation and system data. Tracks which entities were loaded (read) and which were created/modified (written). Contains conversation ID, timestamp, loaded entity references, and created entity references.

- **AIGeneratedEntity**: Marker interface or metadata for any system entity (Product, Scenario, Account, BookingRule, Numscript) that was created or modified through AI interaction. Includes source conversation ID, creation/modification timestamp, and optional user confirmation details.

- **ConflictResolution**: Represents a detected conflict between AI suggestion and existing data. Contains conflict type (duplicate, overlap, constraint violation), affected entities, resolution options, and user's chosen resolution.

- **SyncOperation**: Represents a single data synchronization action (read or write). Includes operation type, entity type, entity identifier, status (pending/success/failed), timestamp, and error details if failed.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete a full workflow (AI suggests → user confirms → data persisted → user verifies in system) in under 60 seconds
- **SC-002**: 95% of AI-suggested entities are successfully persisted on first confirmation attempt without errors
- **SC-003**: Duplicate entity creation attempts are detected and prevented in 100% of cases
- **SC-004**: Users report 80% reduction in time spent manually transferring AI suggestions to system data (measured via user survey)
- **SC-005**: Zero data integrity violations (orphaned entities, constraint violations) caused by AI sync operations
- **SC-006**: AI conversation context includes relevant system data in 90% of product/scenario discussions (measured by sampling conversations)
- **SC-007**: Users successfully resolve conflicts when presented with conflict resolution options in 95% of cases without needing support

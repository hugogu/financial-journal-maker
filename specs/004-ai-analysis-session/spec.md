# Feature Specification: AI Analysis Session

**Feature Branch**: `004-ai-analysis-session`  
**Created**: 2026-01-31  
**Status**: Draft  
**Input**: User description: "AI 分析会话模块 - 用户通过对话方式描述业务场景，AI 逐步引导完成会计流程设计"

## Clarifications

### Session 2026-01-31

- Q: How should user authorization work between Analyst and Admin roles? → A: Deferred to v2 (no auth in v1 per constitution exclusion)
- Q: How long should session data be retained? → A: Indefinite (never auto-delete, manual cleanup only)
- Q: Can an analyst work on multiple sessions at the same time? → A: Multiple allowed, limit 5 concurrent active sessions
- Q: How should the system handle LLM provider failures? → A: Manual switch (admin must change active provider when issues occur)
- Q: How should export conflicts be resolved? → A: Force overwrite (warn but allow overwriting existing data)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Analyst Creates and Conducts AI Analysis Session (Priority: P1)

An analyst opens the AI Analysis page, creates a new session, describes a business scenario in natural language, and receives AI-generated accounting design suggestions. The analyst reviews suggestions, makes modifications, and the AI validates modifications while maintaining design consistency. The session progresses through Product → Scenario → TransactionType → Accounting hierarchy.

**Why this priority**: This is the core value proposition - enabling analysts to design accounting processes through AI-guided conversation without deep technical expertise.

**Independent Test**: Can be fully tested by creating a session, entering a business description, receiving AI suggestions, and confirming a design at each hierarchical level. Delivers immediate value by generating accounting structures from business descriptions.

**Acceptance Scenarios**:

1. **Given** analyst is on the AI Analysis page, **When** they create a new session and enter "Consumer loan disbursement process", **Then** AI responds with suggested Product structure within 3 seconds (first byte).
2. **Given** an active session with confirmed Product design, **When** analyst requests Scenario suggestions, **Then** AI generates scenarios consistent with the confirmed Product.
3. **Given** AI suggests a Scenario, **When** analyst modifies the scenario name, **Then** AI validates the modification and explains any consistency implications.
4. **Given** session at TransactionType level, **When** analyst confirms the design, **Then** system auto-saves progress and AI recommends next steps.

---

### User Story 2 - Session Lifecycle Management (Priority: P1)

An analyst manages session states - creating new sessions, pausing ongoing sessions to continue later, resuming paused sessions with full context, marking sessions complete, and archiving old sessions for reference.

**Why this priority**: Essential for real-world usage where design work spans multiple sessions and needs persistence.

**Independent Test**: Can be tested by creating a session, adding content, pausing it, closing the browser, reopening, resuming the session, and verifying all context is preserved.

**Acceptance Scenarios**:

1. **Given** an analyst with an active session, **When** they click "Pause", **Then** session state is saved and analyst can safely close the page.
2. **Given** a paused session exists, **When** analyst opens the session list and clicks "Resume", **Then** full conversation history and current design state are restored.
3. **Given** a session with completed design, **When** analyst marks it "Complete", **Then** session becomes read-only and design artifacts are finalized.
4. **Given** multiple old completed sessions, **When** analyst archives them, **Then** sessions move to archive view and don't clutter active list.

---

### User Story 3 - Design Export (Priority: P2)

An analyst with a completed session exports the finalized design as accounts (Chart of Accounts entries), accounting rules, and Numscript definitions for integration with the core system.

**Why this priority**: Bridges the AI-assisted design phase with actual system configuration, making designs actionable.

**Independent Test**: Can be tested by completing a session with full hierarchy and exporting each artifact type, verifying exported content matches the confirmed design.

**Acceptance Scenarios**:

1. **Given** a completed session, **When** analyst clicks "Export Accounts", **Then** system generates COA entries based on the confirmed design.
2. **Given** a completed session, **When** analyst clicks "Export Rules", **Then** system generates accounting rules matching the design.
3. **Given** a completed session, **When** analyst clicks "Export Numscript", **Then** system generates valid Numscript code for each transaction type.
4. **Given** exported artifacts, **When** analyst imports them to the main system, **Then** they integrate without manual modification.

---

### User Story 4 - Admin Configures AI Models and API Keys (Priority: P2)

A system administrator accesses the AI Configuration page to manage LLM provider settings, API keys, and select active models. Configuration changes take effect for new sessions.

**Why this priority**: Required for deployment flexibility and supporting multiple LLM providers based on organization needs.

**Independent Test**: Can be tested by configuring a new API key, selecting a different model, creating a session, and verifying the new model is used.

**Acceptance Scenarios**:

1. **Given** admin is on AI Configuration page, **When** they add a new API key for OpenAI, **Then** the key is securely stored and validated.
2. **Given** multiple configured providers, **When** admin selects "Claude" as active model, **Then** new sessions use Claude for responses.
3. **Given** an invalid API key, **When** admin saves configuration, **Then** system shows validation error with specific failure reason.
4. **Given** active sessions exist, **When** admin changes model, **Then** existing sessions continue with original model, new sessions use new model.

---

### User Story 5 - Admin Manages Custom Prompts (Priority: P3)

A system administrator customizes the AI prompts used during analysis sessions - adjusting system prompts, examples, and guidance text for each design phase to improve AI output quality.

**Why this priority**: Enables fine-tuning AI behavior for organization-specific accounting practices without code changes.

**Independent Test**: Can be tested by modifying a prompt, starting a new session, and verifying AI responses reflect the customization.

**Acceptance Scenarios**:

1. **Given** admin is on Prompt Management page, **When** they edit the "Product Analysis" prompt, **Then** changes are saved with version history.
2. **Given** custom prompts configured, **When** analyst creates a new session, **Then** AI uses the customized prompts.
3. **Given** a problematic prompt edit, **When** admin wants to revert, **Then** they can restore any previous prompt version.
4. **Given** multiple prompt templates, **When** admin views them, **Then** prompts are organized by design phase (Product/Scenario/Type/Accounting).

---

### User Story 6 - AI Integrates Existing System Data (Priority: P2)

During analysis, AI automatically retrieves and incorporates existing Products, Scenarios, TransactionTypes, and accounting rules from the system, ensuring suggestions align with established structures.

**Why this priority**: Prevents duplicate creation and ensures new designs are consistent with existing accounting framework.

**Independent Test**: Can be tested by having existing Products in the system, starting a session about a related topic, and verifying AI references existing structures in suggestions.

**Acceptance Scenarios**:

1. **Given** Products exist in system, **When** analyst describes a scenario related to existing Product, **Then** AI suggests linking to existing Product rather than creating new one.
2. **Given** existing accounting rules, **When** AI suggests TransactionType accounting, **Then** suggestions reference compatible existing rules.
3. **Given** analyst asks about current setup, **When** they request "show me existing loan products", **Then** AI retrieves and displays relevant system data.

---

### Edge Cases

- What happens when AI service is unavailable or times out?
  - System displays graceful error message and allows retry
  - Session state is preserved even if AI call fails
- How does system handle concurrent edits to same session?
  - Sessions are single-user; attempting to open active session elsewhere shows warning
- What happens when API rate limits are exceeded?
  - System queues requests and shows estimated wait time
- How does system handle very long conversations exceeding context limits?
  - System summarizes older context while preserving confirmed design decisions
- What happens when exported design conflicts with existing system data?
  - System warns about conflicts but allows force overwrite of existing data

## Requirements *(mandatory)*

### Functional Requirements

**Session Management**
- **FR-001**: System MUST support session lifecycle states: DRAFT, ACTIVE, PAUSED, COMPLETED, ARCHIVED
- **FR-002**: System MUST auto-save session content at configurable intervals (default: 30 seconds)
- **FR-003**: System MUST preserve full conversation history and design state when pausing/resuming
- **FR-004**: System MUST prevent editing of COMPLETED sessions (read-only access)
- **FR-024**: System MUST retain session data indefinitely (manual cleanup only, no auto-deletion)
- **FR-025**: System MUST limit each analyst to maximum 5 concurrent active sessions

**AI Conversation**
- **FR-005**: System MUST support streaming AI responses to display output progressively
- **FR-006**: System MUST achieve first byte response time under 3 seconds for AI requests
- **FR-007**: System MUST maintain design consistency - AI suggestions at each level must align with confirmed higher-level decisions
- **FR-008**: System MUST validate user modifications against design constraints and explain conflicts
- **FR-009**: System MUST provide smart recommendations for next steps based on current design state

**Hierarchical Design**
- **FR-010**: System MUST enforce Product → Scenario → TransactionType → Accounting design hierarchy
- **FR-011**: System MUST allow analysts to confirm or reject AI suggestions at each level before proceeding
- **FR-012**: System MUST track which design elements are confirmed vs. tentative

**Data Integration**
- **FR-013**: System MUST query existing Products, Scenarios, TransactionTypes during analysis
- **FR-014**: System MUST allow AI to reference and suggest linking to existing system data
- **FR-015**: System MUST support exporting completed designs as COA entries, accounting rules, and Numscript
- **FR-027**: System MUST warn about conflicts during export but allow force overwrite of existing system data

**Administration**
- **FR-016**: System MUST support configuring multiple LLM providers (API endpoints and keys)
- **FR-017**: System MUST securely store API keys (encrypted at rest)
- **FR-018**: System MUST allow administrators to select active LLM model
- **FR-019**: System MUST support custom prompt templates for each design phase
- **FR-020**: System MUST maintain prompt version history with rollback capability
- **FR-026**: System MUST require manual admin intervention to switch LLM providers on failure (no auto-failover)

**Authorization** *(deferred to v2)*
- Authorization and role-based access control are excluded from v1 scope per project constitution

**UI Requirements**
- **FR-021**: System MUST provide analyst-facing analysis page with conversation interface
- **FR-022**: System MUST provide admin-facing configuration pages for AI settings and prompts
- **FR-023**: System MUST display session list with filtering by status

### Key Entities

- **AnalysisSession**: Represents a complete AI-assisted design conversation. Contains conversation history, current design state, lifecycle status, and references to generated design elements.
- **SessionMessage**: Individual message in the conversation (user input or AI response). Includes message type, content, timestamp, and associated design decisions.
- **DesignDecision**: A confirmed design element at any hierarchy level. Links to session, records the decision type (Product/Scenario/Type/Accounting), and stores the confirmed content.
- **AIConfiguration**: System-level AI settings including provider, endpoint, model selection, and encrypted API key.
- **PromptTemplate**: Customizable prompt text for each design phase with version tracking.
- **ExportArtifact**: Generated export from a completed session (COA, rules, or Numscript) with format and content.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: AI first byte response time is under 3 seconds for 95% of requests
- **SC-002**: Analysts can complete a full Product → Accounting design in under 30 minutes for standard scenarios
- **SC-003**: Session auto-save ensures no more than 30 seconds of work is lost on unexpected disconnection
- **SC-004**: Exported designs successfully import to the main system without manual modification in 90% of cases
- **SC-005**: AI suggestions maintain 100% consistency with previously confirmed design decisions within a session
- **SC-006**: System supports switching between at least 3 different LLM providers without code changes
- **SC-007**: Custom prompt changes take effect for new sessions within 1 minute of saving
- **SC-008**: 80% of analysts successfully complete their first AI-assisted design without external help

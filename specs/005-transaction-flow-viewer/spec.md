# Feature Specification: Transaction Flow Viewer

**Feature Branch**: `005-transaction-flow-viewer`  
**Created**: 2026-01-31  
**Status**: Draft  
**Input**: User description: "前端页面浏览查看每个产品、场景下设计的交易流程、会计科目和分录，包含Numscript DSL预览和资金流/信息流可视化"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse Transaction Flow Designs by Product/Scenario (Priority: P1)

An analyst or reviewer opens the Transaction Flow Browser page and navigates through the hierarchy of Products → Scenarios → Transaction Types to view the accounting designs created from AI analysis sessions. They can see a list of all finalized transaction flows, filter by product or scenario, and select any transaction to view its detailed accounting structure.

**Why this priority**: This is the core browsing capability - users need to find and access transaction flow designs before they can view details.

**Independent Test**: Can be fully tested by navigating to the browser page, selecting a product, drilling into scenarios, and viewing the list of transaction types with their accounting entries.

**Acceptance Scenarios**:

1. **Given** multiple completed AI sessions exist, **When** user opens Transaction Flow Browser, **Then** they see a hierarchical navigation tree of Products → Scenarios → Transaction Types.
2. **Given** user is viewing product list, **When** they click on a product, **Then** the view expands to show all scenarios under that product.
3. **Given** user is viewing scenarios, **When** they click "View All", **Then** they see all transaction flows across all products/scenarios in a flat list.
4. **Given** many transaction flows exist, **When** user uses search/filter, **Then** results are filtered by product name, scenario name, or transaction type.

---

### User Story 2 - View Transaction Accounting Details with Journal Entries (Priority: P1)

A user selects a specific transaction type and views its complete accounting design including: all involved accounts (Chart of Accounts entries), the journal entry template showing debit/credit entries, and the conditions under which each entry is triggered.

**Why this priority**: Viewing the accounting structure is the primary purpose - users must understand how each transaction affects the books.

**Independent Test**: Can be tested by selecting any transaction type and verifying the display shows accounts, DR/CR entries, amounts/percentages, and conditions.

**Acceptance Scenarios**:

1. **Given** user selects a transaction type, **When** the detail view opens, **Then** all associated accounts are displayed with account codes, names, and types.
2. **Given** a transaction detail view, **When** user views journal entries, **Then** entries show operation (DR/CR), account code, account name, and amount expression.
3. **Given** a multi-step transaction, **When** user views entries, **Then** entries are grouped by trigger event (e.g., "On order creation", "On payment confirmation").
4. **Given** a transaction with conditional logic, **When** user views entries, **Then** conditions are clearly displayed (e.g., "If fee > 0").

---

### User Story 3 - View Numscript DSL Script (Priority: P1)

After viewing the accounting structure for a transaction, user can switch to view the generated Numscript DSL code that implements the accounting logic. The Numscript view provides syntax highlighting and can be copied for use in the core system.

**Why this priority**: Numscript is the executable format - developers and advanced users need to verify and use the generated code.

**Independent Test**: Can be tested by viewing any transaction detail and switching to Numscript tab, verifying valid DSL code is displayed with syntax highlighting.

**Acceptance Scenarios**:

1. **Given** user is viewing transaction details, **When** they click "Numscript" tab, **Then** the generated Numscript DSL code is displayed.
2. **Given** Numscript view is active, **When** user views the code, **Then** code has syntax highlighting for Numscript keywords, accounts, and amounts.
3. **Given** Numscript is displayed, **When** user clicks "Copy", **Then** the full Numscript code is copied to clipboard.
4. **Given** the accounting entries change, **When** Numscript is regenerated, **Then** the Numscript accurately reflects all journal entries and conditions.

---

### User Story 4 - Real-time Design Preview During AI Session (Priority: P1)

During an AI analysis session, a side panel displays the AI's current understanding of the transaction being designed. As the conversation progresses and the AI suggests accounts and entries, the preview panel updates in real-time to show:
- Current accounts being designed
- Proposed journal entries (DR/CR)
- Transaction flow relationships

**Why this priority**: Essential for the AI analysis workflow - analysts need immediate visual feedback on what the AI is proposing to make informed decisions.

**Independent Test**: Can be tested by starting an AI session, describing a transaction, and observing the preview panel update as AI suggests accounting structures.

**Acceptance Scenarios**:

1. **Given** an active AI analysis session, **When** AI suggests accounts for a transaction, **Then** the preview panel immediately shows the suggested accounts.
2. **Given** AI is proposing journal entries, **When** the response streams in, **Then** the preview panel updates progressively showing DR/CR entries.
3. **Given** user confirms a design element, **When** confirmation is recorded, **Then** the preview panel visually distinguishes confirmed vs. tentative elements.
4. **Given** user modifies AI suggestion, **When** modification is accepted, **Then** preview panel reflects the modified design.
5. **Given** preview is showing, **When** user resizes the panel or collapses it, **Then** the layout adjusts and preference is remembered.

---

### User Story 5 - Cash Flow and Information Flow Visualization (Priority: P2)

Users can view a graphical diagram showing the cash flow (solid arrows) and information flow (dashed arrows) between accounts involved in a transaction. The diagram uses a node-based visualization where:
- Nodes represent accounts (color-coded by type: customer, bank, channel, P&L)
- Solid arrows show actual fund movements with amounts
- Dashed arrows show information flow
- Account states (available, frozen, in-transit) are visually distinguished

**Why this priority**: Visual diagrams dramatically improve comprehension of complex multi-party transactions and are essential for validation and communication.

**Independent Test**: Can be tested by viewing any transaction with multiple accounts and verifying the flow diagram renders with correct nodes, arrows, and labels.

**Acceptance Scenarios**:

1. **Given** user views a transaction, **When** they open the Flow Diagram view, **Then** a node diagram renders showing all involved accounts.
2. **Given** diagram is displayed, **When** user examines nodes, **Then** account types are color-coded (customer=teal, bank=gray, channel=blue, P&L=pink/green).
3. **Given** a fund transfer exists, **When** diagram renders, **Then** solid arrow shows direction and amount (e.g., "1000$").
4. **Given** information flow exists, **When** diagram renders, **Then** dashed arrow shows the information relationship.
5. **Given** accounts have different states, **When** diagram renders, **Then** states are visually distinguished (available=solid fill, frozen=dotted border, in-transit=dashed border).
6. **Given** complex transaction with multiple steps, **When** user hovers on an arrow, **Then** tooltip shows step timing and conditions.

---

### User Story 6 - Transaction Timeline View (Priority: P3)

For transactions involving multiple time points (real-time vs. batch settlement), users can view a timeline diagram showing when each accounting event occurs. This helps understand T+0 vs T+1 vs T+n settlements and the relationship between order events and accounting events.

**Why this priority**: Important for understanding settlement timing but secondary to understanding the accounting structure itself.

**Independent Test**: Can be tested by viewing a transaction with multiple timing events and verifying timeline diagram shows events in temporal order.

**Acceptance Scenarios**:

1. **Given** a transaction with multi-day settlement, **When** user views timeline, **Then** events are plotted on a time axis showing T+0, T+1, etc.
2. **Given** timeline view, **When** user clicks an event point, **Then** the associated journal entries for that event are highlighted.
3. **Given** real-time and batch events, **When** timeline renders, **Then** visual distinction shows which entries are real-time vs. batch.

---

### User Story 7 - Link to Source AI Session (Priority: P3)

From any transaction flow view, users can navigate back to the AI analysis session that created it to understand the design rationale and conversation context.

**Why this priority**: Provides audit trail and design rationale but is secondary to viewing the designs themselves.

**Independent Test**: Can be tested by viewing a transaction and clicking "View Source Session" to navigate to the archived AI session.

**Acceptance Scenarios**:

1. **Given** user is viewing a transaction design, **When** they click "View Source Session", **Then** they navigate to the completed AI analysis session that created it.
2. **Given** source session is displayed, **When** user reads conversation, **Then** they can see the rationale for design decisions.

---

### Edge Cases

- What happens when a transaction has no journal entries defined yet?
  - Display placeholder indicating "No accounting entries defined" with link to source session.
- How does system handle transactions with circular fund flows?
  - Flow diagram uses curved arrows and automatic layout to avoid overlapping lines.
- What happens when Numscript cannot be generated (invalid design)?
  - Display validation errors inline and highlight problematic entries.
- How does preview panel handle very long AI responses?
  - Preview panel has maximum update frequency and batches rapid changes.
- What happens when flow diagram has more than 10 accounts?
  - Provide zoom controls and allow user to collapse/expand related account groups.
- How does system handle transactions with variable amounts (formulas)?
  - Display amount expressions (e.g., "amount * 0.015") rather than literal values.

## Requirements *(mandatory)*

### Functional Requirements

**Browsing & Navigation**
- **FR-001**: System MUST display hierarchical navigation of Products → Scenarios → Transaction Types
- **FR-002**: System MUST support flat list view showing all transaction flows across products
- **FR-003**: System MUST support search/filter by product name, scenario name, and transaction type name
- **FR-004**: System MUST show summary counts at each hierarchy level (e.g., "3 scenarios", "12 transaction types")

**Transaction Detail View**
- **FR-005**: System MUST display all accounts involved in a transaction with code, name, and type
- **FR-006**: System MUST display journal entries as a table with Operation (DR/CR), Account Code, Account Name, and Amount
- **FR-007**: System MUST group journal entries by trigger event for multi-step transactions
- **FR-008**: System MUST display conditions for conditional entries clearly
- **FR-009**: System MUST support multiple view tabs: Accounts, Journal Entries, Numscript, Flow Diagram

**Numscript View**
- **FR-010**: System MUST generate Numscript DSL code from confirmed accounting design
- **FR-011**: System MUST display Numscript with syntax highlighting
- **FR-012**: System MUST provide copy-to-clipboard functionality for Numscript
- **FR-013**: System MUST validate Numscript syntax and highlight errors if invalid

**Real-time Preview (AI Session Integration)**
- **FR-014**: System MUST display a preview panel alongside the AI conversation interface
- **FR-015**: System MUST update preview panel in real-time as AI suggests design elements
- **FR-016**: System MUST visually distinguish confirmed vs. tentative (proposed) elements in preview
- **FR-017**: System MUST support collapsing/expanding the preview panel
- **FR-018**: System MUST remember panel size preference per user session
- **FR-019**: Preview panel MUST show accounts, journal entries summary, and mini flow diagram

**Flow Diagram Visualization**
- **FR-020**: System MUST render node-based flow diagrams with accounts as nodes
- **FR-021**: System MUST color-code nodes by account type (customer, bank, channel, revenue, cost)
- **FR-022**: System MUST show solid arrows for cash/fund flow with amount labels
- **FR-023**: System MUST show dashed arrows for information flow
- **FR-024**: System MUST distinguish account states visually (available, frozen, in-transit)
- **FR-025**: System MUST support interactive features: zoom, pan, hover tooltips
- **FR-026**: System MUST handle complex diagrams with automatic layout optimization

**Timeline View**
- **FR-027**: System MUST render timeline for multi-timing transactions
- **FR-028**: System MUST show relationship between business events and accounting events on timeline

**Traceability**
- **FR-029**: System MUST link each transaction design to its source AI analysis session
- **FR-030**: System MUST allow navigation from transaction view to source session

### Key Entities

- **TransactionFlowView**: Read-only view model combining transaction type with its complete accounting design, journal entries, and generated Numscript.
- **AccountNode**: Visual representation of an account in flow diagram, including position, type, state, and connections.
- **FlowConnection**: Edge in flow diagram representing fund movement or information flow between accounts, with direction, amount expression, and flow type (cash/info).
- **JournalEntryDisplay**: Presentation model for a single debit or credit entry showing operation, account reference, amount, and trigger condition.
- **DesignPreview**: Real-time preview state during AI session, containing tentative accounts, entries, and confirmation status.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate from Product to specific transaction journal entries in under 3 clicks
- **SC-002**: Flow diagrams render within 2 seconds for transactions with up to 20 accounts
- **SC-003**: Preview panel updates within 500ms of AI response chunks arriving
- **SC-004**: Generated Numscript passes syntax validation for 100% of completed designs
- **SC-005**: 90% of users can correctly identify cash flow direction from diagram without training
- **SC-006**: Page load time for transaction browser is under 1 second for up to 1000 transactions
- **SC-007**: Users report flow diagrams improve understanding of complex transactions (>70% positive feedback)
- **SC-008**: Copy-to-clipboard for Numscript works across all major browsers (Chrome, Firefox, Safari, Edge)

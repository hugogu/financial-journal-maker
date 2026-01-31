# Feature Specification: Accounting Rules Management

**Feature Branch**: `002-accounting-rules`  
**Created**: 2026-01-31  
**Status**: Draft  
**Input**: User description: "记账规则定义了'什么业务场景下如何生成会计分录'，并将规则转换为 Formance Ledger 兼容的 Numscript DSL。核心能力包括：记账规则的 CRUD 管理、规则触发条件的可视化配置、分录模板设计、自动生成 Numscript DSL、Numscript 语法验证、规则模拟执行测试。注意：不应当与 Formance Ledger 绑定，Formance Ledger 只是规则表达的一种输出形式。"

---

## Overview

The Accounting Rules Management module provides a **framework-agnostic** engine for defining accounting rules that specify "under what business conditions, what accounting entries should be generated." 

While the initial implementation supports Formance Ledger's Numscript DSL as an output format, the core rule model is designed to be **output-format independent**, enabling future support for other ledger systems or custom DSLs.

---

## Clarifications

### Session 2026-01-31

- **Q**: What are the allowed state transitions for rule lifecycle? → **A**: Validation-gated: draft → active requires validation pass; archived can be restored to draft
- **Q**: How many historical versions should be retained per rule? → **A**: Unlimited versions retained, no automatic purge (manual cleanup only)
- **Q**: What data types should the expression language support? → **A**: Strict types with schema-defined variables and static type checking
- **Q**: When optimistic locking detects a conflict, what should the system do? → **A**: Reject with error (standard optimistic locking with version field)
- **Q**: What permission model should control access to rule operations? → **A**: No permissions (all authenticated users have full access, rely on audit logs)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create and Manage Accounting Rules (Priority: P1)

As a financial controller, I want to create accounting rules that define how business events translate into journal entries, so that I can automate the accounting process consistently across the organization.

**Why this priority**: This is the foundational capability - without basic rule CRUD, no other features can function. Rules are the core entity that everything else depends on.

**Independent Test**: Can be fully tested by creating a rule with trigger conditions and entry templates, then verifying the rule persists correctly and can be retrieved/updated/deleted.

**Acceptance Scenarios**:

1. **Given** I am on the rules management page, **When** I create a new rule with name, description, trigger conditions, and entry template, **Then** the rule is saved and appears in the rules list.

2. **Given** an existing rule, **When** I update its entry template or trigger conditions, **Then** the changes are persisted with version tracking.

3. **Given** an existing rule that is not referenced by any active scenario, **When** I delete it, **Then** the rule is removed from the system.

4. **Given** an existing rule that is referenced by active scenarios, **When** I attempt to delete it, **Then** the system prevents deletion and shows which scenarios reference it.

---

### User Story 2 - Design Entry Templates with Expressions (Priority: P1)

As a financial controller, I want to design entry templates that specify debit and credit accounts with dynamic amount expressions, so that journal entries can be automatically calculated based on transaction data.

**Why this priority**: Entry templates are the core output of rules - they define what accounting entries are generated. This is essential for any useful rule.

**Independent Test**: Can be tested by creating an entry template with multiple lines, each having account references (from COA), debit/credit designation, and amount expressions using variables.

**Acceptance Scenarios**:

1. **Given** I am designing an entry template, **When** I add a debit line with an account from the Chart of Accounts and an amount expression like `transaction.amount * 0.1`, **Then** the line is validated and saved.

2. **Given** an entry template with debit and credit lines, **When** the total debit expression does not equal total credit expression, **Then** the system warns about potential imbalance (but allows saving for complex scenarios).

3. **Given** I reference an account code in an entry line, **When** that account exists in the Chart of Accounts, **Then** the reference is validated and linked.

4. **Given** I use a variable in an amount expression, **When** the variable is not defined in the rule's input schema, **Then** the system shows a validation warning.

---

### User Story 3 - Configure Rule Trigger Conditions (Priority: P2)

As a financial controller, I want to define trigger conditions that specify when a rule should be applied, so that rules automatically activate for the correct business scenarios.

**Why this priority**: Trigger conditions determine rule applicability. While rules can be manually invoked without triggers, automatic triggering is essential for production use.

**Independent Test**: Can be tested by creating various trigger conditions (event type matching, amount thresholds, date ranges) and verifying they evaluate correctly against sample data.

**Acceptance Scenarios**:

1. **Given** I am configuring a rule, **When** I add a trigger condition based on event type (e.g., "payment_received"), **Then** the condition is saved and the rule only matches events of that type.

2. **Given** a rule with multiple trigger conditions, **When** I specify they should be combined with AND/OR logic, **Then** the rule evaluates the combined conditions correctly.

3. **Given** a trigger condition with a comparison operator (e.g., `amount > 10000`), **When** I test the rule against sample data, **Then** the condition evaluates correctly.

4. **Given** a rule with trigger conditions, **When** I view the rule details, **Then** the conditions are displayed in a human-readable format.

---

### User Story 4 - Generate Output DSL (Numscript) (Priority: P2)

As a system integrator, I want accounting rules to be automatically converted to Formance Ledger's Numscript DSL, so that rules can be executed on the Formance Ledger platform.

**Why this priority**: While the system is framework-agnostic, Numscript is the initial target output format. This enables integration with Formance Ledger.

**Independent Test**: Can be tested by creating a complete rule and generating Numscript output, then validating the syntax is correct Numscript DSL.

**Acceptance Scenarios**:

1. **Given** a complete accounting rule with entry template, **When** I request Numscript generation, **Then** valid Numscript DSL is produced that represents the rule logic.

2. **Given** an entry template with account references, **When** generating Numscript, **Then** the account codes are mapped to Formance Ledger account paths using the mappings from COA module.

3. **Given** an entry template with amount expressions, **When** generating Numscript, **Then** the expressions are translated to valid Numscript monetary operations.

4. **Given** a rule with trigger conditions, **When** generating Numscript, **Then** the conditions are translated to Numscript `if/else` constructs where applicable.

---

### User Story 5 - Validate Generated DSL (Priority: P2)

As a financial controller, I want the system to validate generated Numscript syntax, so that I can be confident the rules will execute correctly on the target ledger.

**Why this priority**: Validation prevents deployment of invalid rules, reducing errors in production.

**Independent Test**: Can be tested by generating Numscript for various rules and verifying the syntax validation catches errors.

**Acceptance Scenarios**:

1. **Given** generated Numscript, **When** I request validation, **Then** the system checks syntax correctness and reports any errors.

2. **Given** Numscript with invalid account references, **When** validated, **Then** the error identifies the specific line and account.

3. **Given** valid Numscript, **When** validated, **Then** the system confirms it passes validation with no errors.

---

### User Story 6 - Simulate Rule Execution (Priority: P3)

As a financial controller, I want to simulate rule execution with sample data, so that I can verify the rule produces the expected journal entries before deploying it.

**Why this priority**: Simulation is important for testing but rules can be deployed and tested in staging without it. This is a quality-of-life feature.

**Independent Test**: Can be tested by providing sample transaction data to a rule and verifying the simulated journal entries match expectations.

**Acceptance Scenarios**:

1. **Given** a complete rule and sample transaction data, **When** I run a simulation, **Then** the system shows the journal entries that would be generated.

2. **Given** a simulation result, **When** I view the details, **Then** I can see each entry line with resolved account names and calculated amounts.

3. **Given** a rule with trigger conditions, **When** sample data does not match the conditions, **Then** the simulation indicates the rule would not fire.

4. **Given** a simulation with expression evaluation errors, **When** the simulation runs, **Then** clear error messages identify which expressions failed and why.

---

### User Story 7 - Support Rule Reuse Across Scenarios (Priority: P3)

As a financial controller, I want to mark rules as reusable across multiple products or scenarios, so that common accounting patterns don't need to be duplicated.

**Why this priority**: Reusability reduces maintenance burden but requires the base rule system to be stable first.

**Independent Test**: Can be tested by marking a rule as shared, using it in multiple scenarios, and verifying changes propagate correctly.

**Acceptance Scenarios**:

1. **Given** an accounting rule, **When** I mark it as "shared across scenarios", **Then** the rule can be referenced by multiple business scenarios.

2. **Given** a shared rule referenced by multiple scenarios, **When** I attempt to modify it, **Then** the system warns about the impact on all referencing scenarios.

3. **Given** a rule specific to one scenario, **When** I want to reuse it, **Then** I can clone it as a new independent rule.

---

### Edge Cases

- What happens when an account referenced in a rule is deleted from the Chart of Accounts? → System should prevent deletion or show broken references.
- How does the system handle circular references in amount expressions? → Expression parser should detect and reject circular dependencies.
- What happens when Numscript generation fails due to unsupported features? → Clear error message identifying what cannot be translated.
- How are currency/monetary values handled in expressions? → Must support multi-currency with explicit currency specification.
- What happens when a rule has no entry lines? → Validation error, rules must have at least one entry line.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Rule Management
- **FR-001**: System MUST allow creating accounting rules with name, description, status (draft/active/archived), and version. Status transitions: draft → active requires validation pass; active ↔ archived bidirectional; archived can be restored to draft.
- **FR-002**: System MUST support updating rules with optimistic locking to prevent concurrent modification conflicts. On version mismatch, reject update with error requiring manual refresh.
- **FR-003**: System MUST prevent deletion of rules that are referenced by active scenarios.
- **FR-004**: System MUST maintain unlimited version history for rules (no automatic purge), allowing rollback to any previous version.
- **FR-005**: System MUST support cloning rules to create independent copies.

#### Entry Templates
- **FR-006**: System MUST allow defining entry templates with multiple entry lines (debit and credit).
- **FR-007**: Each entry line MUST specify: account reference (from COA), entry type (debit/credit), amount expression, and optional memo.
- **FR-008**: System MUST validate that account references exist in the Chart of Accounts.
- **FR-009**: System MUST support amount expressions using strictly-typed variables (schema-defined), arithmetic operators (+, -, *, /), and parentheses.
- **FR-010**: System MUST validate expression syntax and warn about undefined variables.
- **FR-011**: System SHOULD warn (not error) when debit and credit expressions don't obviously balance.

#### Trigger Conditions
- **FR-012**: System MUST support defining trigger conditions based on event properties (type, source, etc.).
- **FR-013**: System MUST support comparison operators: equals, not equals, greater than, less than, contains, matches (regex).
- **FR-014**: System MUST support combining multiple conditions with AND/OR logic.
- **FR-015**: System MUST support nested condition groups for complex logic.

#### Output Generation
- **FR-016**: System MUST generate Formance Ledger Numscript DSL from accounting rules.
- **FR-017**: System MUST map COA account codes to Formance Ledger account paths using AccountMapping.
- **FR-018**: System MUST translate amount expressions to valid Numscript monetary operations.
- **FR-019**: System MUST support multiple output formats through a pluggable architecture.
- **FR-020**: System MUST validate generated output syntax before returning it.

#### Simulation
- **FR-021**: System MUST allow simulating rule execution with user-provided sample data.
- **FR-022**: Simulation MUST show resolved account names, calculated amounts, and entry direction (debit/credit).
- **FR-023**: Simulation MUST evaluate trigger conditions and indicate whether the rule would fire.
- **FR-024**: Simulation MUST report expression evaluation errors clearly.

#### Cross-References
- **FR-025**: System MUST track which scenarios/products reference each rule.
- **FR-026**: System MUST support marking rules as "shared" for cross-scenario reuse.
- **FR-027**: Shared rules MUST show impact analysis before modification.

---

### Key Entities

- **AccountingRule**: The primary entity representing a rule. Contains metadata (name, description, status, version), references to trigger conditions and entry template. Can be marked as shared.

- **EntryTemplate**: Defines the journal entry structure to be generated. Contains multiple EntryLines. Associated with exactly one AccountingRule.

- **EntryLine**: A single line in an entry template. Specifies account reference (COA code), entry type (DEBIT/CREDIT), amount expression, and optional memo template.

- **TriggerCondition**: Defines when a rule should apply. Contains field name, operator, value, and optional nested conditions with AND/OR combinators.

- **AmountExpression**: Represents a mathematical expression for calculating amounts. Supports variables, literals, and arithmetic operations. Parsed and validated independently.

- **OutputGenerator**: Abstract concept for generating DSL output. NumscriptGenerator is the initial implementation. Other generators can be added (e.g., for different ledger systems).

- **RuleReference**: Tracks which scenarios/products use which rules. Used for impact analysis and deletion protection.

- **SimulationResult**: Represents the output of a rule simulation. Contains resolved entries, evaluation details, and any errors encountered.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a complete accounting rule (with trigger conditions and entry template) in under 5 minutes.

- **SC-002**: Generated Numscript passes syntax validation 100% of the time for valid rules.

- **SC-003**: Rule simulation completes in under 2 seconds for typical rules with up to 10 entry lines.

- **SC-004**: 95% of users can successfully create their first rule without documentation assistance.

- **SC-005**: System prevents 100% of rule deletions that would break active scenario references.

- **SC-006**: Expression validation catches 100% of syntax errors before rule save.

- **SC-007**: Rules can be cloned in under 3 seconds regardless of complexity.

- **SC-008**: Impact analysis for shared rule modifications shows all affected scenarios within 1 second.

---

## Assumptions

1. The Chart of Accounts module (001-coa-management) is available and provides account lookup and Formance mapping services.
2. Amount expressions use a simple arithmetic syntax (no function calls in initial version).
3. Numscript is the only required output format for initial release; other formats are future enhancements.
4. Rule versioning stores complete snapshots (not diffs) for simplicity.
5. Trigger conditions are evaluated at runtime by the consuming system; this module only defines and validates them.
6. All authenticated users have full access to rule operations; audit logs track changes for accountability.

---

## Out of Scope

- Real-time rule execution/triggering (this module defines rules, execution is handled by the consuming system)
- Complex expression functions (ABS, ROUND, etc.) - may be added in future versions
- Graphical rule designer/flowchart view - text-based configuration for initial version
- Bulk rule import/export - single rule operations only for initial version
- Audit logging of rule changes - covered by general platform audit requirements

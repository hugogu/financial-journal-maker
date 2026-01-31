# Research: AI Analysis Session

**Feature**: 004-ai-analysis-session
**Date**: 2026-01-31

## Research Areas

### 1. Spring AI Integration for LLM Communication

**Decision**: Use Spring AI with configurable LLM clients

**Rationale**:
- Spring AI provides unified abstraction over multiple LLM providers (OpenAI, Anthropic, Azure OpenAI, etc.)
- Native Spring Boot integration with auto-configuration
- Built-in support for streaming responses via Flux
- Supports prompt templates with variable substitution
- Active development and community support

**Alternatives Considered**:
- **LangChain4j**: More feature-rich but heavier; Spring AI sufficient for our needs
- **Direct HTTP clients**: More control but requires implementing provider-specific logic
- **OpenAI Java SDK only**: Limits future provider flexibility

**Implementation Notes**:
- Use `ChatClient` for synchronous calls, `StreamingChatClient` for streaming
- Store provider configurations in database, inject at runtime
- Implement custom `ChatClientProvider` to switch between configured providers

### 2. Streaming Response Architecture

**Decision**: Server-Sent Events (SSE) for AI response streaming

**Rationale**:
- SSE is simpler than WebSocket for unidirectional server-to-client streaming
- Native browser support without additional libraries
- Works well with Spring WebFlux's `Flux` return types
- Automatic reconnection on connection loss
- HTTP/2 compatible

**Alternatives Considered**:
- **WebSocket**: Bidirectional but overkill for streaming responses; more complex
- **Long polling**: Higher latency, more server overhead
- **Chunked HTTP**: Less standardized client handling

**Implementation Notes**:
- Backend: `@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)`
- Frontend: Use native `EventSource` API or `@microsoft/fetch-event-source` for POST support
- Include message ID in each chunk for client-side ordering

### 3. Session State Management

**Decision**: Database-persisted sessions with periodic auto-save

**Rationale**:
- Ensures durability across server restarts
- Supports pause/resume workflow naturally
- Enables session recovery on client reconnection
- Simplifies horizontal scaling (stateless backend)

**Alternatives Considered**:
- **Redis for active sessions**: Faster but adds infrastructure complexity
- **Client-side storage**: Risk of data loss, limited capacity
- **In-memory with periodic persistence**: Data loss risk on crash

**Implementation Notes**:
- Auto-save every 30 seconds (configurable)
- Optimistic locking with version field for concurrent access detection
- Store conversation history as JSONB for flexibility

### 4. Design Hierarchy Enforcement

**Decision**: State machine pattern for design progression

**Rationale**:
- Clear transitions: PRODUCT → SCENARIO → TRANSACTION_TYPE → ACCOUNTING
- Prevents skipping required design phases
- Easy to validate current state and allowed actions
- Maps naturally to UI step indicators

**Alternatives Considered**:
- **Free-form navigation**: Flexible but risks inconsistent designs
- **Linear wizard only**: Too rigid for iterative refinement

**Implementation Notes**:
- `DesignDecision` entity tracks confirmed elements at each level
- AI context includes all confirmed decisions from higher levels
- Allow revisiting previous levels but warn about downstream impacts

### 5. Prompt Template Management

**Decision**: Database-stored templates with version history

**Rationale**:
- Enables runtime customization without deployments
- Version history supports safe experimentation and rollback
- Different templates per design phase (Product/Scenario/Type/Accounting)
- Audit trail for compliance

**Alternatives Considered**:
- **File-based templates**: Requires deployment for changes
- **Git-managed templates**: Good for versioning but complex runtime loading

**Implementation Notes**:
- Template variables: `{{existingProducts}}`, `{{confirmedDecisions}}`, `{{userMessage}}`
- Store as Markdown/text with variable placeholders
- Preview functionality in admin UI before activation

### 6. Export Generation Strategy

**Decision**: Template-based generation with validation

**Rationale**:
- Confirmed design decisions provide structured input
- Templates ensure consistent output format
- Validation catches issues before export completes
- Supports multiple export formats (COA, Rules, Numscript)

**Alternatives Considered**:
- **AI-generated exports**: Less predictable, harder to validate
- **Direct entity mapping**: May miss nuanced accounting rules

**Implementation Notes**:
- COA Export: Map design accounts to existing COA structure or create new entries
- Rules Export: Generate `AccountingRule` entities from design decisions
- Numscript Export: Use existing `NumscriptGenerator` service with design inputs
- Conflict detection: Query existing entities before export, warn on duplicates

### 7. Frontend Framework Choice

**Decision**: Nuxt 3 with Vue 3 Composition API

**Rationale**:
- Per constitution: Vue + Nuxt is mandatory
- Composition API enables cleaner state management for complex chat UI
- Nuxt provides SSR capabilities if needed later
- Rich ecosystem for UI components (e.g., Vuetify, PrimeVue)

**Implementation Notes**:
- Use Pinia for state management
- Composables for reusable logic (useSession, useAIStream)
- Component library: Consider PrimeVue for consistent UI

### 8. Concurrent Session Limit Enforcement

**Decision**: Database constraint with service-layer validation

**Rationale**:
- Database ensures consistency even with multiple backend instances
- Service layer provides user-friendly error messages
- Simple count query before session creation

**Implementation Notes**:
- Query: `SELECT COUNT(*) FROM sessions WHERE analyst_id = ? AND status IN ('ACTIVE', 'PAUSED')`
- Reject creation if count >= 5
- Consider soft enforcement (warning) vs hard enforcement (block)

## Technology Summary

| Component | Technology | Version |
|-----------|------------|---------|
| LLM Integration | Spring AI | 1.0.x |
| Streaming | SSE (Server-Sent Events) | - |
| Backend Framework | Spring Boot | 3.2.x |
| Frontend Framework | Nuxt 3 + Vue 3 | Latest |
| State Management | Pinia | Latest |
| Database | PostgreSQL | 15+ |
| ORM | JPA/Hibernate | 6.x |

## Open Questions Resolved

All NEEDS CLARIFICATION items from spec have been addressed in clarification session:
- ✅ Authorization: Deferred to v2
- ✅ Data retention: Indefinite
- ✅ Concurrent sessions: Max 5
- ✅ LLM failover: Manual
- ✅ Export conflicts: Force overwrite with warning

# Research: AI-System Data Bidirectional Sync

**Feature**: 006-ai-data-sync  
**Date**: 2026-02-01  
**Purpose**: Resolve technical unknowns and establish implementation patterns

## Research Questions

### 1. How to detect entity conflicts in Spring Data JPA?

**Decision**: Use repository query methods with optimistic locking

**Rationale**:
- Existing entities (Product, Scenario, Account, AccountingRule) already use `@Version` for optimistic locking
- Spring Data JPA provides `existsByCode()` and `findByCode()` methods for duplicate detection
- For rule overlap detection, use custom JPQL queries to check transaction type filters

**Implementation Pattern**:
```java
// Duplicate detection
if (accountRepository.existsByCode(suggestedCode)) {
    throw new ConflictException("Account with code already exists");
}

// Optimistic locking prevents concurrent modifications
@Version
private Long version;
```

**Alternatives Considered**:
- Pessimistic locking: Rejected due to performance impact on read-heavy AI operations
- Database constraints only: Rejected because we need to provide user-friendly conflict resolution options before attempting insert

---

### 2. How to maintain conversation-to-entity associations?

**Decision**: Create `DataSyncContext` entity with JSON metadata column

**Rationale**:
- PostgreSQL supports JSONB for flexible metadata storage
- Hibernate can map JSON to `Map<String, Object>` or custom types
- Allows tracking multiple entity references per conversation without complex join tables
- Existing `SessionMessage` entity already uses `@Type(JsonType.class)` pattern

**Implementation Pattern**:
```java
@Entity
@Table(name = "data_sync_contexts")
public class DataSyncContext {
    @Id
    private Long sessionId;  // One-to-one with AnalysisSession
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> loadedEntities;  // {"products": [1,2], "accounts": [10,11]}
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> createdEntities;  // {"products": [3], "rules": [5]}
}
```

**Alternatives Considered**:
- Separate join tables for each entity type: Rejected due to schema complexity (would need 5+ tables)
- Store as comma-separated IDs: Rejected because not queryable and error-prone

---

### 3. How to implement atomic multi-entity creation?

**Decision**: Use Spring `@Transactional` with explicit rollback on partial failure

**Rationale**:
- Spring's declarative transaction management handles ACID properties automatically
- Existing services already use `@Transactional` consistently
- Can leverage existing service methods (ProductService.createProduct, AccountService.createAccount)
- Rollback on exception ensures all-or-nothing semantics

**Implementation Pattern**:
```java
@Transactional
public SyncResult syncAISuggestions(SyncRequest request) {
    List<CreatedEntity> created = new ArrayList<>();
    try {
        for (EntitySuggestion suggestion : request.getSuggestions()) {
            Object entity = createEntity(suggestion);  // Delegates to existing services
            created.add(new CreatedEntity(suggestion.getType(), entity.getId()));
        }
        
        // Record sync operation
        recordSyncContext(request.getSessionId(), created);
        
        return SyncResult.success(created);
    } catch (Exception e) {
        // Transaction automatically rolls back
        log.error("Sync failed, rolling back all changes", e);
        throw new SyncFailedException("Partial sync not allowed", e);
    }
}
```

**Alternatives Considered**:
- Manual transaction management: Rejected because Spring's declarative approach is less error-prone
- Saga pattern with compensating transactions: Rejected as over-engineering for single-database operations

---

### 4. How to enrich AI prompts with system data efficiently?

**Decision**: Extend existing `SystemDataService` with caching layer

**Rationale**:
- `SystemDataService` already exists and loads products, scenarios, accounts, rules
- Current implementation builds context string on every AI call (no caching)
- Add Spring Cache abstraction with 5-minute TTL to reduce database queries
- Invalidate cache when entities are created/updated via sync

**Implementation Pattern**:
```java
@Service
public class SystemDataService {
    
    @Cacheable(value = "systemContext", key = "#phase")
    public SystemDataContext buildContextForPhase(String phase) {
        // Existing logic to query products, scenarios, accounts, rules
        // Returns formatted string for AI prompt injection
    }
    
    @CacheEvict(value = "systemContext", allEntries = true)
    public void invalidateCache() {
        // Called after sync operations create/update entities
    }
}
```

**Alternatives Considered**:
- Real-time queries on every message: Current approach, causes 4-6 database queries per AI message
- Pre-load all data into memory: Rejected due to memory overhead and stale data risk
- Event-driven cache invalidation: Considered but deferred to v2 (adds complexity with Spring Events)

---

### 5. How to handle Numscript generation for AI-created rules?

**Decision**: Reuse existing `NumscriptGenerator` service

**Rationale**:
- `NumscriptGenerator` already exists and generates valid Formance Numscript from `AccountingRule` + `EntryTemplate`
- No changes needed to generation logic
- Sync service will call `NumscriptGenerator.generate()` after creating `AccountingRule` entity
- Generated Numscript stored in `AccountingRuleVersion` table (existing pattern)

**Implementation Pattern**:
```java
@Transactional
public AccountingRule createRuleFromAI(RuleSuggestion suggestion, Long sessionId) {
    // 1. Create AccountingRule entity
    AccountingRule rule = accountingRuleService.createRule(suggestion.toCreateRequest());
    
    // 2. Create EntryTemplate
    EntryTemplate template = entryTemplateService.create(rule.getId(), suggestion.getEntries());
    
    // 3. Generate Numscript (existing service)
    String numscript = numscriptGenerator.generate(rule, template);
    
    // 4. Save version with Numscript
    ruleVersionService.createVersion(rule.getId(), numscript);
    
    // 5. Record in sync context
    syncContextService.recordCreation(sessionId, "rule", rule.getId());
    
    return rule;
}
```

**Alternatives Considered**:
- AI generates Numscript directly: Rejected due to syntax validation complexity and error risk
- Separate Numscript generation endpoint: Rejected because tightly coupled to rule creation

---

### 6. Best practices for Spring Boot REST API design for sync operations?

**Decision**: Follow existing controller patterns with DTOs and exception handling

**Rationale**:
- Existing controllers use consistent patterns: `@RestController`, `@RequestBody`, `@Valid`
- DTOs separate API contract from domain entities
- Global exception handler (`@ControllerAdvice`) provides consistent error responses
- OpenAPI annotations generate Swagger documentation

**Implementation Pattern**:
```java
@RestController
@RequestMapping("/api/ai/sync")
@RequiredArgsConstructor
public class DataSyncController {
    
    private final DataSyncService syncService;
    
    @PostMapping("/suggestions")
    @Operation(summary = "Sync AI suggestions to system entities")
    public ResponseEntity<SyncResponse> syncSuggestions(
            @Valid @RequestBody SyncRequest request) {
        SyncResponse response = syncService.syncSuggestions(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/context/{sessionId}")
    @Operation(summary = "Get sync context for session")
    public ResponseEntity<SyncContextResponse> getContext(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(syncService.getContext(sessionId));
    }
}
```

**Alternatives Considered**:
- GraphQL: Rejected because REST is project standard and simpler for CRUD operations
- WebSocket for real-time sync: Deferred to v2 (current requirement is request-response)

---

### 7. How to implement conflict resolution UI flow?

**Decision**: Backend returns conflict details, frontend shows modal with options

**Rationale**:
- Backend detects conflicts during validation phase (before persistence)
- Return HTTP 409 Conflict with structured error containing conflict type and resolution options
- Frontend catches 409, displays modal with radio buttons for user choice
- User selection sent back to backend with explicit resolution strategy

**Implementation Pattern**:
```java
// Backend
public class ConflictException extends RuntimeException {
    private ConflictType type;
    private List<ResolutionOption> options;
}

// Frontend (Vue composable)
async function syncSuggestion(suggestion) {
    try {
        await api.post('/api/ai/sync/suggestions', suggestion);
    } catch (error) {
        if (error.response.status === 409) {
            const choice = await showConflictModal(error.response.data);
            await api.post('/api/ai/sync/suggestions', {
                ...suggestion,
                resolution: choice
            });
        }
    }
}
```

**Alternatives Considered**:
- Auto-resolve conflicts with heuristics: Rejected because user must make final decision (constitution principle III)
- Batch conflict resolution: Deferred to v2 (current scope is single-entity confirmation)

---

## Technology Stack Confirmation

| Component | Technology | Version | Status |
|-----------|------------|---------|--------|
| Backend Framework | Spring Boot | 3.x | ✅ Existing |
| ORM | Hibernate (JPA) | 6.x | ✅ Existing |
| Database | PostgreSQL | 15+ | ✅ Existing |
| AI Integration | Spring AI | 1.x | ✅ Existing |
| Caching | Spring Cache | (Spring Boot) | ✅ Add annotation |
| Testing | JUnit 5 + Testcontainers | Latest | ✅ Existing |
| API Documentation | SpringDoc OpenAPI | 2.x | ✅ Existing |
| Frontend Framework | Vue 3 + Nuxt | 3.x | ✅ Existing |

**No new dependencies required** - All technologies already in use.

---

## Performance Considerations

### Database Query Optimization
- **Issue**: Loading all products/scenarios/accounts for AI context could be slow
- **Solution**: Add pagination and filtering to SystemDataService queries
- **Metric**: Target <200ms for context loading

### Concurrent Session Handling
- **Issue**: Multiple users syncing simultaneously could cause lock contention
- **Solution**: Optimistic locking (already in place) + retry logic for version conflicts
- **Metric**: Support 100 concurrent sessions

### Cache Strategy
- **Issue**: Stale data in AI prompts after sync operations
- **Solution**: Cache invalidation on write operations
- **Metric**: 5-minute TTL, invalidate on entity creation/update

---

## Security Considerations

### Authorization
- **Issue**: Users should only sync data for their own sessions
- **Solution**: Validate session ownership in sync endpoints (check session.userId == currentUser.id)
- **Note**: User authentication deferred to v2 per constitution, but add validation hooks for future

### Input Validation
- **Issue**: AI-generated data could violate business rules
- **Solution**: Reuse existing service-layer validation (e.g., ProductService validates business model)
- **Pattern**: All sync operations delegate to existing services which have validation

### Audit Trail
- **Issue**: Need to track who created what via AI
- **Solution**: Populate `createdBy` field with "AI:session:{sessionId}" pattern
- **Benefit**: Clear provenance for debugging and compliance

---

## Summary

All technical unknowns resolved. Implementation will:
1. Extend existing services rather than create parallel infrastructure
2. Use proven Spring patterns (transactions, caching, exception handling)
3. Leverage existing entity validation and Numscript generation
4. Follow OpenAPI-first approach for new endpoints
5. Maintain zero new dependencies

Ready to proceed to Phase 1 (data model and contracts).

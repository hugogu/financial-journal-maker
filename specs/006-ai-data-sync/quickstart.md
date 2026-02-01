# Quickstart: AI-System Data Bidirectional Sync

**Feature**: 006-ai-data-sync  
**Date**: 2026-02-01  
**Audience**: Developers implementing this feature

## Overview

This feature enables bidirectional data synchronization between AI conversation sessions and system entities. AI can read existing products, scenarios, accounts, and rules for context, and user-confirmed AI suggestions are automatically persisted to the database.

## Architecture Summary

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Vue/Nuxt)                      │
│  ┌──────────────────┐         ┌─────────────────────┐      │
│  │ ConversationPanel│◄────────┤ SyncStatusIndicator │      │
│  └────────┬─────────┘         └─────────────────────┘      │
│           │ useAISync composable                            │
└───────────┼─────────────────────────────────────────────────┘
            │ REST API
┌───────────▼─────────────────────────────────────────────────┐
│                  Backend (Spring Boot)                      │
│  ┌──────────────────┐         ┌─────────────────────┐      │
│  │ DataSyncController├────────►│ DataSyncService     │      │
│  └──────────────────┘         └──────┬──────────────┘      │
│                                       │                      │
│  ┌──────────────────┐         ┌──────▼──────────────┐      │
│  │ConflictDetection │◄────────┤ Entity Services     │      │
│  │     Service      │         │ (Product, Account,  │      │
│  └──────────────────┘         │  Rule, Scenario)    │      │
│                               └─────────────────────┘      │
│  ┌──────────────────────────────────────────────────┐      │
│  │         Database (PostgreSQL)                    │      │
│  │  - data_sync_contexts                            │      │
│  │  - sync_operations                               │      │
│  │  - conflict_resolutions                          │      │
│  │  - products/scenarios/accounts/rules (extended)  │      │
│  └──────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## Implementation Phases

### Phase 1: Database Schema (1-2 days)
1. Create migration script for new tables
2. Add AI provenance columns to existing tables
3. Run migration on dev environment
4. Verify schema with test data

**Files to create**:
- `backend/src/main/resources/db/migration/V{N}__add_ai_sync_tables.sql`

**Verification**:
```sql
-- Check tables exist
\dt data_sync_contexts
\dt sync_operations
\dt conflict_resolutions

-- Check columns added
\d products
\d scenarios
\d accounts
```

---

### Phase 2: Domain Entities (1 day)
Create JPA entities for new tables.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/domain/DataSyncContext.java`
- `backend/src/main/java/com/financial/ai/sync/domain/SyncOperation.java`
- `backend/src/main/java/com/financial/ai/sync/domain/ConflictResolution.java`
- `backend/src/main/java/com/financial/ai/sync/domain/OperationType.java` (enum)
- `backend/src/main/java/com/financial/ai/sync/domain/ConflictType.java` (enum)
- `backend/src/main/java/com/financial/ai/sync/domain/SyncStatus.java` (enum)

**Key patterns**:
```java
@Entity
@Table(name = "data_sync_contexts")
public class DataSyncContext {
    @Id
    private Long sessionId;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, List<Long>> loadedEntities;
    
    // ... other fields
}
```

---

### Phase 3: Repositories (0.5 days)
Create Spring Data JPA repositories.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/repository/DataSyncContextRepository.java`
- `backend/src/main/java/com/financial/ai/sync/repository/SyncOperationRepository.java`
- `backend/src/main/java/com/financial/ai/sync/repository/ConflictResolutionRepository.java`

**Example**:
```java
@Repository
public interface DataSyncContextRepository extends JpaRepository<DataSyncContext, Long> {
    Optional<DataSyncContext> findBySessionId(Long sessionId);
}
```

---

### Phase 4: DTOs (1 day)
Create request/response DTOs matching OpenAPI spec.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/dto/LoadEntitiesRequest.java`
- `backend/src/main/java/com/financial/ai/sync/dto/LoadEntitiesResponse.java`
- `backend/src/main/java/com/financial/ai/sync/dto/SyncSuggestionsRequest.java`
- `backend/src/main/java/com/financial/ai/sync/dto/SyncSuggestionsResponse.java`
- `backend/src/main/java/com/financial/ai/sync/dto/EntitySuggestion.java`
- `backend/src/main/java/com/financial/ai/sync/dto/SyncResult.java`
- `backend/src/main/java/com/financial/ai/sync/dto/ConflictResponse.java`
- `backend/src/main/java/com/financial/ai/sync/dto/SyncContextResponse.java`

**Validation example**:
```java
public class SyncSuggestionsRequest {
    @NotNull
    private Long sessionId;
    
    @NotEmpty
    @Valid
    private List<EntitySuggestion> suggestions;
    
    // ... getters/setters
}
```

---

### Phase 5: Conflict Detection Service (2 days)
Implement conflict detection logic.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/service/ConflictDetectionService.java`
- `backend/src/main/java/com/financial/ai/sync/exception/ConflictException.java`

**Key methods**:
```java
@Service
public class ConflictDetectionService {
    
    public void detectProductConflicts(ProductCreateRequest request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new ConflictException(
                ConflictType.DUPLICATE_CODE,
                "Product",
                request.getCode(),
                findExistingProduct(request.getCode())
            );
        }
    }
    
    public void detectRuleConflicts(RuleCreateRequest request) {
        // Check for overlapping transaction type filters
        List<AccountingRule> overlapping = findOverlappingRules(request);
        if (!overlapping.isEmpty()) {
            throw new ConflictException(
                ConflictType.OVERLAPPING_RULE,
                "AccountingRule",
                request.getCode(),
                overlapping.get(0)
            );
        }
    }
}
```

---

### Phase 6: Data Sync Service (3 days)
Core synchronization logic.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/service/DataSyncService.java`
- `backend/src/main/java/com/financial/ai/sync/service/EntityFactory.java`

**Key methods**:
```java
@Service
@RequiredArgsConstructor
public class DataSyncService {
    
    private final ProductService productService;
    private final ScenarioService scenarioService;
    private final AccountService accountService;
    private final AccountingRuleService ruleService;
    private final ConflictDetectionService conflictDetection;
    private final DataSyncContextRepository contextRepository;
    
    @Transactional
    public SyncSuggestionsResponse syncSuggestions(SyncSuggestionsRequest request) {
        DataSyncContext context = getOrCreateContext(request.getSessionId());
        List<SyncResult> results = new ArrayList<>();
        
        for (EntitySuggestion suggestion : request.getSuggestions()) {
            try {
                // 1. Detect conflicts
                detectConflicts(suggestion);
                
                // 2. Create/update entity
                Object entity = createOrUpdateEntity(suggestion);
                
                // 3. Record operation
                recordOperation(context, suggestion, entity, SyncStatus.SUCCESS);
                
                results.add(SyncResult.success(suggestion, entity));
                
            } catch (ConflictException e) {
                // Record conflict and rethrow
                recordConflict(context, suggestion, e);
                throw e;
            } catch (Exception e) {
                recordOperation(context, suggestion, null, SyncStatus.FAILED);
                results.add(SyncResult.failed(suggestion, e.getMessage()));
            }
        }
        
        return new SyncSuggestionsResponse(request.getSessionId(), results);
    }
    
    private Object createOrUpdateEntity(EntitySuggestion suggestion) {
        return switch (suggestion.getEntityType()) {
            case PRODUCT -> productService.createProduct(suggestion.toProductRequest());
            case SCENARIO -> scenarioService.createScenario(suggestion.toScenarioRequest());
            case ACCOUNT -> accountService.createAccount(suggestion.toAccountRequest());
            case ACCOUNTING_RULE -> createRuleWithNumscript(suggestion);
            case TRANSACTION_TYPE -> transactionTypeService.createType(suggestion.toTypeRequest());
        };
    }
}
```

---

### Phase 7: REST Controller (1 day)
Expose sync endpoints.

**Files to create**:
- `backend/src/main/java/com/financial/ai/sync/controller/DataSyncController.java`

**Implementation**:
```java
@RestController
@RequestMapping("/api/ai/sync")
@RequiredArgsConstructor
public class DataSyncController {
    
    private final DataSyncService syncService;
    
    @PostMapping("/suggestions")
    @Operation(summary = "Sync AI suggestions to system entities")
    public ResponseEntity<SyncSuggestionsResponse> syncSuggestions(
            @Valid @RequestBody SyncSuggestionsRequest request) {
        SyncSuggestionsResponse response = syncService.syncSuggestions(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/context/{sessionId}")
    public ResponseEntity<SyncContextResponse> getContext(@PathVariable Long sessionId) {
        return ResponseEntity.ok(syncService.getContext(sessionId));
    }
    
    // ... other endpoints
}
```

---

### Phase 8: Extend SystemDataService (1 day)
Add caching to existing context loading.

**Files to modify**:
- `backend/src/main/java/com/financial/ai/service/SystemDataService.java`

**Changes**:
```java
@Service
public class SystemDataService {
    
    @Cacheable(value = "systemContext", key = "#phase")
    public SystemDataContext buildContextForPhase(String phase) {
        // Existing logic - now cached
    }
    
    @CacheEvict(value = "systemContext", allEntries = true)
    public void invalidateCache() {
        // Called after sync operations
    }
}
```

**Configuration**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("systemContext");
    }
}
```

---

### Phase 9: Frontend Components (2-3 days)

**Files to create**:
- `frontend/components/ai/SyncStatusIndicator.vue`
- `frontend/composables/useAISync.ts`
- `frontend/stores/aiSyncStore.ts`

**SyncStatusIndicator.vue**:
```vue
<template>
  <div v-if="syncState.isLoading" class="sync-indicator loading">
    📖 Loading: {{ syncState.loadingMessage }}
  </div>
  <div v-else-if="syncState.lastSync" class="sync-indicator success">
    ✅ {{ syncState.lastSync.message }}
    <NuxtLink :to="syncState.lastSync.entityUrl">View</NuxtLink>
  </div>
  <div v-else-if="syncState.error" class="sync-indicator error">
    ❌ {{ syncState.error }}
  </div>
</template>

<script setup lang="ts">
import { useAISync } from '~/composables/useAISync'

const { syncState } = useAISync()
</script>
```

**useAISync.ts**:
```typescript
export function useAISync() {
  const syncStore = useAISyncStore()
  
  async function syncSuggestion(sessionId: number, suggestion: EntitySuggestion) {
    try {
      syncStore.setLoading(true, `Creating ${suggestion.entityType}...`)
      
      const response = await $fetch('/api/ai/sync/suggestions', {
        method: 'POST',
        body: { sessionId, suggestions: [suggestion] }
      })
      
      syncStore.setSuccess(response.results[0])
      
    } catch (error: any) {
      if (error.status === 409) {
        // Conflict detected - show resolution modal
        const conflict = error.data
        const resolution = await showConflictModal(conflict)
        
        // Retry with resolution
        await syncSuggestion(sessionId, {
          ...suggestion,
          conflictResolution: resolution
        })
      } else {
        syncStore.setError(error.message)
      }
    } finally {
      syncStore.setLoading(false)
    }
  }
  
  return {
    syncState: computed(() => syncStore.state),
    syncSuggestion
  }
}
```

---

### Phase 10: Testing (2-3 days)

**Unit tests**:
- `backend/src/test/java/com/financial/ai/sync/service/ConflictDetectionServiceTest.java`
- `backend/src/test/java/com/financial/ai/sync/service/DataSyncServiceTest.java`

**Integration tests**:
- `backend/src/test/java/com/financial/ai/sync/controller/DataSyncControllerIntegrationTest.java`

**Test example**:
```java
@SpringBootTest
@Transactional
class DataSyncServiceTest {
    
    @Autowired
    private DataSyncService syncService;
    
    @Test
    void shouldCreateProductFromAISuggestion() {
        // Given
        EntitySuggestion suggestion = EntitySuggestion.builder()
            .entityType(EntityType.PRODUCT)
            .action(SyncAction.CREATE)
            .data(Map.of(
                "code", "TEST_PRODUCT",
                "name", "Test Product"
            ))
            .build();
        
        SyncSuggestionsRequest request = new SyncSuggestionsRequest(
            sessionId, List.of(suggestion)
        );
        
        // When
        SyncSuggestionsResponse response = syncService.syncSuggestions(request);
        
        // Then
        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getStatus()).isEqualTo("SUCCESS");
        
        Product product = productRepository.findByCode("TEST_PRODUCT").orElseThrow();
        assertThat(product.getAiCreated()).isTrue();
        assertThat(product.getAiSessionId()).isEqualTo(sessionId);
    }
    
    @Test
    void shouldDetectDuplicateCodeConflict() {
        // Given - existing product
        productRepository.save(Product.builder()
            .code("EXISTING")
            .name("Existing Product")
            .build());
        
        // When - AI suggests same code
        EntitySuggestion suggestion = EntitySuggestion.builder()
            .entityType(EntityType.PRODUCT)
            .action(SyncAction.CREATE)
            .data(Map.of("code", "EXISTING", "name", "New Product"))
            .build();
        
        // Then - should throw conflict
        assertThatThrownBy(() -> 
            syncService.syncSuggestions(new SyncSuggestionsRequest(sessionId, List.of(suggestion)))
        ).isInstanceOf(ConflictException.class)
         .hasMessageContaining("DUPLICATE_CODE");
    }
}
```

---

## Development Workflow

### 1. Setup Development Environment
```bash
# Start database
docker-compose up -d postgres

# Run backend
cd backend
./mvnw spring-boot:run

# Run frontend
cd frontend
npm run dev
```

### 2. Run Migrations
```bash
cd backend
./mvnw flyway:migrate
```

### 3. Test Endpoints
```bash
# Initialize sync context
curl -X POST http://localhost:8080/api/ai/sync/context/1

# Load entities
curl -X POST http://localhost:8080/api/ai/sync/load \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "entityTypes": ["PRODUCT", "ACCOUNT"]
  }'

# Sync suggestion
curl -X POST http://localhost:8080/api/ai/sync/suggestions \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": 1,
    "suggestions": [{
      "entityType": "PRODUCT",
      "action": "CREATE",
      "data": {
        "code": "AI_PRODUCT",
        "name": "AI Generated Product"
      }
    }]
  }'
```

---

## Common Pitfalls

### 1. Transaction Boundaries
**Problem**: Partial entity creation when one fails  
**Solution**: Ensure `@Transactional` on service methods, not controllers

### 2. JSONB Type Mapping
**Problem**: Hibernate doesn't map JSONB by default  
**Solution**: Use `@Type(JsonType.class)` annotation (requires hibernate-types dependency)

### 3. Optimistic Locking Conflicts
**Problem**: Concurrent updates cause version conflicts  
**Solution**: Catch `OptimisticLockException` and retry with exponential backoff

### 4. Cache Invalidation
**Problem**: Stale data in AI prompts after sync  
**Solution**: Call `systemDataService.invalidateCache()` after successful sync

### 5. Circular Dependencies
**Problem**: DataSyncService depends on ProductService which might depend on DataSyncService  
**Solution**: Use `@Lazy` injection or event-driven approach

---

## Performance Optimization

### Database Indexes
Already defined in schema, but verify with:
```sql
EXPLAIN ANALYZE 
SELECT * FROM sync_operations 
WHERE session_id = 1 
ORDER BY created_at DESC;
```

### Caching Strategy
- System context: 5-minute TTL
- Invalidate on write operations
- Consider Redis for production (deferred to v2)

### Batch Operations
For multiple suggestions, process in single transaction:
```java
@Transactional
public SyncSuggestionsResponse syncSuggestions(SyncSuggestionsRequest request) {
    // All suggestions succeed or all fail
}
```

---

## Monitoring & Observability

### Metrics to Track
- Sync operation success rate
- Conflict detection rate by type
- Average sync operation duration
- Cache hit rate for system context

### Logging
```java
log.info("Syncing {} suggestions for session {}", 
    request.getSuggestions().size(), request.getSessionId());
log.debug("Creating entity: type={}, code={}", 
    suggestion.getEntityType(), suggestion.getData().get("code"));
log.error("Sync failed for session {}: {}", sessionId, e.getMessage(), e);
```

---

## Deployment Checklist

- [ ] Database migration applied
- [ ] Integration tests passing
- [ ] OpenAPI spec validated
- [ ] Cache configuration verified
- [ ] Error handling tested (conflict scenarios)
- [ ] Rollback functionality tested
- [ ] Frontend components integrated
- [ ] Performance benchmarks met (<500ms sync operations)

---

## Next Steps After Implementation

1. Run `/speckit.tasks` to generate detailed task breakdown
2. Create GitHub issues from tasks
3. Assign to team members
4. Begin Phase 1 (database schema)

## References

- [Spec](./spec.md) - Feature requirements
- [Research](./research.md) - Technical decisions
- [Data Model](./data-model.md) - Entity definitions
- [API Contract](./contracts/api-spec.yaml) - OpenAPI specification

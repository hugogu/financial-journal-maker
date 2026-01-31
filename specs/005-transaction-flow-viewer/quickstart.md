# Quickstart: Transaction Flow Viewer

**Feature**: 005-transaction-flow-viewer  
**Date**: 2026-01-31

## Overview

This feature adds frontend pages and backend APIs for browsing and visualizing transaction flow designs created through AI analysis sessions. It is a **read-only** feature that aggregates data from existing entities.

## Prerequisites

- Feature 001-coa-management: Account data structure
- Feature 004-ai-analysis-session: DesignDecision and ExportArtifact entities
- Running Docker environment (`docker-compose up`)

## Quick Setup

### 1. Install Frontend Dependencies

```bash
cd frontend
npm install @vue-flow/core @vue-flow/background dagre prismjs
npm install -D @types/dagre
```

### 2. No Database Migration Required

This feature reads from existing tables. No new migrations needed.

### 3. Verify API Availability

After starting the backend, the new endpoints are available at:
- `GET /api/v1/transaction-flows/products` - List products
- `GET /api/v1/transaction-flows` - List all transaction flows
- `GET /api/v1/transaction-flows/{code}` - Get flow details
- `GET /api/v1/sessions/{id}/preview` - Get design preview

## Frontend Routes

| Route | Page | Description |
|-------|------|-------------|
| `/flows` | Transaction Flow Browser | Hierarchical product/scenario navigation |
| `/flows/:code` | Transaction Flow Detail | Accounts, entries, Numscript, diagram views |
| `/analysis/:id` | AI Session (extended) | Now includes preview panel |

## Key Components

### Transaction Flow Browser

```vue
<!-- pages/flows/index.vue -->
<template>
  <div class="flex">
    <!-- Left: Hierarchy Tree -->
    <ProductScenarioTree @select="onSelect" />
    
    <!-- Right: Transaction List -->
    <TransactionFlowList :filter="selectedFilter" />
  </div>
</template>
```

### Transaction Flow Detail

```vue
<!-- pages/flows/[code].vue -->
<template>
  <div>
    <h1>{{ flow.transactionTypeName }}</h1>
    
    <Tabs>
      <Tab name="accounts">
        <AccountsTable :accounts="flow.accounts" />
      </Tab>
      <Tab name="entries">
        <JournalEntriesTable :entries="flow.journalEntries" />
      </Tab>
      <Tab name="numscript">
        <NumscriptViewer :code="flow.numscript" :valid="flow.numscriptValid" />
      </Tab>
      <Tab name="diagram">
        <FlowDiagram :nodes="flow.accounts" :edges="flow.flowConnections" />
      </Tab>
    </Tabs>
  </div>
</template>
```

### Flow Diagram Component

```vue
<!-- components/FlowDiagram.vue -->
<template>
  <VueFlow :nodes="nodes" :edges="edges" fit-view-on-init>
    <Background />
    <Controls />
    
    <template #node-account="{ data }">
      <AccountNode :account="data" />
    </template>
  </VueFlow>
</template>

<script setup>
import { VueFlow } from '@vue-flow/core'
import { Background, Controls } from '@vue-flow/core'
import dagre from 'dagre'

// Auto-layout using dagre
function computeLayout(nodes, edges) {
  const g = new dagre.graphlib.Graph()
  g.setGraph({ rankdir: 'LR', nodesep: 80, ranksep: 120 })
  g.setDefaultEdgeLabel(() => ({}))
  
  nodes.forEach(node => {
    g.setNode(node.id, { width: 150, height: 60 })
  })
  
  edges.forEach(edge => {
    g.setEdge(edge.source, edge.target)
  })
  
  dagre.layout(g)
  
  return nodes.map(node => ({
    ...node,
    position: { x: g.node(node.id).x, y: g.node(node.id).y }
  }))
}
</script>
```

### Account Node Styling

```vue
<!-- components/AccountNode.vue -->
<template>
  <div 
    class="account-node"
    :class="[typeClass, stateClass]"
  >
    <div class="account-code">{{ account.accountCode }}</div>
    <div class="account-name">{{ account.accountName }}</div>
  </div>
</template>

<script setup>
const typeColors = {
  CUSTOMER: 'bg-teal-100 border-teal-500',
  BANK: 'bg-gray-100 border-gray-500',
  CHANNEL: 'bg-blue-100 border-blue-500',
  REVENUE: 'bg-green-100 border-green-500',
  COST: 'bg-pink-100 border-pink-500',
  OTHER: 'bg-gray-100 border-gray-400'
}

const stateStyles = {
  AVAILABLE: 'border-solid',
  FROZEN: 'border-dotted',
  IN_TRANSIT: 'border-dashed'
}
</script>
```

### Real-time Preview Panel

```vue
<!-- components/DesignPreviewPanel.vue -->
<template>
  <aside class="preview-panel" :class="{ collapsed: isCollapsed }">
    <div class="panel-header">
      <h3>设计预览</h3>
      <button @click="isCollapsed = !isCollapsed">
        {{ isCollapsed ? '展开' : '收起' }}
      </button>
    </div>
    
    <div v-if="!isCollapsed" class="panel-content">
      <!-- Accounts Section -->
      <section>
        <h4>科目</h4>
        <AccountsList 
          :confirmed="preview.confirmedAccounts"
          :tentative="preview.tentativeAccounts"
        />
      </section>
      
      <!-- Entries Section -->
      <section>
        <h4>分录</h4>
        <EntriesSummary
          :confirmed="preview.confirmedEntries"
          :tentative="preview.tentativeEntries"
        />
      </section>
      
      <!-- Mini Diagram -->
      <section>
        <h4>流程图</h4>
        <MiniFlowDiagram :accounts="allAccounts" />
      </section>
    </div>
  </aside>
</template>

<script setup>
import { useEventSource } from '@vueuse/core'

const props = defineProps<{ sessionId: number }>()

// SSE connection for real-time updates
const { data } = useEventSource(
  `/api/v1/sessions/${props.sessionId}/preview/stream`
)

const preview = computed(() => 
  data.value ? JSON.parse(data.value) : defaultPreview
)
</script>
```

### Numscript Syntax Highlighting

```javascript
// plugins/prism-numscript.js
import Prism from 'prismjs'

Prism.languages.numscript = {
  'comment': /\/\/.*/,
  'keyword': /\b(vars|send|source|destination|remaining|from|to|max|allowing|overdraft|unbounded|kept)\b/,
  'variable': /\$[a-zA-Z_][a-zA-Z0-9_]*/,
  'account': /@[a-zA-Z0-9:_-]+/,
  'number': /\b\d+(\.\d+)?\b/,
  'operator': /[+\-*\/=<>]/,
  'punctuation': /[{}()\[\],]/
}
```

## Backend Services

### TransactionFlowService

```java
@Service
@RequiredArgsConstructor
public class TransactionFlowService {
    
    private final DesignDecisionRepository decisionRepository;
    private final ExportArtifactRepository artifactRepository;
    
    public Page<ProductSummary> listProducts(String search, Pageable pageable) {
        return decisionRepository.findProductSummaries(search, pageable);
    }
    
    public TransactionFlowView getTransactionFlow(String code) {
        // Aggregate from DesignDecision entities
        var decisions = decisionRepository.findByTransactionTypeCode(code);
        return TransactionFlowView.from(decisions);
    }
    
    public NumscriptView getNumscript(String code, boolean regenerate) {
        if (!regenerate) {
            var artifact = artifactRepository.findByTransactionTypeCode(code);
            if (artifact.isPresent()) {
                return NumscriptView.fromArtifact(artifact.get());
            }
        }
        // Generate on-demand
        var flow = getTransactionFlow(code);
        return numscriptGenerator.generate(flow);
    }
}
```

### Preview SSE Controller

```java
@RestController
@RequestMapping("/api/v1/sessions")
public class PreviewController {
    
    @GetMapping(value = "/{sessionId}/preview/stream", 
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DesignPreview>> streamPreview(
            @PathVariable Long sessionId) {
        return previewService.getPreviewStream(sessionId)
            .map(preview -> ServerSentEvent.<DesignPreview>builder()
                .data(preview)
                .build());
    }
}
```

## Testing

### Manual Testing Checklist

1. **Browse Products**: Navigate to `/flows`, verify product list loads
2. **Drill Down**: Click product → scenarios → transaction types
3. **View Details**: Select transaction, verify all tabs (Accounts, Entries, Numscript, Diagram)
4. **Copy Numscript**: Click copy button, paste elsewhere
5. **Flow Diagram**: Verify nodes colored by type, arrows show correctly
6. **Preview Panel**: Start AI session, verify preview updates during conversation

### API Testing

```bash
# List products
curl http://localhost:8080/api/v1/transaction-flows/products

# Get transaction flow detail
curl http://localhost:8080/api/v1/transaction-flows/LOAN_DISBURSEMENT

# Get Numscript
curl http://localhost:8080/api/v1/transaction-flows/LOAN_DISBURSEMENT/numscript

# Get flow diagram data
curl http://localhost:8080/api/v1/transaction-flows/LOAN_DISBURSEMENT/diagram
```

## Common Issues

### Flow diagram not rendering
- Ensure `@vue-flow/core` is installed
- Check browser console for Vue Flow errors
- Verify nodes have valid positions after layout

### Numscript highlighting not working
- Ensure Prism.js is loaded
- Verify custom language definition is registered
- Check for CSS style imports

### Preview not updating
- Verify SSE connection established
- Check backend emitting events
- Ensure sessionId is correct

## Related Documentation

- [spec.md](./spec.md) - Feature specification
- [data-model.md](./data-model.md) - View model definitions
- [contracts/transaction-flow-api.yaml](./contracts/transaction-flow-api.yaml) - OpenAPI spec
- [004-ai-analysis-session](../004-ai-analysis-session/) - Source data feature

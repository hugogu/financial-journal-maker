# Research: Transaction Flow Viewer

**Feature**: 005-transaction-flow-viewer  
**Date**: 2026-01-31

## Research Questions

### 1. Flow Diagram Library Selection

**Question**: Which JavaScript library should be used for rendering interactive node-based flow diagrams?

**Decision**: Vue Flow (based on React Flow)

**Rationale**:
- Native Vue 3 support aligns with constitution's Vue/Nuxt stack
- Built-in support for custom nodes, edges, zooming, panning
- Handles automatic layout via dagre integration
- Active maintenance and large community
- Supports both straight and curved arrows for complex flows

**Alternatives Considered**:
| Library | Pros | Cons | Rejected Because |
|---------|------|------|------------------|
| D3.js | Extremely flexible, low-level | Steep learning curve, manual layout | Too much boilerplate for standard flow diagrams |
| Mermaid.js | Simple markdown syntax | Limited interactivity, fixed styling | Cannot meet hover/tooltip/zoom requirements |
| GoJS | Commercial, feature-rich | Paid license, overkill | License cost, simpler solution available |
| JointJS | Powerful diagramming | Complex API, heavy | Heavier than needed for read-only viewing |

---

### 2. Numscript Syntax Highlighting

**Question**: How to implement syntax highlighting for Numscript DSL in the browser?

**Decision**: Custom Prism.js language definition + Monaco Editor for advanced view

**Rationale**:
- Prism.js is lightweight for read-only display
- Monaco Editor (VS Code editor) provides copy functionality and advanced features
- Custom language grammar can match Numscript keywords: `send`, `source`, `destination`, `vars`, `remaining`
- Both integrate well with Vue components

**Alternatives Considered**:
| Option | Pros | Cons | Rejected Because |
|--------|------|------|------------------|
| CodeMirror 6 | Modern, extensible | Steeper Vue integration | Monaco has better out-of-box features |
| highlight.js | Simple, many languages | No Numscript support | Prism easier to extend |
| Plain `<pre>` | Zero dependencies | No highlighting | Doesn't meet FR-011 |

---

### 3. Real-time Preview Update Strategy

**Question**: How should the preview panel update during streaming AI responses?

**Decision**: Debounced incremental parsing with structured extraction

**Rationale**:
- AI response stream is parsed for structured data (JSON blocks, markdown tables)
- Preview updates at 200ms debounce to avoid excessive re-renders
- Confirmed elements are persisted immediately; tentative elements are volatile
- WebSocket/SSE stream already established by 004-ai-analysis-session

**Implementation Approach**:
1. AI response includes structured markers: `<!-- ACCOUNTS_START -->...<!-- ACCOUNTS_END -->`
2. Frontend parser extracts structured sections incrementally
3. Preview component receives updates via Vue reactive state
4. Debounce prevents render thrashing during rapid streaming

---

### 4. Account Type Color Scheme

**Question**: What color scheme for account nodes in flow diagrams?

**Decision**: Follow provided Whimsical diagram conventions

| Account Type | Color | Hex | Border Style |
|--------------|-------|-----|--------------|
| Customer Account | Teal | #14B8A6 | Solid |
| Bank Account | Gray | #9CA3AF | Solid |
| Channel Account | Blue | #3B82F6 | Solid |
| Revenue (P&L) | Green | #22C55E | Solid |
| Cost (P&L) | Pink/Red | #F472B6 | Solid |
| Frozen State | - | - | Dotted border |
| In-transit State | - | - | Dashed border |

**Rationale**: Matches user's existing Whimsical diagrams for consistency.

---

### 5. Data Source Strategy

**Question**: Should this feature have its own data tables or read from AI session data?

**Decision**: Read-only views over existing 004-ai-analysis-session data

**Rationale**:
- DesignDecision entity already captures confirmed accounts and entries
- ExportArtifact stores generated Numscript
- No data duplication needed
- Add computed/aggregated views as needed via JPA projections

**Data Flow**:
```
DesignDecision (004) ──read──▶ TransactionFlowView (005)
                                        │
                                        ├── AccountNode (computed)
                                        ├── JournalEntryDisplay (computed)
                                        └── Numscript (from ExportArtifact or on-demand generation)
```

---

### 6. Numscript Generation Strategy

**Question**: Generate Numscript on-demand or store pre-generated?

**Decision**: Hybrid - store on export, generate on-demand for preview

**Rationale**:
- Exported Numscript stored in ExportArtifact for finalized sessions
- Preview during active sessions generates on-demand from current DesignDecisions
- Numscript generator service already planned in 004-ai-analysis-session
- Avoids stale cached Numscript during design iteration

---

### 7. Timeline Diagram Implementation

**Question**: How to render transaction timeline diagrams?

**Decision**: Simple horizontal timeline using CSS + Vue components (P3 priority)

**Rationale**:
- Lower priority feature (P3)
- Simple implementation sufficient: flexbox with time markers
- Events as nodes on timeline with click-to-highlight
- No complex library needed for initial implementation
- Can upgrade to more sophisticated library if needed post-MVP

---

## Technical Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| @vue-flow/core | ^1.x | Flow diagram rendering |
| @vue-flow/background | ^1.x | Grid background for diagrams |
| dagre | ^0.8.x | Automatic graph layout |
| prismjs | ^1.29.x | Syntax highlighting |
| @monaco-editor/vue | ^1.x | Advanced code editing/viewing |

---

## Integration Points with 004-ai-analysis-session

1. **DesignDecision entity**: Source for accounts, entries, transaction structure
2. **ExportArtifact entity**: Source for finalized Numscript
3. **Streaming endpoint**: Extend to emit structured preview data
4. **Session detail page**: Embed preview panel component

---

## Constitution Compliance Check

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Domain Design Assistant | ✅ | Read-only viewing, no transaction processing |
| II. Hierarchical Consistency | ✅ | Displays Product → Scenario → Type hierarchy |
| III. AI-Human Collaboration | ✅ | Shows AI proposals vs confirmed decisions |
| IV. Numscript DSL Output | ✅ | Displays and validates Numscript |
| V. OpenAPI-First Backend | ✅ | APIs defined before implementation |
| VI. Containerized Deployment | ✅ | No new services, existing Docker setup |

---

## Open Questions (None)

All technical questions resolved. Ready for Phase 1 design.

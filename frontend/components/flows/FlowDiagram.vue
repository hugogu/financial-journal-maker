<template>
  <div class="flow-diagram">
    <div class="diagram-header">
      <h3>Transaction Flow Diagram</h3>
      <div class="zoom-controls">
        <Button icon="pi pi-minus" text size="small" @click="zoomOut" />
        <span class="zoom-level">{{ Math.round(zoom * 100) }}%</span>
        <Button icon="pi pi-plus" text size="small" @click="zoomIn" />
        <Button icon="pi pi-refresh" text size="small" @click="fitView" />
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <i class="pi pi-spin pi-spinner" />
      <span>Loading diagram...</span>
    </div>

    <div v-else-if="error" class="error-state">
      <i class="pi pi-exclamation-circle" />
      <span>{{ error }}</span>
    </div>

    <div v-else-if="nodes.length === 0" class="empty-state">
      <i class="pi pi-sitemap" />
      <span>No flow diagram available for this transaction</span>
    </div>

    <div v-else ref="diagramContainer" class="diagram-container">
      <VueFlow
        v-model="elements"
        :default-zoom="zoom"
        :min-zoom="0.2"
        :max-zoom="2"
        :fit-view-on-init="true"
        :nodes-draggable="false"
        :nodes-connectable="false"
        :elements-selectable="false"
        @zoom="onZoom"
      >
        <Background pattern-color="#e5e7eb" :gap="20" />
        
        <template #node-account="{ data }">
          <AccountNode :data="data" />
        </template>

        <template #edge-custom="{ data }">
          <div class="edge-label" v-if="data?.label">{{ data.label }}</div>
        </template>
      </VueFlow>
    </div>

    <div class="diagram-legend">
      <div class="legend-item">
        <span class="legend-line cash"></span>
        <span>Cash Flow</span>
      </div>
      <div class="legend-item">
        <span class="legend-line info"></span>
        <span>Info Flow</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { VueFlow } from '@vue-flow/core';
import { Background } from '@vue-flow/background';

interface NodeData {
  id: string;
  accountCode: string;
  accountName: string;
  accountType: string;
  accountState?: string;
  x: number;
  y: number;
}

interface EdgeData {
  id: string;
  sourceId: string;
  targetId: string;
  flowType: string;
  amountExpression?: string;
  label?: string;
}

const props = defineProps<{
  nodes: NodeData[];
  edges: EdgeData[];
  loading?: boolean;
  error?: string | null;
}>();

const zoom = ref(1);
const diagramContainer = ref<HTMLElement | null>(null);

const elements = computed(() => {
  const vueFlowNodes = props.nodes.map(n => ({
    id: n.id,
    type: 'account',
    position: { x: n.x, y: n.y },
    data: n,
  }));

  const vueFlowEdges = props.edges.map(e => ({
    id: e.id,
    source: e.sourceId,
    target: e.targetId,
    type: 'smoothstep',
    animated: e.flowType === 'CASH',
    style: {
      stroke: e.flowType === 'CASH' ? '#10b981' : '#6b7280',
      strokeWidth: e.flowType === 'CASH' ? 3 : 2,
      strokeDasharray: e.flowType === 'INFO' ? '5,5' : undefined,
    },
    markerEnd: e.flowType === 'CASH' ? 'arrowclosed' : undefined,
    data: { label: e.label },
  }));

  return [...vueFlowNodes, ...vueFlowEdges];
});

const zoomIn = () => {
  zoom.value = Math.min(2, zoom.value + 0.1);
};

const zoomOut = () => {
  zoom.value = Math.max(0.2, zoom.value - 0.1);
};

const fitView = () => {
  zoom.value = 1;
};

const onZoom = (event: any) => {
  zoom.value = event.zoom;
};
</script>

<style scoped>
.flow-diagram {
  display: flex;
  flex-direction: column;
  height: 600px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #f9fafb;
}

.diagram-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.diagram-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.zoom-level {
  min-width: 50px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}

.diagram-container {
  flex: 1;
  position: relative;
}

.loading-state,
.error-state,
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #6b7280;
}

.error-state {
  color: #dc2626;
}

.diagram-legend {
  display: flex;
  gap: 20px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #374151;
}

.legend-line {
  width: 24px;
  height: 3px;
  border-radius: 2px;
}

.legend-line.cash {
  background: #10b981;
}

.legend-line.info {
  background: #6b7280;
  background-image: linear-gradient(to right, #6b7280 50%, transparent 50%);
  background-size: 8px 3px;
}
</style>

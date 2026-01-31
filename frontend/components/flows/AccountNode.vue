<template>
  <div 
    class="account-node"
    :class="[typeClass, stateClass]"
    :style="nodeStyle"
  >
    <div class="node-header">
      <span class="account-code">{{ data.accountCode }}</span>
    </div>
    <div class="node-body">
      <span class="account-name">{{ data.accountName }}</span>
    </div>
    <div class="node-footer">
      <Tag :value="data.accountType" severity="info" class="type-tag" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface NodeData {
  accountCode: string;
  accountName: string;
  accountType: string;
  accountState?: string;
}

const props = defineProps<{
  data: NodeData;
}>();

const typeClass = computed(() => {
  const type = props.data.accountType?.toLowerCase();
  return type ? `type-${type}` : '';
});

const stateClass = computed(() => {
  const state = props.data.accountState?.toLowerCase();
  return state ? `state-${state}` : '';
});

const nodeStyle = computed(() => {
  const colors: Record<string, string> = {
    customer: '#3b82f6',
    bank: '#10b981',
    channel: '#f59e0b',
    revenue: '#8b5cf6',
    cost: '#ef4444',
  };
  
  const color = colors[props.data.accountType?.toLowerCase()] || '#6b7280';
  
  return {
    '--node-color': color,
    borderColor: color,
  };
});
</script>

<style scoped>
.account-node {
  width: 140px;
  background: #fff;
  border: 2px solid var(--node-color, #6b7280);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.node-header {
  background: var(--node-color, #6b7280);
  color: #fff;
  padding: 6px 10px;
  font-weight: 600;
  font-size: 12px;
}

.node-body {
  padding: 8px 10px;
  min-height: 36px;
}

.account-name {
  font-size: 11px;
  color: #374151;
  line-height: 1.3;
}

.node-footer {
  padding: 4px 10px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.type-tag {
  font-size: 9px;
}

/* State variations */
.state-active {
  border-style: solid;
}

.state-pending {
  border-style: dashed;
}

.state-closed {
  border-style: dotted;
  opacity: 0.7;
}
</style>

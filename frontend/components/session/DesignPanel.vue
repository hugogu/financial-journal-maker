<template>
  <div class="design-panel">
    <h3>Design Progress</h3>
    
    <div class="phase-indicator">
      <div 
        v-for="phase in phases" 
        :key="phase.value"
        :class="['phase-item', { active: phase.value === currentPhase, completed: isPhaseCompleted(phase.value) }]"
      >
        <div class="phase-dot">
          <span v-if="isPhaseCompleted(phase.value)">✓</span>
        </div>
        <span class="phase-label">{{ phase.label }}</span>
      </div>
    </div>

    <div class="decisions-section">
      <h4>Confirmed Decisions</h4>
      <div v-if="confirmedDecisions.length === 0" class="no-decisions">
        No confirmed decisions yet
      </div>
      <div v-else class="decision-list">
        <div 
          v-for="decision in confirmedDecisions" 
          :key="decision.id"
          class="decision-card"
        >
          <div class="decision-type">{{ decision.decisionType }}</div>
          <div class="decision-entity" v-if="decision.entityType">
            {{ decision.entityType }}
          </div>
          <pre class="decision-content">{{ formatContent(decision.content) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSessionStore } from '~/stores/session'

const props = defineProps<{
  currentPhase: string
}>()

const store = useSessionStore()

const phases = [
  { value: 'PRODUCT', label: 'Product' },
  { value: 'SCENARIO', label: 'Scenario' },
  { value: 'TRANSACTION_TYPE', label: 'Transaction Type' },
  { value: 'ACCOUNTING', label: 'Accounting' },
]

const confirmedDecisions = computed(() => store.confirmedDecisions)

function isPhaseCompleted(phase: string) {
  return confirmedDecisions.value.some(d => d.decisionType === phase)
}

function formatContent(content: Record<string, unknown>) {
  return JSON.stringify(content, null, 2)
}
</script>

<style scoped>
.design-panel {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid #e5e7eb;
}

.design-panel h3 {
  margin: 0 0 1rem;
  font-size: 1rem;
  color: #374151;
}

.phase-indicator {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  position: relative;
}

.phase-indicator::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 24px;
  right: 24px;
  height: 2px;
  background: #e5e7eb;
  z-index: 0;
}

.phase-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
}

.phase-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  color: white;
  margin-bottom: 0.5rem;
}

.phase-item.active .phase-dot {
  background: #3b82f6;
}

.phase-item.completed .phase-dot {
  background: #10b981;
}

.phase-label {
  font-size: 0.625rem;
  color: #6b7280;
  text-align: center;
}

.phase-item.active .phase-label {
  color: #3b82f6;
  font-weight: 600;
}

.decisions-section h4 {
  font-size: 0.875rem;
  color: #374151;
  margin: 0 0 0.75rem;
}

.no-decisions {
  color: #9ca3af;
  font-size: 0.875rem;
  text-align: center;
  padding: 1rem;
}

.decision-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.decision-card {
  background: #f9fafb;
  border-radius: 6px;
  padding: 0.75rem;
  font-size: 0.75rem;
}

.decision-type {
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 0.25rem;
}

.decision-entity {
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.decision-content {
  background: #f3f4f6;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.625rem;
  overflow-x: auto;
  margin: 0;
}
</style>

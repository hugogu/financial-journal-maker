<template>
  <div class="export-dialog-overlay" @click.self="$emit('close')">
    <div class="export-dialog">
      <header class="dialog-header">
        <h2>Export Design</h2>
        <button @click="$emit('close')" class="close-btn">&times;</button>
      </header>

      <div class="dialog-content">
        <p class="description">
          Export your design decisions as different artifact types.
        </p>

        <div class="export-options">
          <div 
            v-for="option in exportOptions" 
            :key="option.type"
            :class="['export-option', { selected: selectedType === option.type }]"
            @click="selectedType = option.type"
          >
            <div class="option-icon">{{ option.icon }}</div>
            <div class="option-info">
              <div class="option-name">{{ option.name }}</div>
              <div class="option-desc">{{ option.description }}</div>
            </div>
          </div>
        </div>

        <div v-if="conflicts && conflicts.hasConflicts" class="conflicts-warning">
          <div class="warning-header">
            <span class="warning-icon">⚠️</span>
            <span>{{ conflicts.conflictCount }} potential conflict(s) detected</span>
          </div>
          <ul class="conflict-list">
            <li v-for="(detail, index) in conflicts.details" :key="index">
              {{ detail }}
            </li>
          </ul>
          <label class="force-checkbox">
            <input type="checkbox" v-model="forceOverwrite" />
            Force overwrite existing data
          </label>
        </div>

        <div v-if="exportResult" :class="['export-result', { success: exportResult.success }]">
          <div class="result-message">{{ exportResult.message }}</div>
          <div v-if="exportResult.content" class="result-content">
            <pre>{{ exportResult.content.slice(0, 500) }}{{ exportResult.content.length > 500 ? '...' : '' }}</pre>
          </div>
        </div>
      </div>

      <footer class="dialog-footer">
        <button @click="$emit('close')" class="btn btn-secondary">Cancel</button>
        <button 
          @click="handleExport" 
          :disabled="!selectedType || exporting"
          class="btn btn-primary"
        >
          {{ exporting ? 'Exporting...' : 'Export' }}
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useExport, type ExportType, type ExportResponse, type ExportConflictResponse } from '~/composables/useExport'

const props = defineProps<{
  sessionId: number
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'exported', response: ExportResponse): void
}>()

const { exporting, exportDesign, previewConflicts } = useExport()

const selectedType = ref<ExportType | null>(null)
const forceOverwrite = ref(false)
const conflicts = ref<ExportConflictResponse | null>(null)
const exportResult = ref<ExportResponse | null>(null)

const exportOptions = [
  { type: 'COA' as ExportType, name: 'Chart of Accounts', icon: '📊', description: 'Export as COA entries' },
  { type: 'RULES' as ExportType, name: 'Accounting Rules', icon: '📋', description: 'Export as accounting rules' },
  { type: 'NUMSCRIPT' as ExportType, name: 'Numscript', icon: '📝', description: 'Export as Numscript code' },
]

watch(selectedType, async (type) => {
  if (type) {
    exportResult.value = null
    forceOverwrite.value = false
    try {
      conflicts.value = await previewConflicts(props.sessionId, type)
    } catch (error) {
      console.error('Failed to preview conflicts:', error)
      conflicts.value = null
    }
  }
})

async function handleExport() {
  if (!selectedType.value) return

  try {
    const result = await exportDesign(props.sessionId, selectedType.value, forceOverwrite.value)
    exportResult.value = result
    if (result.success) {
      emit('exported', result)
    }
  } catch (error) {
    console.error('Export failed:', error)
  }
}
</script>

<style scoped>
.export-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.export-dialog {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
}

.dialog-content {
  padding: 1.5rem;
  overflow-y: auto;
}

.description {
  margin: 0 0 1rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.export-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.export-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.export-option:hover {
  border-color: #93c5fd;
}

.export-option.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.option-icon {
  font-size: 1.5rem;
}

.option-name {
  font-weight: 600;
  font-size: 0.875rem;
}

.option-desc {
  font-size: 0.75rem;
  color: #6b7280;
}

.conflicts-warning {
  margin-top: 1rem;
  padding: 1rem;
  background: #fef3c7;
  border-radius: 8px;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 0.875rem;
  color: #92400e;
}

.conflict-list {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
  font-size: 0.75rem;
  color: #78350f;
}

.force-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  cursor: pointer;
}

.export-result {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 8px;
  background: #fef2f2;
}

.export-result.success {
  background: #ecfdf5;
}

.result-message {
  font-weight: 500;
  font-size: 0.875rem;
}

.result-content {
  margin-top: 0.5rem;
}

.result-content pre {
  background: white;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.625rem;
  overflow-x: auto;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
}

.btn-primary { background: #3b82f6; color: white; }
.btn-primary:hover { background: #2563eb; }
.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }

.btn-secondary { background: #f3f4f6; color: #374151; }
.btn-secondary:hover { background: #e5e7eb; }
</style>

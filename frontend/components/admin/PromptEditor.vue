<template>
  <div class="prompt-editor">
    <div class="editor-header">
      <h3>{{ isEditing ? 'Edit Prompt' : 'Create New Prompt' }}</h3>
      <button @click="$emit('close')" class="close-btn">&times;</button>
    </div>

    <form @submit.prevent="handleSubmit" class="editor-form">
      <div class="form-group">
        <label for="name">Name</label>
        <input
          id="name"
          v-model="form.name"
          type="text"
          required
          maxlength="100"
          placeholder="e.g., Product Analysis"
          :disabled="isEditing"
        />
      </div>

      <div class="form-group">
        <label for="designPhase">Design Phase</label>
        <select
          id="designPhase"
          v-model="form.designPhase"
          required
          :disabled="isEditing"
        >
          <option value="">Select a phase</option>
          <option value="PRODUCT">Product</option>
          <option value="SCENARIO">Scenario</option>
          <option value="TRANSACTION_TYPE">Transaction Type</option>
          <option value="ACCOUNTING">Accounting</option>
        </select>
      </div>

      <div class="form-group">
        <label for="content">Prompt Content</label>
        <textarea
          id="content"
          v-model="form.content"
          required
          rows="15"
          placeholder="Enter your prompt template...

Available variables:
- {{userMessage}} - The user's message
- {{confirmedDecisions}} - Previously confirmed decisions
- {{existingProducts}} - Products in the system
- {{existingScenarios}} - Scenarios in the system
- {{existingTransactionTypes}} - Transaction types in the system
- {{chartOfAccounts}} - Available accounts"
        ></textarea>
      </div>

      <div class="variables-help">
        <h4>Available Variables</h4>
        <ul>
          <li><code>{{ '{{userMessage}}' }}</code> - The user's message</li>
          <li><code>{{ '{{confirmedDecisions}}' }}</code> - Previously confirmed decisions</li>
          <li><code>{{ '{{existingProducts}}' }}</code> - Products in the system</li>
          <li><code>{{ '{{existingScenarios}}' }}</code> - Scenarios in the system</li>
          <li><code>{{ '{{existingTransactionTypes}}' }}</code> - Transaction types</li>
          <li><code>{{ '{{chartOfAccounts}}' }}</code> - Available accounts</li>
        </ul>
      </div>

      <div class="form-actions">
        <button type="button" @click="$emit('close')" class="btn-cancel">
          Cancel
        </button>
        <button type="submit" class="btn-submit" :disabled="saving">
          {{ saving ? 'Saving...' : (isEditing ? 'Save New Version' : 'Create') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface Prompt {
  id?: number
  name: string
  designPhase: string
  content: string
  version?: number
  isActive?: boolean
}

const props = defineProps<{
  prompt?: Prompt | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', data: { name: string; designPhase: string; content: string }): void
}>()

const isEditing = ref(false)
const saving = ref(false)

const form = ref({
  name: '',
  designPhase: '',
  content: ''
})

onMounted(() => {
  if (props.prompt) {
    isEditing.value = true
    form.value = {
      name: props.prompt.name,
      designPhase: props.prompt.designPhase,
      content: props.prompt.content
    }
  }
})

watch(() => props.prompt, (newPrompt) => {
  if (newPrompt) {
    isEditing.value = true
    form.value = {
      name: newPrompt.name,
      designPhase: newPrompt.designPhase,
      content: newPrompt.content
    }
  } else {
    isEditing.value = false
    form.value = {
      name: '',
      designPhase: '',
      content: ''
    }
  }
}, { immediate: true })

async function handleSubmit() {
  saving.value = true
  try {
    emit('save', {
      name: form.value.name,
      designPhase: form.value.designPhase,
      content: form.value.content
    })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.prompt-editor {
  background: white;
  border-radius: 8px;
  padding: 24px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.editor-header h3 {
  margin: 0;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.close-btn:hover {
  color: #333;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group textarea {
  font-family: 'Monaco', 'Menlo', monospace;
  resize: vertical;
  min-height: 200px;
}

.form-group input:disabled,
.form-group select:disabled {
  background-color: #f3f4f6;
  cursor: not-allowed;
}

.variables-help {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px 16px;
}

.variables-help h4 {
  margin: 0 0 8px 0;
  font-size: 0.875rem;
  color: #475569;
}

.variables-help ul {
  margin: 0;
  padding-left: 20px;
  font-size: 0.85rem;
  color: #64748b;
}

.variables-help li {
  margin-bottom: 4px;
}

.variables-help code {
  background: #e2e8f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 0.8rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.btn-cancel,
.btn-submit {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-cancel:hover {
  background: #f3f4f6;
}

.btn-submit {
  background: #3b82f6;
  border: none;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #2563eb;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

<template>
  <div class="provider-form">
    <h3>{{ isEdit ? 'Edit Provider' : 'Add Provider' }}</h3>
    
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="providerName">Provider Name</label>
        <select id="providerName" v-model="form.providerName" required>
          <option value="">Select provider...</option>
          <option value="openai">OpenAI</option>
          <option value="azure">Azure OpenAI</option>
          <option value="anthropic">Anthropic</option>
          <option value="custom">Custom</option>
        </select>
      </div>

      <div class="form-group">
        <label for="modelName">Model Name</label>
        <input 
          id="modelName"
          v-model="form.modelName"
          type="text"
          placeholder="e.g., gpt-4, claude-3-opus"
          required
        />
      </div>

      <div class="form-group">
        <label for="apiKey">API Key</label>
        <input 
          id="apiKey"
          v-model="form.apiKey"
          type="password"
          placeholder="Enter API key"
          :required="!isEdit"
        />
        <small v-if="isEdit">Leave empty to keep existing key</small>
      </div>

      <div class="form-group">
        <label for="endpoint">Endpoint URL</label>
        <input 
          id="endpoint"
          v-model="form.endpoint"
          type="url"
          placeholder="https://api.openai.com/v1"
        />
      </div>

      <div class="form-actions">
        <button type="button" @click="$emit('cancel')" class="btn btn-secondary">
          Cancel
        </button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : (isEdit ? 'Update' : 'Create') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { AIConfig } from '~/stores/aiConfig'

const props = defineProps<{
  config?: AIConfig | null
}>()

const emit = defineEmits<{
  (e: 'submit', data: { providerName: string; modelName: string; apiKey: string; endpoint: string }): void
  (e: 'cancel'): void
}>()

const isEdit = computed(() => !!props.config)
const submitting = ref(false)

const form = reactive({
  providerName: '',
  modelName: '',
  apiKey: '',
  endpoint: '',
})

watch(() => props.config, (config) => {
  if (config) {
    form.providerName = config.providerName
    form.modelName = config.modelName
    form.endpoint = config.endpoint
    form.apiKey = ''
  } else {
    form.providerName = ''
    form.modelName = ''
    form.apiKey = ''
    form.endpoint = ''
  }
}, { immediate: true })

function handleSubmit() {
  submitting.value = true
  emit('submit', { ...form })
  submitting.value = false
}
</script>

<style scoped>
.provider-form {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
}

.provider-form h3 {
  margin: 0 0 1.5rem;
  font-size: 1.125rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.form-group small {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
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

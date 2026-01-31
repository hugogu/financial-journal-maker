<template>
  <div class="ai-config-page">
    <header class="page-header">
      <h1>AI Configuration</h1>
      <button @click="showForm = true" class="btn btn-primary">
        + Add Provider
      </button>
    </header>

    <div v-if="loading" class="loading">Loading configurations...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="config-content">
      <div v-if="configs.length === 0" class="empty-state">
        <p>No AI providers configured yet.</p>
        <button @click="showForm = true" class="btn btn-primary">
          Add your first provider
        </button>
      </div>

      <div v-else class="config-list">
        <div 
          v-for="config in configs" 
          :key="config.id"
          :class="['config-card', { active: config.isActive }]"
        >
          <div class="config-header">
            <div class="provider-info">
              <h3>{{ config.providerName }}</h3>
              <span class="model-name">{{ config.modelName }}</span>
            </div>
            <span v-if="config.isActive" class="active-badge">Active</span>
          </div>
          
          <div class="config-details">
            <div class="detail-row">
              <span class="label">Endpoint:</span>
              <span class="value">{{ config.endpoint || 'Default' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Created:</span>
              <span class="value">{{ formatDate(config.createdAt) }}</span>
            </div>
          </div>

          <div class="config-actions">
            <button 
              v-if="!config.isActive"
              @click="handleActivate(config.id)"
              class="btn btn-sm btn-success"
            >
              Activate
            </button>
            <button @click="handleTest(config.id)" class="btn btn-sm btn-secondary">
              Test
            </button>
            <button @click="handleEdit(config)" class="btn btn-sm btn-secondary">
              Edit
            </button>
            <button 
              v-if="!config.isActive"
              @click="handleDelete(config.id)"
              class="btn btn-sm btn-danger"
            >
              Delete
            </button>
          </div>

          <div v-if="testResults[config.id]" :class="['test-result', testResults[config.id].success ? 'success' : 'error']">
            {{ testResults[config.id].message }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <ProviderForm 
        :config="editingConfig"
        @submit="handleSubmit"
        @cancel="closeForm"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAIConfigStore, type AIConfig } from '~/stores/aiConfig'

const config = useRuntimeConfig()
const apiBase = config.public.apiBase
const store = useAIConfigStore()

const showForm = ref(false)
const editingConfig = ref<AIConfig | null>(null)
const testResults = ref<Record<number, { success: boolean; message: string }>>({})

const configs = computed(() => store.configs)
const loading = computed(() => store.loading)
const error = computed(() => store.error)

onMounted(() => loadConfigs())

async function loadConfigs() {
  store.setLoading(true)
  store.setError(null)
  try {
    const response = await fetch(`${apiBase}/admin/ai-config`)
    if (!response.ok) throw new Error('Failed to load configurations')
    const data = await response.json()
    store.setConfigs(data)
  } catch (err) {
    store.setError((err as Error).message)
  } finally {
    store.setLoading(false)
  }
}

async function handleSubmit(formData: { providerName: string; modelName: string; apiKey: string; endpoint: string }) {
  try {
    const method = editingConfig.value ? 'PUT' : 'POST'
    const url = editingConfig.value 
      ? `${apiBase}/admin/ai-config/${editingConfig.value.id}`
      : `${apiBase}/admin/ai-config`

    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData),
    })

    if (!response.ok) throw new Error('Failed to save configuration')
    
    const savedConfig = await response.json()
    if (editingConfig.value) {
      store.updateConfig(savedConfig)
    } else {
      store.addConfig(savedConfig)
    }
    closeForm()
  } catch (err) {
    alert((err as Error).message)
  }
}

async function handleActivate(configId: number) {
  try {
    const response = await fetch(`${apiBase}/admin/ai-config/${configId}/activate`, {
      method: 'POST',
    })
    if (!response.ok) throw new Error('Failed to activate configuration')
    const activated = await response.json()
    store.updateConfig(activated)
  } catch (err) {
    alert((err as Error).message)
  }
}

async function handleTest(configId: number) {
  try {
    testResults.value[configId] = { success: false, message: 'Testing...' }
    const response = await fetch(`${apiBase}/admin/ai-config/${configId}/test`, {
      method: 'POST',
    })
    const result = await response.json()
    testResults.value[configId] = result
  } catch (err) {
    testResults.value[configId] = { success: false, message: (err as Error).message }
  }
}

async function handleDelete(configId: number) {
  if (!confirm('Are you sure you want to delete this configuration?')) return

  try {
    const response = await fetch(`${apiBase}/admin/ai-config/${configId}`, {
      method: 'DELETE',
    })
    if (!response.ok) throw new Error('Failed to delete configuration')
    store.removeConfig(configId)
  } catch (err) {
    alert((err as Error).message)
  }
}

function handleEdit(config: AIConfig) {
  editingConfig.value = config
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingConfig.value = null
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString()
}
</script>

<style scoped>
.ai-config-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.page-header h1 {
  margin: 0;
  font-size: 1.75rem;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.error { color: #dc2626; }

.config-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.config-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.25rem;
}

.config-card.active {
  border-color: #10b981;
  background: #ecfdf5;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.provider-info h3 {
  margin: 0;
  font-size: 1rem;
  text-transform: capitalize;
}

.model-name {
  font-size: 0.875rem;
  color: #6b7280;
}

.active-badge {
  background: #10b981;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
}

.config-details {
  margin-bottom: 1rem;
}

.detail-row {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
  margin-bottom: 0.25rem;
}

.detail-row .label {
  color: #6b7280;
}

.config-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

.btn-primary { background: #3b82f6; color: white; }
.btn-secondary { background: #f3f4f6; color: #374151; }
.btn-success { background: #10b981; color: white; }
.btn-danger { background: #ef4444; color: white; }

.test-result {
  margin-top: 0.75rem;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.test-result.success { background: #dcfce7; color: #166534; }
.test-result.error { background: #fef2f2; color: #dc2626; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-overlay > * {
  width: 100%;
  max-width: 400px;
}
</style>

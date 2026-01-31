<template>
  <div class="prompts-page">
    <div class="page-header">
      <h1>Prompt Templates</h1>
      <p class="subtitle">Manage AI prompt templates for each design phase</p>
    </div>

    <div class="actions-bar">
      <select v-model="selectedPhase" class="phase-filter">
        <option value="">All Phases</option>
        <option value="PRODUCT">Product</option>
        <option value="SCENARIO">Scenario</option>
        <option value="TRANSACTION_TYPE">Transaction Type</option>
        <option value="ACCOUNTING">Accounting</option>
      </select>

      <button @click="openCreateDialog" class="btn-create">
        <span>+</span> Create Prompt
      </button>

      <button @click="initializeDefaults" class="btn-init" :disabled="initializing">
        {{ initializing ? 'Initializing...' : 'Initialize Defaults' }}
      </button>
    </div>

    <div v-if="loading" class="loading">
      Loading prompts...
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <div v-else class="prompts-grid">
      <div
        v-for="prompt in filteredPrompts"
        :key="prompt.id"
        class="prompt-card"
        :class="{ active: prompt.isActive }"
      >
        <div class="card-header">
          <div class="card-title">
            <h3>{{ prompt.name }}</h3>
            <span class="version">v{{ prompt.version }}</span>
          </div>
          <span class="phase-badge" :class="prompt.designPhase.toLowerCase()">
            {{ formatPhase(prompt.designPhase) }}
          </span>
        </div>

        <div class="card-content">
          <pre class="prompt-preview">{{ truncateContent(prompt.content) }}</pre>
        </div>

        <div class="card-footer">
          <div class="status">
            <span v-if="prompt.isActive" class="active-badge">Active</span>
            <span v-else class="inactive-badge">Inactive</span>
          </div>

          <div class="card-actions">
            <button @click="viewHistory(prompt.name)" class="btn-action" title="Version History">
              📜
            </button>
            <button @click="editPrompt(prompt)" class="btn-action" title="Edit">
              ✏️
            </button>
            <button
              v-if="!prompt.isActive"
              @click="activatePrompt(prompt.id)"
              class="btn-action"
              title="Activate"
            >
              ✅
            </button>
            <button
              v-if="!prompt.isActive"
              @click="deletePrompt(prompt.id)"
              class="btn-action btn-danger"
              title="Delete"
            >
              🗑️
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredPrompts.length === 0 && !loading" class="empty-state">
      <p>No prompts found.</p>
      <p>Click "Initialize Defaults" to create default prompt templates.</p>
    </div>

    <!-- Editor Modal -->
    <div v-if="showEditor" class="modal-overlay" @click.self="closeEditor">
      <AdminPromptEditor
        :prompt="editingPrompt"
        @close="closeEditor"
        @save="savePrompt"
      />
    </div>

    <!-- History Modal -->
    <div v-if="showHistory" class="modal-overlay" @click.self="closeHistory">
      <div class="history-modal">
        <div class="modal-header">
          <h3>Version History: {{ historyName }}</h3>
          <button @click="closeHistory" class="close-btn">&times;</button>
        </div>

        <div class="history-list">
          <div
            v-for="version in historyVersions"
            :key="version.id"
            class="history-item"
            :class="{ active: version.isActive }"
          >
            <div class="version-info">
              <span class="version-number">Version {{ version.version }}</span>
              <span class="version-date">{{ formatDate(version.createdAt) }}</span>
              <span v-if="version.isActive" class="active-tag">Active</span>
            </div>
            <div class="version-actions">
              <button @click="rollbackToVersion(version.version)" class="btn-rollback">
                Rollback
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Prompt {
  id: number
  name: string
  designPhase: string
  content: string
  version: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

const config = useRuntimeConfig()
const apiBase = config.public.apiBase

const prompts = ref<Prompt[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const selectedPhase = ref('')
const initializing = ref(false)

const showEditor = ref(false)
const editingPrompt = ref<Prompt | null>(null)

const showHistory = ref(false)
const historyName = ref('')
const historyVersions = ref<Prompt[]>([])

const filteredPrompts = computed(() => {
  if (!selectedPhase.value) return prompts.value
  return prompts.value.filter(p => p.designPhase === selectedPhase.value)
})

onMounted(() => {
  loadPrompts()
})

async function loadPrompts() {
  loading.value = true
  error.value = null
  try {
    const response = await fetch(`${apiBase}/admin/prompts`)
    if (!response.ok) throw new Error('Failed to load prompts')
    prompts.value = await response.json()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function initializeDefaults() {
  initializing.value = true
  try {
    await fetch(`${apiBase}/admin/prompts/initialize`, { method: 'POST' })
    await loadPrompts()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    initializing.value = false
  }
}

function openCreateDialog() {
  editingPrompt.value = null
  showEditor.value = true
}

function editPrompt(prompt: Prompt) {
  editingPrompt.value = prompt
  showEditor.value = true
}

function closeEditor() {
  showEditor.value = false
  editingPrompt.value = null
}

async function savePrompt(data: { name: string; designPhase: string; content: string }) {
  try {
    const url = editingPrompt.value
      ? `${apiBase}/admin/prompts/${editingPrompt.value.id}`
      : `${apiBase}/admin/prompts`
    const method = editingPrompt.value ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })

    if (!response.ok) throw new Error('Failed to save prompt')

    await loadPrompts()
    closeEditor()
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function activatePrompt(id: number) {
  try {
    const response = await fetch(`${apiBase}/admin/prompts/${id}/activate`, {
      method: 'POST'
    })
    if (!response.ok) throw new Error('Failed to activate prompt')
    await loadPrompts()
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function deletePrompt(id: number) {
  if (!confirm('Are you sure you want to delete this prompt?')) return

  try {
    const response = await fetch(`${apiBase}/admin/prompts/${id}`, {
      method: 'DELETE'
    })
    if (!response.ok) throw new Error('Failed to delete prompt')
    await loadPrompts()
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function viewHistory(name: string) {
  historyName.value = name
  try {
    const response = await fetch(`${apiBase}/admin/prompts/history/${encodeURIComponent(name)}`)
    if (!response.ok) throw new Error('Failed to load history')
    historyVersions.value = await response.json()
    showHistory.value = true
  } catch (e) {
    error.value = (e as Error).message
  }
}

function closeHistory() {
  showHistory.value = false
  historyVersions.value = []
}

async function rollbackToVersion(version: number) {
  if (!confirm(`Rollback to version ${version}? This will create a new version with the old content.`)) return

  try {
    const response = await fetch(
      `${apiBase}/admin/prompts/rollback?name=${encodeURIComponent(historyName.value)}&version=${version}`,
      { method: 'POST' }
    )
    if (!response.ok) throw new Error('Failed to rollback')
    await loadPrompts()
    await viewHistory(historyName.value)
  } catch (e) {
    error.value = (e as Error).message
  }
}

function formatPhase(phase: string): string {
  return phase.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}

function truncateContent(content: string): string {
  const maxLength = 200
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.prompts-page {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 1.75rem;
}

.subtitle {
  color: #64748b;
  margin: 0;
}

.actions-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.phase-filter {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.95rem;
  min-width: 180px;
}

.btn-create,
.btn-init {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-create {
  background: #3b82f6;
  border: none;
  color: white;
}

.btn-create:hover {
  background: #2563eb;
}

.btn-init {
  background: white;
  border: 1px solid #d1d5db;
  color: #374151;
}

.btn-init:hover:not(:disabled) {
  background: #f3f4f6;
}

.btn-init:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading,
.error,
.empty-state {
  text-align: center;
  padding: 48px;
  color: #64748b;
}

.error {
  color: #dc2626;
}

.prompts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.prompt-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.prompt-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.prompt-card.active {
  border-color: #22c55e;
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title h3 {
  margin: 0;
  font-size: 1rem;
}

.version {
  font-size: 0.75rem;
  color: #94a3b8;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

.phase-badge {
  font-size: 0.75rem;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.phase-badge.product {
  background: #dbeafe;
  color: #1d4ed8;
}

.phase-badge.scenario {
  background: #dcfce7;
  color: #16a34a;
}

.phase-badge.transaction_type {
  background: #fef3c7;
  color: #d97706;
}

.phase-badge.accounting {
  background: #f3e8ff;
  color: #9333ea;
}

.card-content {
  padding: 16px;
}

.prompt-preview {
  margin: 0;
  font-size: 0.85rem;
  color: #475569;
  white-space: pre-wrap;
  word-break: break-word;
  background: #f8fafc;
  padding: 12px;
  border-radius: 4px;
  max-height: 120px;
  overflow: hidden;
}

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.active-badge,
.inactive-badge {
  font-size: 0.75rem;
  padding: 4px 8px;
  border-radius: 4px;
}

.active-badge {
  background: #dcfce7;
  color: #16a34a;
}

.inactive-badge {
  background: #f1f5f9;
  color: #64748b;
}

.card-actions {
  display: flex;
  gap: 4px;
}

.btn-action {
  background: none;
  border: none;
  padding: 6px;
  cursor: pointer;
  font-size: 1rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-action:hover {
  background: #f1f5f9;
}

.btn-danger:hover {
  background: #fee2e2;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.history-modal {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #64748b;
}

.history-list {
  padding: 16px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  margin-bottom: 8px;
}

.history-item.active {
  border-color: #22c55e;
  background: #f0fdf4;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-number {
  font-weight: 500;
}

.version-date {
  font-size: 0.85rem;
  color: #64748b;
}

.active-tag {
  font-size: 0.75rem;
  background: #dcfce7;
  color: #16a34a;
  padding: 2px 8px;
  border-radius: 4px;
}

.btn-rollback {
  padding: 6px 12px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-rollback:hover {
  background: #f3f4f6;
}
</style>

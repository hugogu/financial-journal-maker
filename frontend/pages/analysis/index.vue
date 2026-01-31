<template>
  <div class="analysis-page">
    <header class="page-header">
      <h1>Analysis Sessions</h1>
      <button @click="showCreateModal = true" class="btn btn-primary">
        + New Session
      </button>
    </header>

    <div class="filters">
      <select v-model="statusFilter" @change="loadSessions">
        <option value="">All Status</option>
        <option value="ACTIVE">Active</option>
        <option value="PAUSED">Paused</option>
        <option value="COMPLETED">Completed</option>
        <option value="ARCHIVED">Archived</option>
      </select>
    </div>

    <div v-if="loading" class="loading">Loading sessions...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="sessions.length === 0" class="empty">
      <p>No sessions found.</p>
      <button @click="showCreateModal = true" class="btn btn-primary">
        Create your first session
      </button>
    </div>
    <div v-else class="session-grid">
      <div 
        v-for="session in sessions" 
        :key="session.id"
        class="session-card"
      >
        <div class="card-header">
          <h3>{{ session.title }}</h3>
          <span :class="['status-badge', session.status.toLowerCase()]">
            {{ session.status }}
          </span>
        </div>
        <div class="card-meta">
          <span class="phase">Phase: {{ formatPhase(session.currentPhase) }}</span>
          <span class="date">{{ formatDate(session.createdAt) }}</span>
        </div>
        <div class="card-actions">
          <NuxtLink :to="`/analysis/${session.id}`" class="btn btn-secondary">
            {{ session.status === 'COMPLETED' ? 'View' : 'Open' }}
          </NuxtLink>
          <button 
            v-if="session.status === 'ACTIVE'" 
            @click="handlePause(session.id)"
            class="btn btn-outline"
          >
            Pause
          </button>
          <button 
            v-if="session.status === 'PAUSED'" 
            @click="handleResume(session.id)"
            class="btn btn-outline"
          >
            Resume
          </button>
          <button 
            v-if="session.status === 'ACTIVE' || session.status === 'PAUSED'" 
            @click="handleComplete(session.id)"
            class="btn btn-success-outline"
          >
            Complete
          </button>
        </div>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <h2>Create New Session</h2>
        <form @submit.prevent="handleCreate">
          <div class="form-group">
            <label for="title">Session Title</label>
            <input 
              id="title"
              v-model="newSessionTitle" 
              type="text" 
              placeholder="e.g., E-commerce Order Processing"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="button" @click="showCreateModal = false" class="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" class="btn btn-primary" :disabled="!newSessionTitle.trim()">
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSession } from '~/composables/useSession'

const router = useRouter()
const { store, fetchSessions, createSession, pauseSession, resumeSession, completeSession } = useSession()

const statusFilter = ref('')
const showCreateModal = ref(false)
const newSessionTitle = ref('')

const sessions = computed(() => store.sessions)
const loading = computed(() => store.loading)
const error = computed(() => store.error)

onMounted(() => {
  loadSessions()
})

async function loadSessions() {
  try {
    await fetchSessions(statusFilter.value || undefined)
  } catch (err) {
    console.error('Failed to load sessions:', err)
  }
}

async function handleCreate() {
  if (!newSessionTitle.value.trim()) return
  
  try {
    const session = await createSession(newSessionTitle.value.trim())
    showCreateModal.value = false
    newSessionTitle.value = ''
    router.push(`/analysis/${session.id}`)
  } catch (err) {
    console.error('Failed to create session:', err)
  }
}

async function handlePause(sessionId: number) {
  try {
    await pauseSession(sessionId)
    await loadSessions()
  } catch (err) {
    console.error('Failed to pause session:', err)
  }
}

async function handleResume(sessionId: number) {
  try {
    await resumeSession(sessionId)
    await loadSessions()
  } catch (err) {
    console.error('Failed to resume session:', err)
  }
}

async function handleComplete(sessionId: number) {
  if (confirm('Are you sure you want to complete this session? It will become read-only.')) {
    try {
      await completeSession(sessionId)
      await loadSessions()
    } catch (err) {
      console.error('Failed to complete session:', err)
    }
  }
}

function formatPhase(phase: string) {
  const phases: Record<string, string> = {
    'PRODUCT': 'Product',
    'SCENARIO': 'Scenario',
    'TRANSACTION_TYPE': 'Transaction Type',
    'ACCOUNTING': 'Accounting',
  }
  return phases[phase] || phase
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString()
}
</script>

<style scoped>
.analysis-page {
  max-width: 1200px;
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

.filters {
  margin-bottom: 1.5rem;
}

.filters select {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.error { color: #dc2626; }

.session-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

.session-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.25rem;
  transition: box-shadow 0.2s;
}

.session-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.625rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.active { background: #dcfce7; color: #166534; }
.status-badge.paused { background: #fef3c7; color: #92400e; }
.status-badge.completed { background: #dbeafe; color: #1e40af; }
.status-badge.archived { background: #f3f4f6; color: #4b5563; }

.card-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 1rem;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-primary { background: #3b82f6; color: white; }
.btn-primary:hover { background: #2563eb; }
.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }

.btn-secondary { background: #f3f4f6; color: #374151; }
.btn-secondary:hover { background: #e5e7eb; }

.btn-outline { 
  background: transparent; 
  color: #374151; 
  border: 1px solid #d1d5db; 
}
.btn-outline:hover { background: #f9fafb; }

.btn-success-outline { 
  background: transparent; 
  color: #059669; 
  border: 1px solid #10b981; 
}
.btn-success-outline:hover { background: #ecfdf5; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  width: 100%;
  max-width: 400px;
}

.modal h2 {
  margin: 0 0 1rem;
  font-size: 1.25rem;
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

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>

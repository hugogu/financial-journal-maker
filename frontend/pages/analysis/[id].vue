<template>
  <div class="session-page">
    <div v-if="loading" class="loading">Loading session...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="session" class="session-content">
      <header class="session-header">
        <div class="session-info">
          <h1>{{ session.title }}</h1>
          <div class="session-meta">
            <span :class="['status-badge', session.status.toLowerCase()]">
              {{ session.status }}
            </span>
            <span class="phase">Phase: {{ session.currentPhase }}</span>
          </div>
        </div>
        <div class="session-actions">
          <button 
            v-if="session.status === 'ACTIVE'" 
            @click="handlePause"
            class="btn btn-secondary"
          >
            Pause
          </button>
          <button 
            v-if="session.status === 'PAUSED'" 
            @click="handleResume"
            class="btn btn-primary"
          >
            Resume
          </button>
          <button 
            v-if="session.status === 'ACTIVE' || session.status === 'PAUSED'" 
            @click="handleComplete"
            class="btn btn-success"
          >
            Complete
          </button>
          <button 
            v-if="session.status === 'COMPLETED'" 
            @click="showExportDialog = true"
            class="btn btn-primary"
          >
            Export
          </button>
        </div>
      </header>

      <div class="session-main">
        <div class="chat-section">
          <ChatInterface 
            :session-id="sessionId" 
            :status="session.status"
          />
        </div>
        <div class="design-section">
          <DesignPanel :current-phase="session.currentPhase" />
        </div>
      </div>

      <ExportDialog 
        v-if="showExportDialog"
        :session-id="sessionId"
        @close="showExportDialog = false"
        @exported="handleExported"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useSession } from '~/composables/useSession'

const route = useRoute()
const { store, fetchSession, fetchMessages, fetchDecisions, pauseSession, resumeSession, completeSession } = useSession()

const sessionId = computed(() => Number(route.params.id))
const showExportDialog = ref(false)
const session = computed(() => store.currentSession)
const loading = computed(() => store.loading)
const error = computed(() => store.error)

onMounted(async () => {
  try {
    await fetchSession(sessionId.value)
    await fetchMessages(sessionId.value)
    await fetchDecisions(sessionId.value)
  } catch (err) {
    console.error('Failed to load session:', err)
  }
})

async function handlePause() {
  try {
    await pauseSession(sessionId.value)
  } catch (err) {
    console.error('Failed to pause session:', err)
  }
}

async function handleResume() {
  try {
    await resumeSession(sessionId.value)
  } catch (err) {
    console.error('Failed to resume session:', err)
  }
}

async function handleComplete() {
  if (confirm('Are you sure you want to complete this session? It will become read-only.')) {
    try {
      await completeSession(sessionId.value)
    } catch (err) {
      console.error('Failed to complete session:', err)
    }
  }
}

function handleExported() {
  showExportDialog.value = false
}
</script>

<style scoped>
.session-page {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.loading, .error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 1.125rem;
  color: #6b7280;
}

.error {
  color: #dc2626;
}

.session-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.session-info h1 {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
}

.session-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.active { background: #dcfce7; color: #166534; }
.status-badge.paused { background: #fef3c7; color: #92400e; }
.status-badge.completed { background: #dbeafe; color: #1e40af; }
.status-badge.archived { background: #f3f4f6; color: #4b5563; }

.phase {
  font-size: 0.875rem;
  color: #6b7280;
}

.session-actions {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary { background: #3b82f6; color: white; }
.btn-primary:hover { background: #2563eb; }

.btn-secondary { background: #f3f4f6; color: #374151; }
.btn-secondary:hover { background: #e5e7eb; }

.btn-success { background: #10b981; color: white; }
.btn-success:hover { background: #059669; }

.session-main {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 1rem;
  min-height: 0;
}

.chat-section {
  min-height: 0;
  overflow: hidden;
}

.design-section {
  overflow-y: auto;
}
</style>

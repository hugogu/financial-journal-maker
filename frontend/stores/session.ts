import { defineStore } from 'pinia'

export interface Session {
  id: number
  title: string
  status: 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ARCHIVED'
  currentPhase: 'PRODUCT' | 'SCENARIO' | 'TRANSACTION_TYPE' | 'ACCOUNTING'
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: number
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  metadata?: Record<string, unknown>
  createdAt: string
}

export interface Decision {
  id: number
  decisionType: 'PRODUCT' | 'SCENARIO' | 'TRANSACTION_TYPE' | 'ACCOUNTING'
  entityType?: string
  content: Record<string, unknown>
  isConfirmed: boolean
  linkedEntityId?: number
  createdAt: string
}

interface SessionState {
  sessions: Session[]
  currentSession: Session | null
  messages: Message[]
  decisions: Decision[]
  loading: boolean
  error: string | null
}

export const useSessionStore = defineStore('session', {
  state: (): SessionState => ({
    sessions: [],
    currentSession: null,
    messages: [],
    decisions: [],
    loading: false,
    error: null,
  }),

  getters: {
    activeSessions: (state) => state.sessions.filter(s => s.status === 'ACTIVE'),
    pausedSessions: (state) => state.sessions.filter(s => s.status === 'PAUSED'),
    completedSessions: (state) => state.sessions.filter(s => s.status === 'COMPLETED'),
    confirmedDecisions: (state) => state.decisions.filter(d => d.isConfirmed),
  },

  actions: {
    setLoading(loading: boolean) {
      this.loading = loading
    },

    setError(error: string | null) {
      this.error = error
    },

    setSessions(sessions: Session[]) {
      this.sessions = sessions
    },

    setCurrentSession(session: Session | null) {
      this.currentSession = session
    },

    setMessages(messages: Message[]) {
      this.messages = messages
    },

    addMessage(message: Message) {
      this.messages.push(message)
    },

    setDecisions(decisions: Decision[]) {
      this.decisions = decisions
    },

    addDecision(decision: Decision) {
      this.decisions.push(decision)
    },

    updateDecision(decision: Decision) {
      const index = this.decisions.findIndex(d => d.id === decision.id)
      if (index !== -1) {
        this.decisions[index] = decision
      }
    },

    clearCurrentSession() {
      this.currentSession = null
      this.messages = []
      this.decisions = []
    },
  },
})

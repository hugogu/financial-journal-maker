import { useSessionStore, type Session, type Message, type Decision } from '~/stores/session'

export function useSession() {
  const config = useRuntimeConfig()
  const store = useSessionStore()
  const apiBase = config.public.apiBase

  async function fetchSessions(status?: string) {
    store.setLoading(true)
    store.setError(null)
    try {
      const url = status 
        ? `${apiBase}/sessions?status=${status}`
        : `${apiBase}/sessions`
      const response = await fetch(url)
      if (!response.ok) throw new Error('Failed to fetch sessions')
      const data = await response.json()
      store.setSessions(data.content || [])
      return data
    } catch (error) {
      store.setError((error as Error).message)
      throw error
    } finally {
      store.setLoading(false)
    }
  }

  async function fetchSession(sessionId: number) {
    store.setLoading(true)
    store.setError(null)
    try {
      const response = await fetch(`${apiBase}/sessions/${sessionId}`)
      if (!response.ok) throw new Error('Failed to fetch session')
      const data = await response.json()
      store.setCurrentSession(data)
      return data
    } catch (error) {
      store.setError((error as Error).message)
      throw error
    } finally {
      store.setLoading(false)
    }
  }

  async function createSession(title: string) {
    store.setLoading(true)
    store.setError(null)
    try {
      const response = await fetch(`${apiBase}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title }),
      })
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Failed to create session')
      }
      const data = await response.json()
      store.setCurrentSession(data)
      return data
    } catch (error) {
      store.setError((error as Error).message)
      throw error
    } finally {
      store.setLoading(false)
    }
  }

  async function pauseSession(sessionId: number) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/pause`, {
      method: 'POST',
    })
    if (!response.ok) throw new Error('Failed to pause session')
    const data = await response.json()
    store.setCurrentSession(data)
    return data
  }

  async function resumeSession(sessionId: number) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/resume`, {
      method: 'POST',
    })
    if (!response.ok) throw new Error('Failed to resume session')
    const data = await response.json()
    store.setCurrentSession(data)
    return data
  }

  async function completeSession(sessionId: number) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/complete`, {
      method: 'POST',
    })
    if (!response.ok) throw new Error('Failed to complete session')
    const data = await response.json()
    store.setCurrentSession(data)
    return data
  }

  async function fetchMessages(sessionId: number) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/messages`)
    if (!response.ok) throw new Error('Failed to fetch messages')
    const data = await response.json()
    store.setMessages(data)
    return data
  }

  async function sendMessage(sessionId: number, content: string) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/messages`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content }),
    })
    if (!response.ok) throw new Error('Failed to send message')
    const data = await response.json()
    store.addMessage(data)
    return data
  }

  async function fetchDecisions(sessionId: number, confirmed?: boolean) {
    let url = `${apiBase}/sessions/${sessionId}/decisions`
    if (confirmed !== undefined) {
      url += `?confirmed=${confirmed}`
    }
    const response = await fetch(url)
    if (!response.ok) throw new Error('Failed to fetch decisions')
    const data = await response.json()
    store.setDecisions(data)
    return data
  }

  async function confirmDecision(sessionId: number, decision: Partial<Decision>) {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/decisions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(decision),
    })
    if (!response.ok) throw new Error('Failed to confirm decision')
    const data = await response.json()
    store.addDecision(data)
    return data
  }

  return {
    store,
    fetchSessions,
    fetchSession,
    createSession,
    pauseSession,
    resumeSession,
    completeSession,
    fetchMessages,
    sendMessage,
    fetchDecisions,
    confirmDecision,
  }
}

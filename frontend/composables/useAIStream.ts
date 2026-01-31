import { ref } from 'vue'
import { useSessionStore } from '~/stores/session'

export function useAIStream() {
  const config = useRuntimeConfig()
  const store = useSessionStore()
  const apiBase = config.public.apiBase

  const isStreaming = ref(false)
  const streamError = ref<string | null>(null)
  const currentResponse = ref('')

  async function streamMessage(sessionId: number, content: string) {
    isStreaming.value = true
    streamError.value = null
    currentResponse.value = ''

    store.addMessage({
      id: Date.now(),
      role: 'USER',
      content,
      createdAt: new Date().toISOString(),
    })

    try {
      const response = await fetch(`${apiBase}/sessions/${sessionId}/messages/stream`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ content }),
      })

      if (!response.ok) {
        throw new Error('Failed to start stream')
      }

      const reader = response.body?.getReader()
      if (!reader) {
        throw new Error('No response body')
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        
        const lines = buffer.split('\n\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const dataLines = line
            .split('\n')
            .map((entry) => entry.trim())
            .filter((entry) => entry.startsWith('data:'))

          if (!dataLines.length) continue

          const data = dataLines
            .map((entry) => (entry.startsWith('data: ') ? entry.slice(6) : entry.slice(5)))
            .join('\n')
            .replace(/\n/g, '\n')

          console.log('SSE chunk received:', data)
          currentResponse.value += data
          console.log('Current response length:', currentResponse.value.length)
        }
      }

      store.addMessage({
        id: Date.now() + 1,
        role: 'ASSISTANT',
        content: currentResponse.value,
        createdAt: new Date().toISOString(),
      })

      return currentResponse.value
    } catch (error) {
      streamError.value = (error as Error).message
      throw error
    } finally {
      isStreaming.value = false
    }
  }

  function cancelStream() {
    isStreaming.value = false
  }

  return {
    isStreaming,
    streamError,
    currentResponse,
    streamMessage,
    cancelStream,
  }
}

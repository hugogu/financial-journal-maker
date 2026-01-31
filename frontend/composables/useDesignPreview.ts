import { ref, onMounted, onUnmounted } from 'vue'

export function useDesignPreview(sessionId: number) {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const preview = ref<any>(null)
  const isConnected = ref(false)
  
  let eventSource: EventSource | null = null

  const connect = () => {
    if (!sessionId) return
    
    loading.value = true
    error.value = null
    
    // First get initial state
    fetch(`/api/v1/sessions/${sessionId}/preview`)
      .then(res => res.json())
      .then(data => {
        preview.value = data
        loading.value = false
      })
      .catch(err => {
        error.value = err.message
        loading.value = false
      })
    
    // Then connect to SSE stream
    eventSource = new EventSource(`/api/v1/sessions/${sessionId}/preview/stream`)
    
    eventSource.onopen = () => {
      isConnected.value = true
    }
    
    eventSource.onmessage = (event) => {
      try {
        const lines = event.data.split('\n').filter((line: string) => line.startsWith('data:'))
        if (lines.length > 0) {
          const data = lines[0].slice(5).trim()
          preview.value = JSON.parse(data)
        }
      } catch (err) {
        console.error('Failed to parse preview update:', err)
      }
    }
    
    eventSource.onerror = () => {
      error.value = 'Connection lost'
      isConnected.value = false
    }
  }

  const disconnect = () => {
    eventSource?.close()
    eventSource = null
    isConnected.value = false
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    loading,
    error,
    preview,
    isConnected,
    connect,
    disconnect
  }
}

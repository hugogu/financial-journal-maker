<template>
  <div class="chat-interface">
    <div class="messages-container" ref="messagesContainer">
      <div v-if="messages.length === 0 && !isStreaming" class="empty-state">
        <div class="welcome-message">
          <h3>👋 Welcome to AI Analysis Session</h3>
          <p>Start by describing your business scenario. For example:</p>
          <ul>
            <li>"I need to track e-commerce order processing with inventory management"</li>
            <li>"Help me design accounts for a subscription-based SaaS business"</li>
            <li>"I'm building a marketplace with buyer and seller transactions"</li>
          </ul>
          <p class="hint">The AI will guide you through Product → Scenario → Transaction Type → Accounting design phases.</p>
        </div>
      </div>
      <SessionMessageBubble
        v-for="message in messages"
        :key="message.id"
        :message="message"
      />
      <div v-if="isStreaming" class="streaming-indicator">
        <SessionMessageBubble
          :message="{ id: 0, role: 'ASSISTANT', content: currentResponse || 'Thinking...', createdAt: '' }"
        />
      </div>
    </div>
    
    <div class="input-container">
      <textarea
        v-model="inputMessage"
        @keydown.enter.exact.prevent="handleSend"
        :disabled="isStreaming || !isActive"
        placeholder="Describe your business scenario..."
        rows="3"
      />
      <button 
        @click="handleSend" 
        :disabled="!inputMessage.trim() || isStreaming || !isActive"
        class="send-button"
      >
        {{ isStreaming ? 'Sending...' : 'Send' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useSessionStore } from '~/stores/session'
import { useAIStream } from '~/composables/useAIStream'

const props = defineProps<{
  sessionId: number
  status: string
}>()

const store = useSessionStore()
const { isStreaming, currentResponse, streamMessage } = useAIStream()

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

const messages = computed(() => store.messages)
const isActive = computed(() => props.status === 'ACTIVE')

async function handleSend() {
  if (!inputMessage.value.trim() || isStreaming.value || !isActive.value) return
  
  const content = inputMessage.value
  inputMessage.value = ''
  
  try {
    await streamMessage(props.sessionId, content)
  } catch (error) {
    console.error('Failed to send message:', error)
  }
}

watch(messages, async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}, { deep: true })

watch(currentResponse, async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
})
</script>

<style scoped>
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 400px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 2rem;
}

.welcome-message {
  max-width: 600px;
  text-align: left;
}

.welcome-message h3 {
  margin: 0 0 1rem;
  font-size: 1.5rem;
  color: #111827;
}

.welcome-message p {
  margin: 0.75rem 0;
  color: #4b5563;
  line-height: 1.6;
}

.welcome-message ul {
  margin: 1rem 0;
  padding-left: 1.5rem;
  color: #6b7280;
}

.welcome-message li {
  margin: 0.5rem 0;
  font-style: italic;
}

.welcome-message .hint {
  margin-top: 1.5rem;
  padding: 1rem;
  background: #eff6ff;
  border-left: 3px solid #3b82f6;
  border-radius: 4px;
  font-size: 0.875rem;
  color: #1e40af;
}

.streaming-indicator {
  opacity: 0.8;
}

.input-container {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
  background: white;
}

.input-container textarea {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  resize: none;
  font-size: 0.875rem;
}

.input-container textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.input-container textarea:disabled {
  background: #f3f4f6;
}

.send-button {
  padding: 0.75rem 1.5rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.send-button:hover:not(:disabled) {
  background: #2563eb;
}

.send-button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
</style>

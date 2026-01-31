<template>
  <div :class="['message-bubble', message.role.toLowerCase()]">
    <div class="role-label">{{ roleLabel }}</div>
    <div class="content" v-html="formattedContent"></div>
    <div v-if="message.createdAt" class="timestamp">
      {{ formatTime(message.createdAt) }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Message {
  id: number
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  createdAt: string
}

const props = defineProps<{
  message: Message
}>()

const roleLabel = computed(() => {
  switch (props.message.role) {
    case 'USER': return 'You'
    case 'ASSISTANT': return 'AI Assistant'
    case 'SYSTEM': return 'System'
    default: return props.message.role
  }
})

const formattedContent = computed(() => {
  let content = props.message.content
  content = content.replace(/</g, '&lt;').replace(/>/g, '&gt;')
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  content = content.replace(/\n/g, '<br>')
  return content
})

function formatTime(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.message-bubble {
  max-width: 80%;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  line-height: 1.5;
}

.message-bubble.user {
  align-self: flex-end;
  background: #3b82f6;
  color: white;
}

.message-bubble.assistant {
  align-self: flex-start;
  background: white;
  border: 1px solid #e5e7eb;
}

.message-bubble.system {
  align-self: center;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  max-width: 90%;
}

.role-label {
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  opacity: 0.8;
}

.content {
  word-wrap: break-word;
}

.timestamp {
  font-size: 0.625rem;
  margin-top: 0.5rem;
  opacity: 0.6;
  text-align: right;
}
</style>

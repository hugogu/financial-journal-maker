<template>
  <div class="source-session-link">
    <NuxtLink 
      v-if="sessionId" 
      :to="`/analysis/${sessionId}`"
      class="session-link"
    >
      <i class="pi pi-history" />
      <span>View Source Session</span>
      <Tag v-if="isReadOnly" value="Read Only" severity="warning" />
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  sessionId?: number;
  isReadOnly?: boolean;
}>();
</script>

<style scoped>
.source-session-link {
  display: inline-block;
}

.session-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f3f4f6;
  border-radius: 6px;
  color: #374151;
  text-decoration: none;
  font-size: 13px;
  transition: background 0.2s;
}

.session-link:hover {
  background: #e5e7eb;
}
</style>

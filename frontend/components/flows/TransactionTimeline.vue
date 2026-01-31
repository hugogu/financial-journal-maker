<template>
  <div class="transaction-timeline">
    <div v-if="events.length === 0" class="empty-state">
      <i class="pi pi-calendar" />
      <span>No timeline events available</span>
    </div>
    
    <div v-else class="timeline">
      <div
        v-for="(event, index) in events"
        :key="index"
        class="timeline-item"
        :class="{ settlement: event.isSettlement }"
        @click="selectEvent(event)"
      >
        <div class="timeline-marker">
          <div class="marker-dot" :class="event.timing" />
          <div v-if="index < events.length - 1" class="marker-line" />
        </div>
        
        <div class="timeline-content">
          <div class="event-timing">
            <Tag :value="event.timing" severity="primary" class="timing-tag" />
            <i v-if="event.isSettlement" class="pi pi-check-circle settlement-icon" />
          </div>
          <div class="event-description">{{ event.description }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface TimelineEvent {
  timing: string;
  description: string;
  relatedEntryIds: string[];
  isSettlement?: boolean;
}

const props = defineProps<{
  events: TimelineEvent[];
}>();

const emit = defineEmits<{
  select: [event: TimelineEvent];
}>();

const selectEvent = (event: TimelineEvent) => {
  emit('select', event);
};
</script>

<style scoped>
.transaction-timeline {
  padding: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #6b7280;
  gap: 12px;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  cursor: pointer;
  transition: background 0.2s;
}

.timeline-item:hover {
  background: #f9fafb;
}

.timeline-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24px;
  flex-shrink: 0;
}

.marker-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #3b82f6;
  border: 3px solid #dbeafe;
}

.marker-dot.T\+0 {
  background: #10b981;
  border-color: #d1fae5;
}

.marker-line {
  flex: 1;
  width: 2px;
  background: #e5e7eb;
  margin: 4px 0;
}

.timeline-content {
  flex: 1;
  padding-bottom: 16px;
}

.event-timing {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.timing-tag {
  font-size: 11px;
}

.settlement-icon {
  color: #10b981;
  font-size: 14px;
}

.event-description {
  color: #374151;
  font-size: 13px;
}

.timeline-item.settlement .event-description {
  font-weight: 500;
}
</style>

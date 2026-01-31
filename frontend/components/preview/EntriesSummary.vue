<template>
  <div class="entries-summary">
    <h4>Journal Entries ({{ totalEntries }})</h4>
    <div v-if="entryGroups.length === 0" class="empty-state">
      No entries defined yet
    </div>
    <div v-else class="entry-groups">
      <div
        v-for="group in entryGroups"
        :key="group.triggerEvent"
        class="entry-group"
      >
        <div class="group-header">
          <span class="trigger-event">{{ group.triggerEvent }}</span>
          <Tag :value="group.entries.length + ' entries'" severity="secondary" />
        </div>
        <div class="entries-list">
          <div
            v-for="entry in group.entries"
            :key="entry.accountCode + entry.operation"
            class="entry-item"
            :class="{ confirmed: entry.confirmed }"
          >
            <span class="operation" :class="entry.operation.toLowerCase()">
              {{ entry.operation }}
            </span>
            <span class="account">{{ entry.accountCode }}</span>
            <span class="amount">{{ entry.amountExpression }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Entry {
  operation: string;
  accountCode: string;
  accountName: string;
  amountExpression: string;
  confirmed: boolean;
}

interface EntryGroup {
  triggerEvent: string;
  entries: Entry[];
}

const props = defineProps<{
  entryGroups: EntryGroup[];
}>();

const totalEntries = computed(() => 
  props.entryGroups.reduce((sum, g) => sum + g.entries.length, 0)
);
</script>

<style scoped>
.entries-summary {
  padding: 12px;
}

.entries-summary h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #374151;
}

.empty-state {
  color: #9ca3af;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.entry-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.entry-group {
  background: #f9fafb;
  border-radius: 6px;
  padding: 12px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.trigger-event {
  font-weight: 600;
  font-size: 13px;
  color: #111827;
}

.entries-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.entry-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: #fff;
  border-radius: 4px;
  font-size: 12px;
}

.operation {
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.operation.dr {
  background: #fef2f2;
  color: #dc2626;
}

.operation.cr {
  background: #f0fdf4;
  color: #16a34a;
}

.account {
  color: #374151;
  font-weight: 500;
}

.amount {
  color: #6b7280;
  font-family: monospace;
  margin-left: auto;
}
</style>

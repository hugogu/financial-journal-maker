<template>
  <div class="design-preview-panel" :style="{ width: panelWidth + 'px' }">
    <div class="panel-header">
      <h3>Design Preview</h3>
      <div class="header-actions">
        <Button
          :icon="isCollapsed ? 'pi pi-chevron-left' : 'pi pi-chevron-right'"
          text
          size="small"
          @click="toggleCollapse"
        />
      </div>
    </div>

    <div v-if="!isCollapsed" class="panel-content">
      <div v-if="loading" class="loading-state">
        <i class="pi pi-spin pi-spinner" />
        <span>Loading preview...</span>
      </div>

      <div v-else-if="error" class="error-state">
        <i class="pi pi-exclamation-circle" />
        <span>{{ error }}</span>
      </div>

      <div v-else-if="preview" class="preview-content">
        <div class="phase-badge">
          <Tag :value="preview.currentPhase" severity="primary" />
          <span v-if="preview.transactionTypeName" class="transaction-name">
            {{ preview.transactionTypeName }}
          </span>
        </div>

        <div v-if="!preview.isValid" class="validation-banner">
          <i class="pi pi-exclamation-triangle" />
          <ul>
            <li v-for="msg in preview.validationMessages" :key="msg">{{ msg }}</li>
          </ul>
        </div>

        <Accordion :multiple="true" :active-index="[0, 1]">
          <AccordionTab header="Accounts">
            <AccountsList :accounts="preview.accounts || []" />
          </AccordionTab>
          
          <AccordionTab header="Journal Entries">
            <EntriesSummary :entry-groups="preview.entryGroups || []" />
          </AccordionTab>
        </Accordion>

        <div class="preview-stats">
          <div class="stat">
            <span class="stat-label">Confirmed</span>
            <span class="stat-value">{{ preview.confirmedAccountCount }} accounts</span>
          </div>
          <div class="stat">
            <span class="stat-label">Entries</span>
            <span class="stat-value">{{ preview.confirmedEntryCount }} entries</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Resize handle -->
    <div
      v-if="!isCollapsed"
      class="resize-handle"
      @mousedown="startResize"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

interface Preview {
  sessionId: number;
  currentPhase: string;
  transactionTypeName?: string;
  accounts: any[];
  entryGroups: any[];
  confirmedAccountCount: number;
  confirmedEntryCount: number;
  isValid: boolean;
  validationMessages: string[];
}

const props = defineProps<{
  sessionId: number;
  preview: Preview | null;
  loading: boolean;
  error: string | null;
}>();

const isCollapsed = ref(false);
const panelWidth = ref(320);
const isResizing = ref(false);

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

const startResize = (e: MouseEvent) => {
  isResizing.value = true;
  const startX = e.clientX;
  const startWidth = panelWidth.value;

  const handleMouseMove = (e: MouseEvent) => {
    if (!isResizing.value) return;
    const diff = e.clientX - startX;
    panelWidth.value = Math.max(240, Math.min(480, startWidth - diff));
  };

  const handleMouseUp = () => {
    isResizing.value = false;
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  };

  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
};

onMounted(() => {
  const saved = localStorage.getItem('designPreviewWidth');
  if (saved) panelWidth.value = parseInt(saved);
});

onUnmounted(() => {
  localStorage.setItem('designPreviewWidth', panelWidth.value.toString());
});
</script>

<style scoped>
.design-preview-panel {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  height: 100%;
  position: relative;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.panel-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 12px;
  color: #6b7280;
}

.error-state {
  color: #dc2626;
}

.phase-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.transaction-name {
  font-weight: 500;
  color: #111827;
  font-size: 14px;
}

.validation-banner {
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
  color: #92400e;
  font-size: 13px;
}

.validation-banner ul {
  margin: 0;
  padding-left: 16px;
}

.preview-stats {
  display: flex;
  gap: 16px;
  padding: 12px;
  margin-top: 16px;
  background: #f9fafb;
  border-radius: 6px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 11px;
  color: #6b7280;
  text-transform: uppercase;
}

.stat-value {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.resize-handle {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  background: transparent;
}

.resize-handle:hover {
  background: #3b82f6;
}
</style>

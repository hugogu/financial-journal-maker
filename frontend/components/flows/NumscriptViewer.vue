<template>
  <div class="numscript-viewer">
    <div v-if="error" class="error-banner">
      <i class="pi pi-exclamation-triangle" />
      <span>{{ error }}</span>
    </div>

    <div class="viewer-header">
      <div class="header-info">
        <Tag :value="source" severity="info" class="source-tag" />
        <span v-if="lineCount" class="line-count">{{ lineCount }} lines</span>
      </div>
      <Button
        :label="copied ? 'Copied!' : 'Copy'"
        :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
        size="small"
        @click="copyToClipboard"
      />
    </div>

    <pre class="numscript-code"><code ref="codeBlock" class="language-numscript">{{ code }}</code></pre>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';

interface Props {
  code: string;
  source?: string;
  lineCount?: number;
  error?: string | null;
}

const props = defineProps<Props>();

const codeBlock = ref<HTMLElement | null>(null);
const copied = ref(false);

const copyToClipboard = async () => {
  if (!props.code) return;
  
  try {
    await navigator.clipboard.writeText(props.code);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (err) {
    console.error('Failed to copy:', err);
  }
};

// Apply Prism highlighting when code changes
const highlight = () => {
  if (codeBlock.value && (window as any).Prism) {
    (window as any).Prism.highlightElement(codeBlock.value);
  }
};

onMounted(() => {
  highlight();
});

watch(() => props.code, () => {
  setTimeout(highlight, 0);
});
</script>

<style scoped>
.numscript-viewer {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #1e1e1e;
}

.error-banner {
  background: #fef3c7;
  color: #92400e;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #2d2d2d;
  border-bottom: 1px solid #3d3d3d;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.source-tag {
  font-size: 12px;
}

.line-count {
  color: #9ca3af;
  font-size: 12px;
}

.numscript-code {
  margin: 0;
  padding: 16px;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  background: #1e1e1e;
  max-height: 600px;
  overflow-y: auto;
}

/* Custom scrollbar for dark theme */
.numscript-code::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.numscript-code::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.numscript-code::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 4px;
}

.numscript-code::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}
</style>

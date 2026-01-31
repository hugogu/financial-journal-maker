<template>
  <div class="flow-detail">
    <div v-if="loading">Loading...</div>
    <div v-else-if="flow">
      <div class="detail-header">
        <div class="header-title">
          <h1>{{ flow.transactionTypeName }}</h1>
          <p>{{ flow.description }}</p>
        </div>
        <SourceSessionLink 
          :session-id="flow.sourceSessionId" 
          :is-read-only="true" 
        />
      </div>
      
      <ClientOnly>
        <TabView>
          <TabPanel header="Accounts">
            <AccountsTable :accounts="flow.accounts || []" :loading="false" />
          </TabPanel>
          
          <TabPanel header="Journal Entries">
            <JournalEntriesTable :entries="flow.journalEntries || []" :loading="false" />
          </TabPanel>
          
          <TabPanel header="Numscript">
            <NumscriptViewer
              :code="numscript.code || ''"
              :source="numscript.source"
              :line-count="numscript.lineCount"
              :error="numscript.error"
            />
          </TabPanel>

          <TabPanel header="Flow Diagram">
            <FlowDiagram
              :nodes="diagram.nodes || []"
              :edges="diagram.edges || []"
              :loading="diagramLoading"
            />
          </TabPanel>
        </TabView>
      </ClientOnly>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useTransactionFlows } from '~/composables/useTransactionFlows';
import AccountsTable from '~/components/flows/AccountsTable.vue';
import JournalEntriesTable from '~/components/flows/JournalEntriesTable.vue';
import NumscriptViewer from '~/components/flows/NumscriptViewer.vue';
import FlowDiagram from '~/components/flows/FlowDiagram.vue';
import SourceSessionLink from '~/components/flows/SourceSessionLink.vue';

const route = useRoute();
const { getTransactionFlow, getNumscript, getFlowDiagram, loading } = useTransactionFlows();

const flow = ref<any>(null);
const numscript = ref<any>({});
const diagram = ref<any>({});
const diagramLoading = ref(false);

onMounted(async () => {
  const code = route.params.code as string;
  try {
    flow.value = await getTransactionFlow(code);
    numscript.value = await getNumscript(code);
    
    diagramLoading.value = true;
    diagram.value = await getFlowDiagram(code);
    diagramLoading.value = false;
  } catch (error) {
    console.error('Failed to load flow:', error);
    diagramLoading.value = false;
  }
});
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-title h1 {
  margin: 0 0 8px 0;
}

.header-title p {
  margin: 0;
  color: #6b7280;
}
</style>

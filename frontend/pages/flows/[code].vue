<template>
  <div class="flow-detail">
    <div v-if="loading">Loading...</div>
    <div v-else-if="flow">
      <h1>{{ flow.transactionTypeName }}</h1>
      <p>{{ flow.description }}</p>
      
      <TabView>
        <TabPanel header="Accounts">
          <AccountsTable :accounts="flow.accounts || []" :loading="false" />
        </TabPanel>
        
        <TabPanel header="Journal Entries">
          <JournalEntriesTable :entries="flow.journalEntries || []" :loading="false" />
        </TabPanel>
      </TabView>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useTransactionFlows } from '~/composables/useTransactionFlows';
import AccountsTable from '~/components/flows/AccountsTable.vue';
import JournalEntriesTable from '~/components/flows/JournalEntriesTable.vue';

const route = useRoute();
const { getTransactionFlow, loading } = useTransactionFlows();

const flow = ref<any>(null);

onMounted(async () => {
  const code = route.params.code as string;
  try {
    flow.value = await getTransactionFlow(code);
  } catch (error) {
    console.error('Failed to load flow:', error);
  }
});
</script>

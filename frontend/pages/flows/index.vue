<template>
  <div class="flows-browser">
    <h1>Transaction Flow Browser</h1>
    
    <div class="search-bar mb-4">
      <ClientOnly>
        <InputText v-model="searchQuery" placeholder="Search flows..." @input="handleSearch" />
      </ClientOnly>
    </div>

    <ClientOnly>
      <TransactionFlowList 
        :flows="flows" 
        :loading="loading"
        @view="viewFlow"
      />
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useTransactionFlows } from '~/composables/useTransactionFlows';
import TransactionFlowList from '~/components/flows/TransactionFlowList.vue';

const router = useRouter();
const { listTransactionFlows, loading } = useTransactionFlows();

const searchQuery = ref('');
const flows = ref<any[]>([]);

const loadFlows = async () => {
  try {
    const result = await listTransactionFlows(undefined, undefined, searchQuery.value);
    flows.value = result.content || [];
  } catch (error) {
    console.error('Failed to load flows:', error);
  }
};

const handleSearch = () => {
  loadFlows();
};

const viewFlow = (code: string) => {
  router.push(`/flows/${code}`);
};

onMounted(() => {
  loadFlows();
});
</script>

<template>
  <div class="transaction-flow-list">
    <DataTable :value="flows" :loading="loading" paginator :rows="20">
      <Column field="transactionTypeCode" header="Code" />
      <Column field="transactionTypeName" header="Name" />
      <Column field="productCode" header="Product" />
      <Column field="scenarioCode" header="Scenario" />
      <Column field="accountCount" header="Accounts" />
      <Column field="entryCount" header="Entries" />
      <Column>
        <template #body="slotProps">
          <Button label="View" @click="$emit('view', slotProps.data.transactionTypeCode)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  flows: any[];
  loading: boolean;
}>();

defineEmits<{
  view: [code: string];
}>();
</script>

<template>
  <div class="journal-entries-table">
    <DataTable :value="entries" :loading="loading" groupRowsBy="triggerEvent">
      <Column field="triggerEvent" header="Trigger Event" />
      <Column field="operation" header="DR/CR">
        <template #body="slotProps">
          <Tag :severity="slotProps.data.operation === 'DR' ? 'danger' : 'success'">
            {{ slotProps.data.operation }}
          </Tag>
        </template>
      </Column>
      <Column field="accountCode" header="Account Code" />
      <Column field="accountName" header="Account Name" />
      <Column field="amountExpression" header="Amount" />
      <Column field="condition" header="Condition" />
    </DataTable>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  entries: any[];
  loading: boolean;
}>();
</script>

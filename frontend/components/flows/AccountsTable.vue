<template>
  <div class="accounts-table">
    <DataTable :value="accounts" :loading="loading">
      <Column field="accountCode" header="Account Code" />
      <Column field="accountName" header="Account Name" />
      <Column field="accountType" header="Type">
        <template #body="slotProps">
          <Tag :severity="getTypeSeverity(slotProps.data.accountType)">
            {{ slotProps.data.accountType }}
          </Tag>
        </template>
      </Column>
      <Column field="accountState" header="State" />
      <Column field="linkedToCoA" header="Linked to COA">
        <template #body="slotProps">
          <i :class="slotProps.data.linkedToCoA ? 'pi pi-check' : 'pi pi-times'" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  accounts: any[];
  loading: boolean;
}>();

const getTypeSeverity = (type: string) => {
  const map: Record<string, string> = {
    CUSTOMER: 'info',
    BANK: 'secondary',
    CHANNEL: 'primary',
    REVENUE: 'success',
    COST: 'danger',
  };
  return map[type] || 'secondary';
};
</script>

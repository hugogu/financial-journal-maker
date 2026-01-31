<template>
  <div class="accounts-list">
    <h4>Accounts ({{ accounts.length }})</h4>
    <div v-if="accounts.length === 0" class="empty-state">
      No accounts defined yet
    </div>
    <div v-else class="account-items">
      <div
        v-for="account in accounts"
        :key="account.accountCode"
        class="account-item"
        :class="{ confirmed: account.confirmed, tentative: !account.confirmed }"
      >
        <div class="account-info">
          <span class="account-code">{{ account.accountCode }}</span>
          <span class="account-name">{{ account.accountName }}</span>
        </div>
        <div class="account-badges">
          <Tag :value="account.accountType" severity="info" class="type-tag" />
          <i v-if="!account.linkedToCoA" class="pi pi-link-slash warning-icon" title="Not linked to COA" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Account {
  accountCode: string;
  accountName: string;
  accountType: string;
  confirmed: boolean;
  linkedToCoA: boolean;
}

defineProps<{
  accounts: Account[];
}>();
</script>

<style scoped>
.accounts-list {
  padding: 12px;
}

.accounts-list h4 {
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

.account-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 3px solid #d1d5db;
}

.account-item.confirmed {
  border-left-color: #10b981;
}

.account-item.tentative {
  border-left-color: #f59e0b;
}

.account-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.account-code {
  font-weight: 600;
  font-size: 13px;
  color: #111827;
}

.account-name {
  font-size: 12px;
  color: #6b7280;
}

.account-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-tag {
  font-size: 11px;
}

.warning-icon {
  color: #f59e0b;
  font-size: 12px;
}
</style>

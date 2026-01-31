<template>
  <div class="product-scenario-tree">
    <div
      v-for="product in products"
      :key="product.productCode"
      class="product-item"
    >
      <div
        class="product-header"
        :class="{ expanded: expandedProducts.has(product.productCode) }"
        @click="toggleProduct(product.productCode)"
      >
        <i
          class="pi pi-chevron-right toggle-icon"
          :class="{ 'pi-chevron-down': expandedProducts.has(product.productCode) }"
        />
        <span class="product-name">{{ product.productName }}</span>
        <Tag
          :value="`${product.scenarioCount} scenarios`"
          severity="secondary"
          class="scenario-count"
        />
      </div>

      <div
        v-if="expandedProducts.has(product.productCode)"
        class="scenarios-list"
      >
        <div
          v-for="scenario in product.scenarios"
          :key="scenario.scenarioCode"
          class="scenario-item"
          :class="{ selected: selectedScenario === scenario.scenarioCode }"
          @click="selectScenario(scenario.scenarioCode)"
        >
          <span class="scenario-name">{{ scenario.scenarioName }}</span>
          <Tag
            :value="`${scenario.transactionTypeCount} types`"
            severity="info"
            class="type-count"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface Scenario {
  scenarioCode: string;
  scenarioName: string;
  transactionTypeCount: number;
}

interface Product {
  productCode: string;
  productName: string;
  scenarioCount: number;
  scenarios: Scenario[];
}

const props = defineProps<{
  products: Product[];
  loading?: boolean;
}>();

const emit = defineEmits<{
  selectProduct: [productCode: string];
  selectScenario: [scenarioCode: string];
}>();

const expandedProducts = ref<Set<string>>(new Set());
const selectedScenario = ref<string | null>(null);

const toggleProduct = (productCode: string) => {
  if (expandedProducts.value.has(productCode)) {
    expandedProducts.value.delete(productCode);
  } else {
    expandedProducts.value.add(productCode);
    emit('selectProduct', productCode);
  }
};

const selectScenario = (scenarioCode: string) => {
  selectedScenario.value = scenarioCode;
  emit('selectScenario', scenarioCode);
};
</script>

<style scoped>
.product-scenario-tree {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.product-item {
  border-bottom: 1px solid #e5e7eb;
}

.product-item:last-child {
  border-bottom: none;
}

.product-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  background: #f9fafb;
  transition: background 0.2s;
}

.product-header:hover {
  background: #f3f4f6;
}

.toggle-icon {
  margin-right: 8px;
  transition: transform 0.2s;
  font-size: 12px;
}

.product-name {
  flex: 1;
  font-weight: 500;
  color: #111827;
}

.scenario-count {
  font-size: 12px;
}

.scenarios-list {
  background: #fff;
}

.scenario-item {
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 40px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.2s;
}

.scenario-item:last-child {
  border-bottom: none;
}

.scenario-item:hover {
  background: #f9fafb;
}

.scenario-item.selected {
  background: #eff6ff;
  border-left: 3px solid #3b82f6;
}

.scenario-name {
  flex: 1;
  color: #374151;
  font-size: 14px;
}

.type-count {
  font-size: 11px;
}
</style>

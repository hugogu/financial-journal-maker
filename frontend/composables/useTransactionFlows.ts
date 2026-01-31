// Composable for transaction flow API calls
import { ref } from 'vue';

export const useTransactionFlows = () => {
  const loading = ref(false);
  const error = ref<string | null>(null);

  const API_BASE = '/api/v1/transaction-flows';

  /**
   * T024: List products with pagination and search
   */
  const listProducts = async (search?: string, page = 0, size = 20) => {
    loading.value = true;
    error.value = null;
    try {
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      params.append('page', page.toString());
      params.append('size', size.toString());

      const response = await fetch(`${API_BASE}/products?${params}`);
      if (!response.ok) throw new Error('Failed to fetch products');
      return await response.json();
    } catch (e: any) {
      error.value = e.message;
      throw e;
    } finally {
      loading.value = false;
    }
  };

  /**
   * T024: Get product details
   */
  const getProduct = async (productCode: string) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await fetch(`${API_BASE}/products/${productCode}`);
      if (!response.ok) throw new Error('Failed to fetch product');
      return await response.json();
    } catch (e: any) {
      error.value = e.message;
      throw e;
    } finally {
      loading.value = false;
    }
  };

  /**
   * T024: List scenarios for a product
   */
  const listScenarios = async (productCode: string) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await fetch(`${API_BASE}/products/${productCode}/scenarios`);
      if (!response.ok) throw new Error('Failed to fetch scenarios');
      return await response.json();
    } catch (e: any) {
      error.value = e.message;
      throw e;
    } finally {
      loading.value = false;
    }
  };

  /**
   * T024: List all transaction flows with filters
   */
  const listTransactionFlows = async (
    productCode?: string,
    scenarioCode?: string,
    search?: string,
    page = 0,
    size = 20
  ) => {
    loading.value = true;
    error.value = null;
    try {
      const params = new URLSearchParams();
      if (productCode) params.append('productCode', productCode);
      if (scenarioCode) params.append('scenarioCode', scenarioCode);
      if (search) params.append('search', search);
      params.append('page', page.toString());
      params.append('size', size.toString());

      const response = await fetch(`${API_BASE}?${params}`);
      if (!response.ok) throw new Error('Failed to fetch transaction flows');
      return await response.json();
    } catch (e: any) {
      error.value = e.message;
      throw e;
    } finally {
      loading.value = false;
    }
  };

  /**
   * T032: Get transaction flow details
   */
  const getTransactionFlow = async (transactionTypeCode: string) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await fetch(`${API_BASE}/${transactionTypeCode}`);
      if (!response.ok) throw new Error('Failed to fetch transaction flow');
      return await response.json();
    } catch (e: any) {
      error.value = e.message;
      throw e;
    } finally {
      loading.value = false;
    }
  };

  return {
    loading,
    error,
    listProducts,
    getProduct,
    listScenarios,
    listTransactionFlows,
    getTransactionFlow,
  };
};

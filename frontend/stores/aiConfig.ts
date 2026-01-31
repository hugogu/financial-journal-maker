import { defineStore } from 'pinia'

export interface AIConfig {
  id: number
  providerName: string
  modelName: string
  endpoint: string
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface AIConfigState {
  configs: AIConfig[]
  activeConfig: AIConfig | null
  loading: boolean
  error: string | null
}

export const useAIConfigStore = defineStore('aiConfig', {
  state: (): AIConfigState => ({
    configs: [],
    activeConfig: null,
    loading: false,
    error: null,
  }),

  getters: {
    hasActiveConfig: (state: AIConfigState) => state.activeConfig !== null,
    configById: (state: AIConfigState) => (id: number) => 
      state.configs.find((c: AIConfig) => c.id === id),
  },

  actions: {
    setConfigs(configs: AIConfig[]) {
      this.configs = configs
      this.activeConfig = configs.find((c: AIConfig) => c.isActive) || null
    },

    setActiveConfig(config: AIConfig | null) {
      this.activeConfig = config
    },

    addConfig(config: AIConfig) {
      this.configs.push(config)
    },

    updateConfig(config: AIConfig) {
      const index = this.configs.findIndex((c: AIConfig) => c.id === config.id)
      if (index !== -1) {
        this.configs[index] = config
      }
      if (config.isActive) {
        this.configs.forEach((c: AIConfig) => {
          if (c.id !== config.id) c.isActive = false
        })
        this.activeConfig = config
      }
    },

    removeConfig(configId: number) {
      this.configs = this.configs.filter((c: AIConfig) => c.id !== configId)
      if (this.activeConfig?.id === configId) {
        this.activeConfig = null
      }
    },

    setLoading(loading: boolean) {
      this.loading = loading
    },

    setError(error: string | null) {
      this.error = error
    },
  },
})

export default defineNuxtConfig({
  devtools: { enabled: true },
  
  modules: [
    '@pinia/nuxt',
    'nuxt-primevue',
  ],

  primevue: {
    options: {
      ripple: true,
    },
    components: {
      include: '*',
    },
  },

  css: [
    'primevue/resources/themes/lara-light-blue/theme.css',
    'primevue/resources/primevue.css',
    'primeicons/primeicons.css',
  ],

  runtimeConfig: {
    public: {
      apiBase: '/api/v1',
    },
  },

  nitro: {
    routeRules: {
      '/api/v1/sessions/**/messages/stream': {
        proxy: false,
      },
      '/api/v1/**': {
        proxy: 'http://localhost:8080/api/v1/**',
        fetchOptions: {
          redirect: 'manual',
        },
      },
    },
  },

  typescript: {
    strict: true,
  },

  compatibilityDate: '2024-01-01',
})

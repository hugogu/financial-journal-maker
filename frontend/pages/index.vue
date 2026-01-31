<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">
          Financial Journal Maker
        </h1>
        <p class="text-xl text-gray-600">
          AI-Powered Chart of Accounts Design & Management
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- AI Analysis Sessions -->
        <NuxtLink
          to="/analysis"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
            </div>
            <h2 class="ml-4 text-xl font-semibold text-gray-900">AI Analysis</h2>
          </div>
          <p class="text-gray-600">
            Start a new AI-powered analysis session to design your chart of accounts
          </p>
        </NuxtLink>

        <!-- Transaction Flows -->
        <NuxtLink
          to="/flows"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-indigo-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h2 class="ml-4 text-xl font-semibold text-gray-900">Transaction Flows</h2>
          </div>
          <p class="text-gray-600">
            Browse and visualize transaction flow designs from AI sessions
          </p>
        </NuxtLink>

        <!-- Chart of Accounts -->
        <a
          href="http://localhost:8080/swagger-ui.html"
          target="_blank"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h2 class="ml-4 text-xl font-semibold text-gray-900">API Documentation</h2>
          </div>
          <p class="text-gray-600">
            Explore the REST API documentation and test endpoints
          </p>
        </a>

        <!-- Admin Configuration -->
        <NuxtLink
          to="/admin/ai-config"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
        >
          <div class="flex items-center mb-4">
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <h2 class="ml-4 text-xl font-semibold text-gray-900">AI Configuration</h2>
          </div>
          <p class="text-gray-600">
            Configure AI providers and manage API keys
          </p>
        </NuxtLink>
      </div>

      <div class="mt-12 bg-white rounded-lg shadow-md p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Quick Start</h3>
        <ol class="list-decimal list-inside space-y-2 text-gray-600">
          <li>Configure your AI provider in the admin panel</li>
          <li>Start a new AI analysis session</li>
          <li>Interact with the AI to design your chart of accounts</li>
          <li>Review and confirm design decisions</li>
          <li>Export the final design to your accounting system</li>
        </ol>
      </div>

      <div class="mt-8 text-center text-gray-500 text-sm">
        <p>Backend API: <a href="http://localhost:8080" target="_blank" class="text-blue-600 hover:underline">http://localhost:8080</a></p>
        <p class="mt-2">Version 1.0.0-SNAPSHOT</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
useHead({
  title: 'Financial Journal Maker - Home',
  meta: [
    { name: 'description', content: 'AI-Powered Chart of Accounts Design & Management' }
  ]
})
</script>

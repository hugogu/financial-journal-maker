<template>
  <div class="layout">
    <header class="header">
      <nav class="nav">
        <NuxtLink to="/" class="logo">Financial Journal Maker</NuxtLink>
        <div class="nav-links">
          <NuxtLink to="/analysis" class="nav-link">Analysis</NuxtLink>
          <NuxtLink to="/flows" class="nav-link">Flows</NuxtLink>
          <NuxtLink to="/admin/ai-config" class="nav-link">AI Config</NuxtLink>
          <NuxtLink to="/admin/prompts" class="nav-link">Prompts</NuxtLink>
        </div>
      </nav>
    </header>
    <main class="main">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #1e40af;
  color: white;
  padding: 1rem 2rem;
}

.nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  font-size: 1.25rem;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: 1.5rem;
}

.nav-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: color 0.2s;
}

.nav-link:hover {
  color: white;
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
</style>

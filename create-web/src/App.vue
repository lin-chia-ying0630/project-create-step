<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { navigationItems } from './router'
</script>

<template>
  <main class="app-shell">
    <section class="workspace">
      <header class="topbar">
        <p class="eyebrow">新契約作業系統</p>
        <h1>保單新契約作業</h1>
      </header>
      <div class="work-layout">
        <nav class="side-menu" aria-label="新契約功能">
          <RouterLink v-for="item in navigationItems" :key="item.name" :to="{ name: item.name }">
            <span class="menu-icon" aria-hidden="true">{{ item.icon }}</span
            >{{ item.label }}
          </RouterLink>
        </nav>
        <section class="work-content">
          <RouterView />
        </section>
      </div>
    </section>
  </main>
</template>

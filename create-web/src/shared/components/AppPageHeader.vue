<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
  status?: string
  documentStyle?: boolean
}>()
</script>

<template>
  <header class="page-header" :class="{ 'application-document-header': documentStyle }">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <p>{{ description }}</p>
    </div>
    <span v-if="status" class="status-chip">{{ status }}</span>
  </header>
</template>

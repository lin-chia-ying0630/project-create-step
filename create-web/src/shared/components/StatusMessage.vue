<script setup lang="ts">
defineProps<{
  message: string | null
  type: 'success' | 'error'
}>()
</script>

<template>
  <p v-if="message" class="status-message" :class="type" role="status">{{ message }}</p>
</template>

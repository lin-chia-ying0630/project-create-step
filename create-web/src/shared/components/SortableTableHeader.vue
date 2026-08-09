<script setup lang="ts">
const props = defineProps<{ field: string; label: string; activeField: string; direction: 'asc' | 'desc' }>()
const emit = defineEmits<{ sort: [field: string, direction: 'asc' | 'desc'] }>()

/** 同欄位切換升降冪；改選欄位時從升冪開始。 */
function toggleSort() {
  const next = props.activeField === props.field && props.direction === 'asc' ? 'desc' : 'asc'
  emit('sort', props.field, next)
}
</script>

<template>
  <th scope="col" :aria-sort="activeField === field ? (direction === 'asc' ? 'ascending' : 'descending') : 'none'">
    <button type="button" class="sortable-header-button" @click="toggleSort">
      {{ label }} <span aria-hidden="true">{{ activeField === field ? (direction === 'asc' ? '▲' : '▼') : '↕' }}</span>
    </button>
  </th>
</template>

<style scoped>
.sortable-header-button {
  display: inline-flex;
  gap: 6px;
  border: 0;
  background: transparent;
  padding: 0;
  color: inherit;
  font: inherit;
  cursor: pointer;
}
</style>

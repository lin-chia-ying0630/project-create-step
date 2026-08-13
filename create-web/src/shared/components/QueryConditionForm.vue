<script setup lang="ts">
withDefaults(
  defineProps<{
    description?: string
  }>(),
  { description: '請選擇或輸入查詢條件；留白可查詢全部資料' },
)
</script>

<template>
  <div class="panel-title">
    <h3>查詢條件</h3>
    <small>{{ description }}</small>
  </div>
  <div class="search-row query-condition-form">
    <slot />
    <slot name="actions" />
  </div>
</template>

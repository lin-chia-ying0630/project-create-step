<template>
  <section class="query-list-panels">
    <article class="panel">
      <slot name="query" />
    </article>
    <article class="panel">
      <slot name="list" />
    </article>
  </section>
</template>

<script setup lang="ts">
/**
 * 統一查詢型頁面的兩區塊結構：上方查詢條件、下方查詢結果清單。
 * 業務頁只提供區塊內容，不得再自行決定 Panel 包覆層級與間距。
 */
</script>

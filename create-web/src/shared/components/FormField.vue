<script setup lang="ts">
withDefaults(defineProps<{ label: string; required?: boolean; wide?: boolean }>(), {
  required: false,
  wide: false,
})
</script>

<template>
  <label :class="{ 'wide-field': wide }">
    {{ label }}<span v-if="required" aria-hidden="true">＊</span>
    <slot />
  </label>
</template>

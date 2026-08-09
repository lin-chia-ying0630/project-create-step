<script setup lang="ts">
interface SectionTabOption {
  value: string | number
  label: string
  caption: string
}

defineProps<{
  modelValue: string | number
  items: readonly SectionTabOption[]
  navigationLabel: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()

/** 將使用者選取的功能頁籤回傳給業務頁，不在共用元件保存第二份狀態。 */
function select(value: string | number) {
  emit('update:modelValue', value)
}
</script>

<template>
  <nav class="section-tab-navigator" :aria-label="navigationLabel">
    <button
      v-for="item in items"
      :key="item.value"
      type="button"
      :class="{ active: modelValue === item.value }"
      @click="select(item.value)"
    >
      <small>{{ item.caption }}</small
      ><b>{{ item.label }}</b>
    </button>
  </nav>
</template>

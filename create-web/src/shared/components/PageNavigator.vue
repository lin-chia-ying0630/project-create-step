<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: number
    total: number
    prefix?: string
  }>(),
  { prefix: '' },
)
const emit = defineEmits<{ 'update:modelValue': [value: number] }>()

/** 產生一致的頁次文字，支援要保書使用的「第／共」格式。 */
const pageLabel = computed(() =>
  props.prefix
    ? `${props.prefix} ${props.modelValue + 1} 頁／共 ${props.total} 頁`
    : `${props.modelValue + 1}／${props.total}`,
)

/** 在不超出第一頁的前提下回到上一頁。 */
function previous() {
  if (props.modelValue > 0) emit('update:modelValue', props.modelValue - 1)
}

/** 在不超出總頁數的前提下前往下一頁。 */
function next() {
  if (props.modelValue < props.total - 1) emit('update:modelValue', props.modelValue + 1)
}
</script>

<template>
  <nav class="shared-page-actions" aria-label="分頁導覽">
    <button type="button" class="secondary-button" :disabled="modelValue === 0" @click="previous">
      上一頁
    </button>
    <span>{{ pageLabel }}</span>
    <button
      type="button"
      class="secondary-button"
      :disabled="modelValue >= total - 1"
      @click="next"
    >
      下一頁
    </button>
  </nav>
</template>

<style scoped>
.shared-page-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 18px 0;
}
.shared-page-actions span {
  color: #475569;
  font-weight: 700;
}
@media (max-width: 760px) {
  .shared-page-actions {
    gap: 8px;
  }
  .shared-page-actions button {
    flex: 1;
  }
  .shared-page-actions span {
    flex: 0 0 auto;
    text-align: center;
  }
}
</style>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: number
    total: number
    prefix?: string
    pageSize?: number
    pageSizeOptions?: number[]
  }>(),
  { prefix: '', pageSize: undefined, pageSizeOptions: () => [10, 20, 50, 100] },
)
const emit = defineEmits<{
  'update:modelValue': [value: number]
  'update:pageSize': [value: number]
}>()

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

/** 變更每頁筆數時通知父層回到第一頁並重新執行後端查詢。 */
function changePageSize(event: Event) {
  emit('update:pageSize', Number((event.target as HTMLSelectElement).value))
}
</script>

<template>
  <nav class="shared-page-actions" aria-label="分頁導覽">
    <label v-if="pageSize !== undefined" class="page-size-control">
      每頁
      <select :value="pageSize" aria-label="每頁筆數" @change="changePageSize">
        <option v-for="option in pageSizeOptions" :key="option" :value="option">
          {{ option }} 筆
        </option>
      </select>
    </label>
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

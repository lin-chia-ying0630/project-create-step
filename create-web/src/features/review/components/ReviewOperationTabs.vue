<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import SectionTabNavigator from '../../../shared/components/SectionTabNavigator.vue'
import { reviewApi } from '../api/reviewApi'
import type { ReviewOperationOption } from '../types/review'

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const reviewOperationOptions = ref<ReviewOperationOption[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const tabItems = computed(() =>
  reviewOperationOptions.value.map((option, index) => ({
    ...option,
    caption: `第 ${index + 1} 頁`,
  })),
)

/** 從後端資料字典載入唯一的覆核功能中英對照。 */
async function loadOptions() {
  loading.value = true
  error.value = null
  try {
    reviewOperationOptions.value = await reviewApi.findOperationOptions()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核功能對照載入失敗'
  } finally {
    loading.value = false
  }
}

/** 將共用頁籤選取值轉成覆核功能使用的英文代碼。 */
function selectOperation(value: string | number) {
  emit('update:modelValue', String(value))
}

onMounted(loadOptions)
</script>

<template>
  <SectionTabNavigator
    v-if="tabItems.length"
    :model-value="modelValue"
    :items="tabItems"
    navigation-label="覆核功能分類"
    @update:model-value="selectOperation"
  />
  <p v-else-if="loading" class="status-message">覆核功能載入中…</p>
  <p v-else-if="error" class="status-message error" role="alert">{{ error }}</p>
</template>

<script setup lang="ts">
import QueryConditionForm from './QueryConditionForm.vue'

withDefaults(
  defineProps<{
    modelValue: string
    buttonLabel: string
    description: string
    fieldLabel?: string
    loading?: boolean
    maxLength?: number
  }>(),
  { loading: false, maxLength: 200, fieldLabel: '查詢條件' },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
  clear: []
}>()

/** 統一回傳單一查詢值，不在各業務頁重複處理輸入事件。 */
function updateValue(event: Event) {
  emit('update:modelValue', (event.target as HTMLInputElement).value)
}
</script>

<template>
  <form class="single-query-form" @submit.prevent="emit('submit')">
    <QueryConditionForm :description="description">
      <label>
        {{ fieldLabel }}
        <input
          :value="modelValue"
          :maxlength="maxLength"
          placeholder="輸入完整查詢值"
          @input="updateValue"
        />
      </label>
      <template #actions>
        <div class="search-actions">
          <button
            v-if="modelValue.trim()"
            type="button"
            class="secondary-button"
            :disabled="loading"
            @click="emit('clear')"
          >
            清除
          </button>
          <button type="submit" class="primary-button" :disabled="loading">
            {{ loading ? '查詢中…' : buttonLabel }}
          </button>
        </div>
      </template>
    </QueryConditionForm>
  </form>
</template>

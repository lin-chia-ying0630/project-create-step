<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { codeDefinitionApi } from '../api/codeDefinitionApi'
import type { CodeDefinitionOption } from '../types/codeDefinition'
import FormField from './FormField.vue'

const props = withDefaults(
  defineProps<{
    modelValue: string
    codeGroup: string
    codeField: string
    label: string
    required?: boolean
    disabled?: boolean
    minimumQueryLength?: number
    placeholder?: string
  }>(),
  {
    required: false,
    disabled: false,
    minimumQueryLength: 2,
    placeholder: '請輸入代碼或中文關鍵字',
  },
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const inputText = ref(props.modelValue)
const options = ref<CodeDefinitionOption[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const open = ref(false)
const inputElement = ref<HTMLInputElement | null>(null)
let debounceTimer: ReturnType<typeof setTimeout> | null = null
let controller: AbortController | null = null

/** 輸入時清除舊代碼，延遲搜尋並取消尚未完成的舊請求。 */
function updateQuery(event: Event) {
  const target = event.target as HTMLInputElement
  inputText.value = target.value
  emit('update:modelValue', '')
  target.setCustomValidity(props.required ? '請從搜尋結果選擇有效代碼' : '')
  options.value = []
  error.value = null
  if (debounceTimer) clearTimeout(debounceTimer)
  controller?.abort()
  const query = inputText.value.trim()
  if (query.length < props.minimumQueryLength) {
    open.value = false
    return
  }
  debounceTimer = setTimeout(() => void search(query), 250)
}

/** 每次只搜尋前 20 筆符合的資料庫代碼，避免下載完整大型代碼表。 */
async function search(query: string) {
  const requestController = new AbortController()
  controller = requestController
  loading.value = true
  open.value = true
  try {
    const result = await codeDefinitionApi.findActiveOptionPage(
      props.codeGroup,
      props.codeField,
      1,
      20,
      query,
      requestController.signal,
    )
    options.value = result.items
  } catch (e) {
    if (requestController.signal.aborted) return
    error.value = e instanceof Error ? e.message : '代碼搜尋失敗'
  } finally {
    if (!requestController.signal.aborted) loading.value = false
  }
}

/** 選取建議項目後只回傳正式代碼，輸入框顯示代碼與繁中說明。 */
function selectOption(option: CodeDefinitionOption) {
  emit('update:modelValue', option.code)
  inputText.value = `${option.code}｜${option.description}`
  inputElement.value?.setCustomValidity('')
  options.value = []
  open.value = false
}

/** 離開元件時取消計時器與網路請求，避免背景工作更新已卸載畫面。 */
onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
  controller?.abort()
})
</script>

<template>
  <FormField :label="label" :required="required">
    <div class="autocomplete-field">
      <input
        ref="inputElement"
        :value="inputText"
        type="search"
        autocomplete="off"
        :required="required"
        :disabled="disabled"
        :placeholder="placeholder"
        :aria-expanded="open"
        @input="updateQuery"
      />
      <div v-if="open" class="autocomplete-results" role="listbox">
        <p v-if="loading" class="autocomplete-state">搜尋中…</p>
        <button
          v-for="option in options"
          v-else
          :key="option.code"
          type="button"
          role="option"
          @click="selectOption(option)"
        >
          {{ option.code }}｜{{ option.description }}
        </button>
        <p v-if="!loading && !error && !options.length" class="autocomplete-state">查無符合代碼</p>
        <p v-if="error" class="autocomplete-state error" role="alert">{{ error }}</p>
      </div>
    </div>
  </FormField>
</template>

<style scoped lang="scss">
.autocomplete-field {
  position: relative;
}

.autocomplete-results {
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  right: 0;
  left: 0;
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid var(--border-color, #ccd8df);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 24px rgb(25 55 70 / 16%);
}

.autocomplete-results button {
  display: block;
  width: 100%;
  min-height: 44px;
  padding: 8px 12px;
  border: 0;
  border-bottom: 1px solid #e5ecef;
  background: transparent;
  text-align: left;
}

.autocomplete-results button:hover,
.autocomplete-results button:focus-visible {
  background: #eef9f7;
}

.autocomplete-state {
  margin: 0;
  padding: 10px 12px;
}
</style>

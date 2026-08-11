<script setup lang="ts">
import type { CodeDefinitionOption } from '../types/codeDefinition'
import FormField from './FormField.vue'

withDefaults(
  defineProps<{
    modelValue: string
    label: string
    options: CodeDefinitionOption[]
    required?: boolean
    disabled?: boolean
    placeholder?: string
  }>(),
  { required: false, disabled: false, placeholder: '請選擇' },
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

/** 回傳資料庫代碼值；中文只用於顯示，不另外保存到表單資料。 */
function updateValue(event: Event) {
  emit('update:modelValue', (event.target as HTMLSelectElement).value)
}
</script>

<template>
  <FormField :label="label" :required="required">
    <select :value="modelValue" :required="required" :disabled="disabled" @change="updateValue">
      <option v-if="!modelValue" value="" disabled>{{ placeholder }}</option>
      <option v-for="option in options" :key="option.code" :value="option.code">
        {{ option.code }}｜{{ option.description }}
      </option>
    </select>
  </FormField>
</template>

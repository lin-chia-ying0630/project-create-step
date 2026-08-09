<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { codeDefinitionApi } from '../api/codeDefinitionApi'
import type { CodeDefinitionOption } from '../types/codeDefinition'

const codeGroup = ref('customer-kyc')
const codeField = ref('occupation_code')
const items = ref<CodeDefinitionOption[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

/** 依輸入的群組與欄位載入資料庫中目前生效的 Code Table 對照。 */
async function search() {
  loading.value = true
  error.value = null
  try {
    items.value = await codeDefinitionApi.findActiveOptions(
      codeGroup.value.trim(),
      codeField.value.trim(),
    )
  } catch (e) {
    items.value = []
    error.value = e instanceof Error ? e.message : 'Code Table 對照載入失敗'
  } finally {
    loading.value = false
  }
}

onMounted(search)
</script>

<template>
  <section class="content-page code-definition-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">CODE TABLE</p>
        <h2>代碼對照查詢</h2>
        <p>查詢資料庫目前生效的動態代碼與繁體中文說明。</p>
      </div>
      <span class="status-chip">{{ items.length }} 筆</span>
    </header>

    <form class="panel lookup-form" @submit.prevent="search">
      <div class="field-grid">
        <label
          >代碼群組
          <input
            v-model.trim="codeGroup"
            required
            maxlength="64"
            pattern="[a-z0-9-]+"
            autocomplete="off"
            placeholder="例如 customer-kyc"
          />
        </label>
        <label
          >代碼欄位
          <input
            v-model.trim="codeField"
            required
            maxlength="64"
            pattern="[a-z0-9_]+"
            autocomplete="off"
            placeholder="例如 occupation_code"
          />
        </label>
      </div>
      <div class="form-actions">
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '查詢中…' : '查詢對照' }}
        </button>
      </div>
    </form>

    <article class="panel section-gap">
      <div class="panel-title">
        <h3>對照結果</h3>
        <small>{{ codeGroup }}／{{ codeField }}</small>
      </div>
      <div class="data-table-scope">
        <table class="data-table">
          <thead>
            <tr>
              <th>代碼</th>
              <th>繁體中文說明</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.code">
              <td class="code-value">{{ item.code }}</td>
              <td>{{ item.description }}</td>
            </tr>
            <tr v-if="!loading && !items.length">
              <td colspan="2">查無目前生效的代碼對照。</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <p v-if="error" class="status-message error" role="alert">{{ error }}</p>
  </section>
</template>

<style scoped lang="scss">
.code-definition-page {
  max-width: 100%;
}

.lookup-form {
  margin-top: 4px;
}

.code-value {
  overflow-wrap: anywhere;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
</style>

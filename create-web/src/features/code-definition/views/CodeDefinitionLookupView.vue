<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import QueryListPanels from '../../../shared/components/QueryListPanels.vue'
import QueryConditionForm from '../../../shared/components/QueryConditionForm.vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import { codeDefinitionApi } from '../../../shared/api/codeDefinitionApi'
import type {
  CodeDefinitionOption,
  CodeDefinitionTableOption,
} from '../../../shared/types/codeDefinition'
import type { PageResult } from '../../../shared/types/common'

const tableOptions = ref<CodeDefinitionTableOption[]>([])
const selectedTableKey = ref('customer-kyc::occupation_code')
const resultPage = ref<PageResult<CodeDefinitionOption>>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const loading = ref(false)
const error = ref<string | null>(null)
const pageIndex = ref(0)

const selectedTable = computed(() =>
  tableOptions.value.find(
    (table) => `${table.codeGroup}::${table.codeField}` === selectedTableKey.value,
  ),
)

/** 依下拉選取的群組與欄位載入資料庫中目前生效的 Code Definitions。 */
async function search(page = 1) {
  const table = selectedTable.value
  if (!table) return
  loading.value = true
  error.value = null
  try {
    resultPage.value = await codeDefinitionApi.findActiveOptionPage(
      table.codeGroup,
      table.codeField,
      page,
      resultPage.value.pageSize,
    )
    pageIndex.value = resultPage.value.page - 1
  } catch (e) {
    resultPage.value = { ...resultPage.value, items: [], totalItems: 0, totalPages: 0 }
    error.value = e instanceof Error ? e.message : 'Code Definitions 載入失敗'
  } finally {
    loading.value = false
  }
}

/** 變更共用每頁筆數後回到第一頁。 */
function changePageSize(value: number) {
  resultPage.value.pageSize = value
  void search(1)
}

/** 將共用元件的零起算頁碼轉成 API 的一日起算頁碼。 */
function changePage(value: number) {
  pageIndex.value = value
  void search(value + 1)
}

/** 初次進入時先載入可查詢代碼表，再預設顯示正式職業代碼。 */
async function initialize() {
  loading.value = true
  error.value = null
  try {
    tableOptions.value = await codeDefinitionApi.findActiveTables()
    if (!selectedTable.value && tableOptions.value.length) {
      const first = tableOptions.value[0]
      selectedTableKey.value = `${first.codeGroup}::${first.codeField}`
    }
    await search()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '代碼表清單載入失敗'
  } finally {
    loading.value = false
  }
}

onMounted(initialize)
</script>

<template>
  <section class="content-page code-definition-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">CODE DEFINITIONS</p>
        <h2>代碼定義查詢</h2>
        <p>查詢資料庫目前生效的動態代碼與繁體中文說明。</p>
      </div>
      <span class="status-chip">{{ resultPage.totalItems }} 筆</span>
    </header>

    <QueryListPanels>
      <template #query>
        <QueryConditionForm description="請選擇要查詢的代碼表">
          <label
            >代碼表
            <select v-model="selectedTableKey" :disabled="loading">
              <option
                v-for="table in tableOptions"
                :key="`${table.codeGroup}::${table.codeField}`"
                :value="`${table.codeGroup}::${table.codeField}`"
              >
                {{ table.codeField }}｜{{ table.codeFieldDescription }}（{{
                  table.codeGroupDescription
                }}）
              </option>
            </select>
          </label>
          <template #actions>
            <button type="button" class="primary-button" :disabled="loading" @click="search(1)">
              {{ loading ? '查詢中…' : '查詢代碼' }}
            </button>
          </template>
        </QueryConditionForm>
      </template>

      <template #list>
        <div class="panel-title">
          <h3>對照結果</h3>
          <small>{{ selectedTable?.codeGroup }}／{{ selectedTable?.codeField }}</small>
        </div>
        <div class="data-table-scope">
          <table class="data-table">
            <thead>
              <tr>
                <th>代碼</th>
                <th>繁體中文說明</th>
                <th>英文說明</th>
                <th>大分類</th>
                <th>中分類</th>
                <th>工作性質</th>
                <th>來源版本</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in resultPage.items" :key="item.code">
                <td class="code-value">{{ item.code }}</td>
                <td>{{ item.description }}</td>
                <td>{{ item.descriptionEn || '—' }}</td>
                <td>
                  {{ item.classificationCode || '—' }}
                  {{ item.classificationDescription || '' }}
                </td>
                <td>{{ item.breakdownCode || '—' }} {{ item.breakdownDescription || '' }}</td>
                <td>{{ item.natureOfWork || '—' }}</td>
                <td>{{ item.sourceVersion || '—' }}</td>
              </tr>
              <tr v-if="!loading && !error && !resultPage.items.length">
                <td colspan="7">查無目前生效的代碼對照。</td>
              </tr>
            </tbody>
          </table>
        </div>
        <PageNavigator
          v-if="resultPage.totalPages > 0"
          :model-value="pageIndex"
          :total="resultPage.totalPages"
          :page-size="resultPage.pageSize"
          prefix="代碼對照清單"
          @update:model-value="changePage"
          @update:page-size="changePageSize"
        />
      </template>
    </QueryListPanels>

    <p v-if="error" class="status-message error" role="alert">{{ error }}</p>
  </section>
</template>

<style scoped lang="scss">
.lookup-form {
  margin-top: 4px;
}

.code-value {
  overflow-wrap: anywhere;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}
</style>

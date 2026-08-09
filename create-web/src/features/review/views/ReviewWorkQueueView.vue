<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import SortableTableHeader from '../../../shared/components/SortableTableHeader.vue'
import { reviewApi } from '../api/reviewApi'
import type { ReviewDetail, ReviewPageResult, ReviewSummary } from '../types/review'

const operationFilters = [
  { value: '', label: '全部覆核' },
  { value: 'CUSTOMER_CREATE', label: '客戶建立' },
  { value: 'APPLICATION_CREATE', label: '保單登打' },
  { value: 'POLICY_REVERSAL', label: '承保撤回' },
  { value: 'UNDERWRITING_BATCH_ENQUEUE', label: '新契約批次承保作業' },
  { value: 'UNDERWRITING_DECISION', label: '核保審查結果' },
  { value: 'INITIAL_PREMIUM_MATCH', label: '首期保費資料' },
] as const

const reviewPage = ref<ReviewPageResult>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const selected = ref<ReviewDetail | null>(null)
const selectedOperation = ref('')
const queryInput = ref('')
const appliedQuery = ref('')
const comment = ref('')
const loading = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)
const sortField = ref('reviewId')
const sortDirection = ref<'asc' | 'desc'>('asc')

const visibleItems = computed(() =>
  selectedOperation.value
    ? reviewPage.value.items.filter((item) => item.operationType === selectedOperation.value)
    : reviewPage.value.items,
)

/** 載入待覆核案件並重設已失效的明細選取。 */
async function refresh(page = reviewPage.value.page) {
  loading.value = true
  error.value = null
  try {
    reviewPage.value = await reviewApi.findPending(
      page,
      reviewPage.value.pageSize,
      `${sortField.value},${sortDirection.value}`,
      appliedQuery.value,
    )
    if (
      selected.value &&
      !reviewPage.value.items.some((item) => item.reviewId === selected.value?.reviewId)
    ) {
      selected.value = null
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核待辦載入失敗'
  } finally {
    loading.value = false
  }
}

/** 套用完整識別值並由第一頁查詢相關待覆核案件。 */
function search() {
  appliedQuery.value = queryInput.value.trim()
  void refresh(1)
}

/** 清除查詢條件並回復全部待覆核案件。 */
function clearSearch() {
  queryInput.value = ''
  appliedQuery.value = ''
  void refresh(1)
}

/** 變更共用每頁筆數後回到第一頁，避免頁碼落在新範圍之外。 */
function changePageSize(pageSize: number) {
  reviewPage.value.pageSize = pageSize
  void refresh(1)
}

/** 共用排序表頭切換前三欄後，重新向後端取得排序完成的待覆核清單。 */
function changeSort(field: string, direction: 'asc' | 'desc') {
  sortField.value = field
  sortDirection.value = direction
  void refresh(1)
}

/** 取得待覆核 payload，畫面以一格一欄呈現。 */
async function openDetail(reviewId: string) {
  loading.value = true
  error.value = null
  try {
    selected.value = await reviewApi.findById(reviewId)
    comment.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核明細載入失敗'
  } finally {
    loading.value = false
  }
}

/** 將巢狀 payload 轉成唯讀顯示文字，不在前端裁決業務規則。 */
function displayValue(value: unknown): string {
  if (value === null || value === undefined || value === '') return '—'
  if (typeof value === 'object') return JSON.stringify(value, null, 2)
  return String(value)
}

/** 核准案件後重新讀取待辦，正式異動只由後端交易執行。 */
async function approve() {
  if (!selected.value) return
  loading.value = true
  error.value = null
  try {
    await reviewApi.approve(selected.value.reviewId, comment.value.trim())
    message.value = `覆核案件 ${selected.value.reviewId} 已核准並套用。`
    selected.value = null
    await refresh()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核核准失敗'
  } finally {
    loading.value = false
  }
}

/** 退回案件後釋放待審鎖，正式資料維持不變。 */
async function reject() {
  if (!selected.value || !comment.value.trim()) return
  loading.value = true
  error.value = null
  try {
    await reviewApi.reject(selected.value.reviewId, comment.value.trim())
    message.value = `覆核案件 ${selected.value.reviewId} 已退回。`
    selected.value = null
    await refresh()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核退回失敗'
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <section class="content-page review-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">MAKER-CHECKER</p>
        <h2>新契約覆核工作台</h2>
        <p>所有資料異動須由不同人員核准後才寫入正式資料。</p>
      </div>
      <span class="status-chip">待覆核 {{ reviewPage.totalItems }} 件</span>
    </header>

    <nav class="review-tabs" aria-label="覆核功能分類">
      <button
        v-for="filter in operationFilters"
        :key="filter.value"
        type="button"
        :class="{ active: selectedOperation === filter.value }"
        @click="selectedOperation = filter.value"
      >
        {{ filter.label }}
      </button>
    </nav>

    <article class="panel">
      <div class="panel-title responsive-split-row">
        <div>
          <h3>查詢條件</h3>
          <small>客戶 ID 會列出其相關待覆核案件</small>
        </div>
      </div>
      <form class="search-row" @submit.prevent="search">
        <label
          >客戶 ID／要保書號碼／正式保單號碼
          <input v-model.trim="queryInput" maxlength="200" placeholder="輸入完整查詢值" />
        </label>
        <div class="search-actions">
          <button v-if="appliedQuery" type="button" class="secondary-button" @click="clearSearch">
            清除
          </button>
          <button type="submit" class="primary-button" :disabled="loading">查詢審核案件</button>
        </div>
      </form>
    </article>

    <article class="panel section-gap">
      <div class="panel-title responsive-split-row">
        <h3>待覆核案件</h3>
        <button class="secondary-button" :disabled="loading" @click="refresh()">重新整理</button>
      </div>
      <div class="data-table-scope">
        <table class="data-table">
          <thead>
            <tr>
              <th>操作</th>
              <SortableTableHeader
                field="reviewId"
                label="覆核編號"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <SortableTableHeader
                field="operationType"
                label="功能"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <SortableTableHeader
                field="businessKey"
                label="業務鍵"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <th>送審人</th>
              <th>送審時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in visibleItems" :key="item.reviewId">
              <td>
                <button class="secondary-button" @click="openDetail(item.reviewId)">覆核</button>
              </td>
              <td>{{ item.reviewId }}</td>
              <td>{{ item.operationDescription }}</td>
              <td>{{ item.businessKey }}</td>
              <td>{{ item.makerId }}</td>
              <td>{{ item.submittedAt }}</td>
            </tr>
            <tr v-if="!visibleItems.length">
              <td colspan="6">目前沒有待覆核案件。</td>
            </tr>
          </tbody>
        </table>
      </div>
      <PageNavigator
        v-if="reviewPage.totalPages > 0"
        :model-value="reviewPage.page - 1"
        :total="reviewPage.totalPages"
        :page-size="reviewPage.pageSize"
        prefix="待覆核案件"
        @update:model-value="refresh($event + 1)"
        @update:page-size="changePageSize"
      />
    </article>

    <article v-if="selected" class="panel section-gap">
      <div class="panel-title">
        <h3>{{ selected.operationDescription }}</h3>
        <small>{{ selected.reviewId }}</small>
      </div>
      <dl class="review-field-grid">
        <div v-for="(value, key) in selected.payload" :key="key">
          <dt>{{ key }}</dt>
          <dd>
            <pre>{{ displayValue(value) }}</pre>
          </dd>
        </div>
      </dl>
      <label class="section-gap"
        >覆核意見<textarea v-model.trim="comment" maxlength="500" rows="4" />
      </label>
      <div class="form-actions review-actions">
        <button
          class="secondary-button reject-button"
          :disabled="loading || !comment"
          @click="reject"
        >
          退回
        </button>
        <button class="primary-button" :disabled="loading" @click="approve">核准並套用</button>
      </div>
    </article>
    <p v-if="message" class="status-message success">{{ message }}</p>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>

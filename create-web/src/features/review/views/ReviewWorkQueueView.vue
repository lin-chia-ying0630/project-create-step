<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { reviewApi } from '../api/reviewApi'
import type { ReviewDetail, ReviewSummary } from '../types/review'

const operationFilters = [
  { value: '', label: '全部覆核' },
  { value: 'CUSTOMER_CREATE', label: '客戶建立' },
  { value: 'APPLICATION_CREATE', label: '保單登打' },
  { value: 'POLICY_REVERSAL', label: '承保撤回' },
  { value: 'UNDERWRITING_BATCH_ENQUEUE', label: '新契約批次承保作業' },
  { value: 'UNDERWRITING_DECISION', label: '核保審查結果' },
  { value: 'INITIAL_PREMIUM_MATCH', label: '首期保費資料' },
] as const

const items = ref<ReviewSummary[]>([])
const selected = ref<ReviewDetail | null>(null)
const selectedOperation = ref('')
const comment = ref('')
const loading = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)

const visibleItems = computed(() =>
  selectedOperation.value
    ? items.value.filter((item) => item.operationType === selectedOperation.value)
    : items.value,
)

/** 載入待覆核案件並重設已失效的明細選取。 */
async function refresh() {
  loading.value = true
  error.value = null
  try {
    items.value = (await reviewApi.findPending()).items
    if (selected.value && !items.value.some((item) => item.reviewId === selected.value?.reviewId)) {
      selected.value = null
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '覆核待辦載入失敗'
  } finally {
    loading.value = false
  }
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
      <span class="status-chip">待覆核 {{ items.length }} 件</span>
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
        <h3>待覆核案件</h3>
        <button class="secondary-button" :disabled="loading" @click="refresh">重新整理</button>
      </div>
      <div class="data-table-scope">
        <table class="data-table">
          <thead>
            <tr>
              <th>覆核編號</th>
              <th>功能</th>
              <th>業務鍵</th>
              <th>送審人</th>
              <th>送審時間</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in visibleItems" :key="item.reviewId">
              <td>{{ item.reviewId }}</td>
              <td>{{ item.operationDescription }}</td>
              <td>{{ item.businessKey }}</td>
              <td>{{ item.makerId }}</td>
              <td>{{ item.submittedAt }}</td>
              <td>
                <button class="secondary-button" @click="openDetail(item.reviewId)">覆核</button>
              </td>
            </tr>
            <tr v-if="!visibleItems.length">
              <td colspan="6">目前沒有待覆核案件。</td>
            </tr>
          </tbody>
        </table>
      </div>
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

<style scoped lang="scss">
.review-page {
  max-width: 100%;
}
.review-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
  overflow-x: auto;
}
.review-tabs button {
  flex: 0 0 auto;
  min-height: 44px;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  background: #fff;
  padding: 8px 14px;
}
.review-tabs button.active {
  border-color: #0f766e;
  background: #e6f4f2;
  color: #0f766e;
  font-weight: 700;
}
.review-field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  background: #dbe4ea;
}
.review-field-grid div {
  min-width: 0;
  background: #f8fafc;
  padding: 12px;
}
.review-field-grid dt {
  color: #647281;
  font-size: 0.82rem;
}
.review-field-grid dd {
  margin: 6px 0 0;
}
.review-field-grid pre {
  margin: 0;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  font-family: inherit;
}
.review-actions {
  gap: 10px;
}
.reject-button {
  border-color: #991b1b;
  color: #991b1b;
}
@media (max-width: 760px) {
  .review-field-grid {
    grid-template-columns: 1fr;
  }
}
</style>

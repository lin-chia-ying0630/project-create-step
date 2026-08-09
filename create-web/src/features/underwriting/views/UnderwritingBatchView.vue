<script setup lang="ts">
import { onMounted, ref } from 'vue'
import QueryListPanels from '../../../shared/components/QueryListPanels.vue'
import { underwritingBatchApi } from '../api/underwritingBatchApi'
import type { UnderwritingBatchExecutionSummary } from '../types/underwritingBatch'

const applicationNo = ref('')
const executionDate = ref(
  new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Taipei' }).format(new Date()),
)
const loading = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)
const executions = ref<UnderwritingBatchExecutionSummary[]>([])

/** 將輸入的保單與指定執行日送交覆核，不在畫面直接觸發批次。 */
async function enqueue() {
  loading.value = true
  error.value = null
  message.value = null
  try {
    const result = await underwritingBatchApi.enqueue(
      { applicationNo: applicationNo.value.trim(), executionDate: executionDate.value },
      crypto.randomUUID(),
    )
    message.value = `保單已依 ${executionDate.value} 執行日送交排程覆核，覆核編號：${result.reviewId}`
    applicationNo.value = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '排入批次失敗'
  } finally {
    loading.value = false
  }
}

/** 重新取得最近批次執行彙總，不改變任何排程或保單狀態。 */
async function refresh() {
  try {
    executions.value = await underwritingBatchApi.latestExecutions()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '讀取批次紀錄失敗'
  }
}

onMounted(refresh)
</script>

<template>
  <section class="content-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">UNDERWRITING BATCH</p>
        <h2>新契約批次承保作業</h2>
        <p>固定排程：每日晚上 21:00（Asia/Taipei）啟動，領取執行日為當日且狀態為待執行的保單。</p>
      </div>
      <span class="status-chip">固定排程</span>
    </header>
    <QueryListPanels>
      <template #query>
        <div class="panel-title">
          <h3>排入批次承保作業</h3>
          <small>畫面只能排入案件，不修改正式排程</small>
        </div>
        <div class="scheduled-query-form">
          <label
            >要保書／保單號碼<input v-model="applicationNo" maxlength="32" autocomplete="off"
          /></label>
          <label>執行日<input v-model="executionDate" type="date" /></label>
          <button
            class="primary-button"
            :disabled="loading || !applicationNo.trim() || !executionDate"
            @click="enqueue"
          >
            執行承保作業
          </button>
        </div>
      </template>
      <template #list>
        <div class="panel-title responsive-split-row">
          <h3>最近執行紀錄</h3>
          <button class="secondary-button" @click="refresh">重新整理</button>
        </div>
        <div class="data-table-scope">
          <table class="data-table">
            <thead>
              <tr>
                <th>執行日</th>
                <th>狀態</th>
                <th>總件數</th>
                <th>承保</th>
                <th>照會</th>
                <th>失敗</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in executions" :key="row.batchExecutionId">
                <td>{{ row.businessDate }}</td>
                <td>{{ row.executionStatus }}</td>
                <td>{{ row.totalCount }}</td>
                <td>{{ row.approvedCount }}</td>
                <td>{{ row.inquiryCount }}</td>
                <td>{{ row.failedCount }}</td>
              </tr>
              <tr v-if="executions.length === 0">
                <td colspan="6">尚無批次執行紀錄</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </QueryListPanels>
    <p v-if="message" class="success">{{ message }}</p>
    <p v-if="error" class="error">{{ error }}</p>
  </section>
</template>

<style scoped>
label {
  display: grid;
  gap: 0.35rem;
}
input {
  padding: 0.7rem;
  border-radius: 0.4rem;
}
input {
  border: 1px solid #9aa4b2;
}
.error {
  color: #b42318;
}
.success {
  color: #087443;
}
@media (max-width: 700px) {
  .scheduled-query-form button {
    width: 100%;
    min-height: 44px;
  }
}
</style>

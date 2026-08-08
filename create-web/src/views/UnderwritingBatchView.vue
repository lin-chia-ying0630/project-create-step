<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { underwritingBatchApi } from '../api/underwritingBatchApi'
import type { UnderwritingBatchExecutionSummary } from '../types/underwritingBatch'

const applicationNo = ref('')
const businessDate = ref(new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Taipei' }).format(new Date()))
const loading = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)
const executions = ref<UnderwritingBatchExecutionSummary[]>([])

async function enqueue() {
  loading.value = true; error.value = null; message.value = null
  try {
    const result = await underwritingBatchApi.enqueue({ applicationNo: applicationNo.value.trim(), requestedBusinessDate: businessDate.value }, crypto.randomUUID())
    message.value = `${result.applicationNo} 已排入核保批次，預計 ${result.scheduledAt} 執行。`
    applicationNo.value = ''
  } catch (e) { error.value = e instanceof Error ? e.message : '排入批次失敗' }
  finally { loading.value = false }
}

async function refresh() {
  try { executions.value = await underwritingBatchApi.latestExecutions() }
  catch (e) { error.value = e instanceof Error ? e.message : '讀取批次紀錄失敗' }
}

onMounted(refresh)
</script>

<template>
  <main class="page">
    <h1>新契約批次核保</h1>
    <p class="schedule">固定排程：每日晚上 21:00（Asia/Taipei）。畫面只能排入案件，不能修改正式排程。</p>
    <section class="card form">
      <label>要保書號碼<input v-model="applicationNo" maxlength="32" autocomplete="off"></label>
      <label>核保營業日<input v-model="businessDate" type="date"></label>
      <button :disabled="loading || !applicationNo.trim() || !businessDate" @click="enqueue">排入今晚核保批次</button>
    </section>
    <p v-if="message" class="success">{{ message }}</p><p v-if="error" class="error">{{ error }}</p>
    <section class="card">
      <div class="title"><h2>最近執行紀錄</h2><button @click="refresh">重新整理</button></div>
      <table><thead><tr><th>營業日</th><th>狀態</th><th>總件數</th><th>承保</th><th>照會</th><th>失敗</th></tr></thead>
        <tbody><tr v-for="row in executions" :key="row.batchExecutionId"><td>{{ row.businessDate }}</td><td>{{ row.executionStatus }}</td><td>{{ row.totalCount }}</td><td>{{ row.approvedCount }}</td><td>{{ row.inquiryCount }}</td><td>{{ row.failedCount }}</td></tr>
        <tr v-if="executions.length === 0"><td colspan="6">尚無批次執行紀錄</td></tr></tbody></table>
    </section>
  </main>
</template>

<style scoped>
.page{max-width:64rem;margin:2rem auto;padding:0 1rem;font-family:system-ui,sans-serif;color:#172033}.schedule{border-left:4px solid #174ea6;background:#eef5ff;padding:1rem}.card{margin:1rem 0;padding:1.25rem;border:1px solid #d7dce5;border-radius:.75rem}.form{display:grid;grid-template-columns:2fr 1fr auto;align-items:end;gap:1rem}label{display:grid;gap:.35rem}input,button{padding:.7rem;border-radius:.4rem}input{border:1px solid #9aa4b2}button{border:0;background:#174ea6;color:white}.title{display:flex;justify-content:space-between;align-items:center}.error{color:#b42318}.success{color:#087443}table{width:100%;border-collapse:collapse}th,td{padding:.6rem;text-align:left;border-bottom:1px solid #e5e7eb}@media(max-width:700px){.form{grid-template-columns:1fr}}
</style>

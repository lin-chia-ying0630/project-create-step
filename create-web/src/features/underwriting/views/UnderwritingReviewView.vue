<script setup lang="ts">
import { ref } from 'vue'
import { underwritingReviewApi } from '../api/underwritingReviewApi'
import type { UnderwritingReviewPreview } from '../types/underwritingReview'

const outcomes = [
  { decisionCode: 'DC' as const, label: 'DC 拒絕承保', stage: 'NS 拒保完成', contract: '13 拒保' },
  { decisionCode: 'PO' as const, label: 'PO 延期承保', stage: 'DS 延期完成', contract: '14 延期' },
  { decisionCode: 'CN' as const, label: 'CN 取消申請', stage: 'CS 取消完成', contract: '15 取消' },
]
const query = ref(''), preview = ref<UnderwritingReviewPreview | null>(null)
const decisionCode = ref<'DC' | 'PO' | 'CN'>('DC'), reasonCode = ref(''), reasonDescription = ref('')
const loading = ref(false), message = ref<string | null>(null), error = ref<string | null>(null)

/** 依要保書或正式保單號碼讀取目前核保狀態及樂觀鎖版本。 */
async function search() {
  loading.value = true
  error.value = null
  try {
    preview.value = await underwritingReviewApi.find(query.value.trim())
  } catch (e) {
    preview.value = null
    error.value = e instanceof Error ? e.message : '查詢失敗'
  } finally {
    loading.value = false
  }
}

/** 將核保結果修改送交 Maker-Checker 覆核，核准前不修改案件。 */
async function submit() {
  if (!preview.value) return
  loading.value = true
  error.value = null
  message.value = null
  try {
    const result = await underwritingReviewApi.submit({
      applicationNo: preview.value.applicationNo,
      decisionCode: decisionCode.value,
      reasonCode: reasonCode.value.trim(),
      reasonDescription: reasonDescription.value.trim(),
      expectedVersion: preview.value.recordVersion,
    })
    message.value = `核保結果修改已送覆核，覆核編號：${result.reviewId}`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '送覆核失敗'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="content-page">
    <header class="page-header"><div><p class="eyebrow">UNDERWRITING REVIEW</p><h2>核保審查作業</h2>
      <p>修改核保結果一律送覆核；核准後才同步核保階段碼與契約狀態。</p></div></header>
    <section class="panel">
      <div class="search-row"><label>要保書號碼／正式保單號碼＊<input v-model.trim="query" maxlength="32" @keyup.enter="search" /></label>
        <button class="primary-button" :disabled="loading || !query" @click="search">查詢案件</button></div>
    </section>
    <section v-if="preview" class="panel section-gap">
      <dl class="case-grid"><div><dt>要保書號碼</dt><dd>{{ preview.applicationNo }}</dd></div>
        <div><dt>正式保單號碼</dt><dd>{{ preview.policyNo || '—' }}</dd></div>
        <div><dt>目前核保階段</dt><dd>{{ preview.currentStageCode }}</dd></div>
        <div><dt>目前核保結果</dt><dd>{{ preview.currentDecisionCode || '—' }}</dd></div>
        <div><dt>目前契約狀態</dt><dd>{{ preview.currentContractStatusCode || '—' }}</dd></div></dl>
      <div class="form-grid section-gap"><label>核保結果＊<select v-model="decisionCode">
        <option v-for="item in outcomes" :key="item.decisionCode" :value="item.decisionCode">
          {{ item.label }}｜{{ item.stage }}｜{{ item.contract }}
        </option>
      </select></label><label>原因代碼＊<input v-model.trim="reasonCode" maxlength="32" /></label>
        <label class="full-width">原因說明＊<textarea v-model.trim="reasonDescription" maxlength="500" rows="4" /></label></div>
      <button class="primary-button" :disabled="loading || !reasonCode || reasonDescription.length < 5" @click="submit">送交覆核</button>
    </section>
    <p v-if="message" class="success">{{ message }}</p><p v-if="error" class="error">{{ error }}</p>
  </main>
</template>

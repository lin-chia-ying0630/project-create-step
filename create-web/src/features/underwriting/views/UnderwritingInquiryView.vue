<script setup lang="ts">
import { ref } from 'vue'
import { underwritingInquiryApi } from '../api/underwritingInquiryApi'
import type { InquiryDetail } from '../types/underwritingInquiry'
const query = ref('DEMO-INQ-001'),
  detail = ref<InquiryDetail | null>(null),
  loading = ref(false),
  pdfLoading = ref(false),
  error = ref<string | null>(null)
const formatTime = (v: string | null) =>
  v
    ? new Intl.DateTimeFormat('zh-TW', {
        dateStyle: 'medium',
        timeStyle: 'short',
        timeZone: 'Asia/Taipei',
      }).format(new Date(v))
    : '—'
const money = (currency: string, value: string) =>
  `${currency} ${Number(value).toLocaleString('zh-TW')}`
async function search() {
  loading.value = true
  error.value = null
  detail.value = null
  try {
    detail.value = await underwritingInquiryApi.find(query.value.trim())
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查詢失敗'
  } finally {
    loading.value = false
  }
}
async function downloadPdf() {
  if (!detail.value) return
  pdfLoading.value = true
  error.value = null
  try {
    const doc = await underwritingInquiryApi.pdf(detail.value.inquiryNo)
    const bytes = Uint8Array.from(atob(doc.base64Content), (c) => c.charCodeAt(0))
    const url = URL.createObjectURL(new Blob([bytes], { type: doc.contentType }))
    const link = document.createElement('a')
    link.href = url
    link.download = doc.fileName
    link.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'PDF 下載失敗'
  } finally {
    pdfLoading.value = false
  }
}
</script>
<template>
  <section class="content-page inquiry-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">UNDERWRITING INQUIRY</p>
        <h2>核保照會單查詢</h2>
        <p>查詢未通過的核保檢核項目、補件原因及照會處理狀態。</p>
      </div>
      <span class="status-chip">核保結果</span>
    </header>
    <article class="panel">
      <div class="panel-title">
        <h3>照會案件查詢</h3>
        <small>照會單號、要保書號碼或預編保單號碼擇一輸入</small>
      </div>
      <div class="search-row">
        <label
          >查詢條件＊<input
            v-model.trim="query"
            maxlength="32"
            placeholder="例：DEMO-INQ-001"
            @keyup.enter="search" /></label
        ><button class="primary-button" :disabled="!query.trim() || loading" @click="search">
          {{ loading ? '查詢中…' : '查詢照會單' }}
        </button>
      </div>
    </article>
    <template v-if="detail"
      ><article class="panel section-gap">
        <div class="result-heading responsive-split-row">
          <div>
            <p class="eyebrow">UNDERWRITING RESULT</p>
            <h3>{{ detail.underwritingStatusDescription }}</h3>
            <p>{{ detail.decisionDescription }}</p>
          </div>
          <div class="result-actions responsive-split-row">
            <span class="inquiry-status">{{ detail.inquiryStatusDescription }}</span
            ><button class="primary-button" :disabled="pdfLoading" @click="downloadPdf">
              {{ pdfLoading ? '產生中…' : '下載核保照會單 PDF' }}
            </button>
          </div>
        </div>
        <dl class="case-grid">
          <div>
            <dt>照會單號</dt>
            <dd>{{ detail.inquiryNo }}</dd>
          </div>
          <div>
            <dt>保單號碼</dt>
            <dd>{{ detail.policyNo || '尚未編發' }}</dd>
          </div>
          <div>
            <dt>新契約階段</dt>
            <dd>{{ detail.newContractStageDescription }}</dd>
          </div>
          <div>
            <dt>契約狀態</dt>
            <dd>{{ detail.contractStatusDescription }}</dd>
          </div>
          <div>
            <dt>要保書號碼</dt>
            <dd>{{ detail.applicationNo }}</dd>
          </div>
          <div>
            <dt>核保案件號</dt>
            <dd>{{ detail.underwritingCaseNo }}</dd>
          </div>
          <div>
            <dt>照會日期</dt>
            <dd>{{ formatTime(detail.issuedAt) }}</dd>
          </div>
        </dl>
      </article>
      <article class="panel section-gap">
        <div class="panel-title">
          <h3>要保人、被保險人與投保資料</h3>
          <small>姓名與客戶識別資料已遮蔽</small>
        </div>
        <dl class="case-grid party-grid">
          <div>
            <dt>要保人</dt>
            <dd>{{ detail.applicantNameMasked }}</dd>
            <small>{{ detail.applicantCustomerReference }}</small>
          </div>
          <div>
            <dt>被保險人</dt>
            <dd>{{ detail.insuredNameMasked }}</dd>
            <small>{{ detail.insuredCustomerReference }}</small>
          </div>
          <div>
            <dt>商品代碼</dt>
            <dd>{{ detail.productCode }}</dd>
          </div>
          <div>
            <dt>要保日期</dt>
            <dd>{{ detail.applicationDate }}</dd>
          </div>
          <div>
            <dt>預定生效日</dt>
            <dd>{{ detail.requestedEffectiveDate }}</dd>
          </div>
          <div>
            <dt>保險金額</dt>
            <dd>{{ money(detail.currencyCode, detail.sumAssuredAmount) }}</dd>
          </div>
          <div>
            <dt>首期保險費</dt>
            <dd>{{ money(detail.currencyCode, detail.premiumAmount) }}</dd>
          </div>
        </dl>
      </article>
      <article class="panel section-gap">
        <div class="panel-title">
          <h3>未通過檢核項目</h3>
          <strong class="issue-count">{{ detail.items.length }} 項</strong>
        </div>
        <div class="issue-list">
          <section v-for="(item, index) in detail.items" :key="item.ruleCode" class="issue-card">
            <span class="issue-number">{{ index + 1 }}</span>
            <div>
              <small>{{ item.ruleCode }}</small>
              <h4>{{ item.ruleName }}</h4>
              <p>{{ item.itemMessage }}</p>
              <div v-if="item.responseText" class="response-box">
                <b>回覆內容</b>{{ item.responseText }}
              </div>
              <span v-else class="pending-response">尚待補件回覆</span>
            </div>
          </section>
        </div>
      </article></template
    >
    <div v-else-if="!error" class="empty-state">
      <strong>尚未查詢照會單</strong>
      <p>測試可輸入 `DEMO-INQ-001` 或 `DEMO-NC-INQ-001`。</p>
    </div>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>
<style scoped>
.inquiry-page {
  max-width: 960px;
}
.result-heading {
  border-left: 5px solid #d97706;
  background: #fff7ed;
  padding: 18px;
}
.result-heading h3 {
  margin: 2px 0;
  color: #9a3412;
  font-size: 1.45rem;
}
.result-heading p {
  margin: 4px 0;
}
.result-actions {
  align-items: center;
  gap: 10px;
}
.inquiry-status {
  border: 1px solid #fdba74;
  border-radius: 999px;
  background: #fff;
  color: #9a3412;
  padding: 7px 11px;
}
.case-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1px;
  margin: 18px 0 0;
  background: #dbe4ea;
}
.case-grid div {
  background: #f8fafb;
  padding: 13px;
}
.case-grid dt {
  color: #647281;
  font-size: 0.8rem;
}
.case-grid dd {
  margin: 5px 0 0;
  font-weight: 700;
}
.issue-count {
  color: #b42318;
}
.issue-list {
  display: grid;
  gap: 12px;
}
.issue-card {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 14px;
  border: 1px solid #fed7aa;
  border-radius: 7px;
  background: #fffbeb;
  padding: 15px;
}
.issue-number {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #d97706;
  color: #fff;
  font-weight: 700;
}
.issue-card small {
  color: #647281;
}
.issue-card h4 {
  margin: 3px 0;
}
.issue-card p {
  margin: 7px 0;
}
.pending-response {
  display: inline-block;
  color: #9a3412;
  font-size: 0.85rem;
}
.response-box {
  display: grid;
  gap: 4px;
  border-left: 3px solid #0f766e;
  background: #ecfdf5;
  padding: 9px;
}
.empty-state {
  text-align: center;
  color: #647281;
  padding: 55px;
}
.empty-state strong {
  color: #334155;
}
@media (max-width: 760px) {
  .case-grid {
    grid-template-columns: 1fr;
  }
  .result-actions,
  .result-actions button {
    width: 100%;
  }
  .issue-card {
    grid-template-columns: 1fr;
  }
  .empty-state {
    padding: 32px 12px;
  }
}
</style>

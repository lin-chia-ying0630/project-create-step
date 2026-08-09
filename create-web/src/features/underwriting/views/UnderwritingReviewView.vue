<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { underwritingReviewApi } from '../api/underwritingReviewApi'
import type {
  UnderwritingOutcomeOption,
  UnderwritingReviewPreview,
} from '../types/underwritingReview'

const query = ref('')
const preview = ref<UnderwritingReviewPreview | null>(null)
const outcomes = ref<UnderwritingOutcomeOption[]>([])
const decisionCode = ref('')
const reasonCode = ref('')
const reasonDescription = ref('')
const loading = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)
const selectedOutcome = computed(() =>
  outcomes.value.find((outcome) => outcome.decisionCode === decisionCode.value),
)

/** 顯示後端提供的完整核保結果名稱與後續階段，前端不自行判斷狀態。 */
function outcomeLabel(outcome: UnderwritingOutcomeOption): string {
  const decision = `${outcome.decisionCode} ${outcome.decisionDescription}`
  const stage = `${outcome.stageCode} ${outcome.stageDescription}`
  const contract = `${outcome.contractStatusCode} ${outcome.contractStatusDescription}`
  return `${decision}｜${stage}｜${contract}`
}

/** 以新臺幣或外幣格式顯示保險金額及保費。 */
function money(currencyCode: string, value: string): string {
  return `${currencyCode} ${Number(value).toLocaleString('zh-TW')}`
}

/** 依要保書或正式保單號碼讀取目前核保狀態及樂觀鎖版本。 */
async function search() {
  loading.value = true
  error.value = null
  message.value = null
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
  if (!preview.value || !selectedOutcome.value) return
  loading.value = true
  error.value = null
  message.value = null
  try {
    const result = await underwritingReviewApi.submit({
      applicationNo: preview.value.applicationNo,
      decisionCode: selectedOutcome.value.decisionCode,
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

/** 進入畫面時取得後端正式核保結果清單，避免前端維護第二份代碼。 */
onMounted(async () => {
  try {
    outcomes.value = await underwritingReviewApi.outcomes()
    decisionCode.value = outcomes.value[0]?.decisionCode ?? ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '讀取核保結果失敗'
  }
})
</script>

<template>
  <section class="content-page underwriting-review-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">UNDERWRITING REVIEW</p>
        <h2>核保審查作業</h2>
        <p>檢視投保案件並決定承保條件；所有核保結果修改均須送交覆核。</p>
      </div>
      <span class="status-chip">核保決定</span>
    </header>

    <article class="panel">
      <div class="panel-title">
        <h3>核保案件查詢</h3>
        <small>要保書號碼或正式保單號碼擇一輸入</small>
      </div>
      <div class="search-row">
        <label>
          查詢條件＊
          <input v-model.trim="query" maxlength="32" placeholder="請輸入要保書或正式保單號碼" @keyup.enter="search" />
        </label>
        <button class="primary-button" :disabled="loading || !query" @click="search">
          {{ loading ? '查詢中…' : '查詢案件' }}
        </button>
      </div>
    </article>

    <template v-if="preview">
      <article class="panel section-gap">
        <div class="panel-title">
          <h3>案件與投保摘要</h3>
          <small>核保案件 {{ preview.underwritingCaseNo }}</small>
        </div>
        <dl class="case-grid">
          <div><dt>要保書號碼</dt><dd>{{ preview.applicationNo }}</dd></div>
          <div><dt>正式保單號碼</dt><dd>{{ preview.policyNo || '—' }}</dd></div>
          <div><dt>商品代碼</dt><dd>{{ preview.productCode }}</dd></div>
          <div><dt>要保日期</dt><dd>{{ preview.applicationDate }}</dd></div>
          <div><dt>預定生效日</dt><dd>{{ preview.requestedEffectiveDate }}</dd></div>
          <div><dt>保險金額</dt><dd>{{ money(preview.currencyCode, preview.sumAssuredAmount) }}</dd></div>
          <div><dt>首期保險費</dt><dd>{{ money(preview.currencyCode, preview.premiumAmount) }}</dd></div>
          <div>
            <dt>目前核保階段</dt>
            <dd>{{ preview.currentStageCode }} {{ preview.currentStageDescription }}</dd>
          </div>
          <div>
            <dt>目前核保結果</dt>
            <dd>{{ preview.currentDecisionCode || '尚未決定' }}</dd>
          </div>
          <div>
            <dt>目前契約狀態</dt>
            <dd>{{ preview.currentContractStatusCode || 'NULL' }} {{ preview.currentContractStatusDescription }}</dd>
          </div>
        </dl>
      </article>

      <article class="panel section-gap">
        <div class="panel-title">
          <h3>核保決定</h3>
          <small>承保類結果會繼續後續流程；延期、拒保及取消不會進入承保流程</small>
        </div>
        <div class="field-grid">
          <label class="wide-field">
            核保結果＊
            <select v-model="decisionCode">
              <optgroup label="可承保">
                <option
                  v-for="outcome in outcomes.filter((item) => item.insurable)"
                  :key="outcome.decisionCode"
                  :value="outcome.decisionCode"
                >
                  {{ outcomeLabel(outcome) }}
                </option>
              </optgroup>
              <optgroup label="不承保／暫緩">
                <option
                  v-for="outcome in outcomes.filter((item) => !item.insurable)"
                  :key="outcome.decisionCode"
                  :value="outcome.decisionCode"
                >
                  {{ outcomeLabel(outcome) }}
                </option>
              </optgroup>
            </select>
          </label>
          <div v-if="selectedOutcome" class="decision-preview wide-field" :class="{ insurable: selectedOutcome.insurable }">
            <strong>{{ selectedOutcome.insurable ? '往下承保' : '不進入承保流程' }}</strong>
            <span>核保階段：{{ selectedOutcome.stageCode }} {{ selectedOutcome.stageDescription }}</span>
            <span>契約狀態：{{ selectedOutcome.contractStatusCode }} {{ selectedOutcome.contractStatusDescription }}</span>
          </div>
          <label>
            原因代碼＊
            <input v-model.trim="reasonCode" maxlength="32" placeholder="例：STANDARD、MEDICAL_RISK" />
          </label>
          <label class="wide-field">
            核保說明＊
            <textarea
              v-model.trim="reasonDescription"
              maxlength="500"
              rows="4"
              placeholder="請說明核保判斷與承保條件；不得輸入健康告知原文"
            />
          </label>
        </div>
        <div class="form-actions">
          <button
            class="primary-button"
            :disabled="loading || !decisionCode || !reasonCode || reasonDescription.length < 5"
            @click="submit"
          >
            {{ loading ? '處理中…' : '送交覆核' }}
          </button>
        </div>
      </article>
    </template>

    <p v-if="message" class="status-message success section-gap">{{ message }}</p>
    <p v-if="error" class="status-message error section-gap">{{ error }}</p>
  </section>
</template>

<style scoped lang="scss">
.decision-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  border: 1px solid #fecaca;
  border-radius: 6px;
  background: #fff7f7;
  padding: 12px;
  color: #991b1b;
}

.decision-preview.insurable {
  border-color: #a7f3d0;
  background: #f0fdf4;
  color: #065f46;
}

.case-grid dd {
  overflow-wrap: anywhere;
}
</style>

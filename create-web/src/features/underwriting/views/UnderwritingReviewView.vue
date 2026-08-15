<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import QueryListPanels from '../../../shared/components/QueryListPanels.vue'
import SingleQueryForm from '../../../shared/components/SingleQueryForm.vue'
import SortableTableHeader from '../../../shared/components/SortableTableHeader.vue'
import { underwritingReviewApi } from '../api/underwritingReviewApi'
import type {
  UnderwritingOutcomeOption,
  UnderwritingReviewPage,
  UnderwritingReviewPreview,
  UnderwritingReviewSummary,
} from '../types/underwritingReview'

const reviewPage = ref<UnderwritingReviewPage>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const preview = ref<UnderwritingReviewPreview | null>(null)
const outcomes = ref<UnderwritingOutcomeOption[]>([])
const decisionCode = ref('')
const reasonCode = ref('')
const reasonDescription = ref('')
const listLoading = ref(false)
const detailLoading = ref(false)
const submitting = ref(false)
const message = ref<string | null>(null)
const error = ref<string | null>(null)
const reviewDialog = ref<HTMLDialogElement | null>(null)
const sortField = ref('applicationNo')
const sortDirection = ref<'asc' | 'desc'>('asc')
const queryInput = ref('')
const appliedQuery = ref('')
const selectedOutcome = computed(() =>
  outcomes.value.find((outcome) => outcome.decisionCode === decisionCode.value),
)

/** 顯示後端提供的完整核保結果名稱與後續階段，前端不自行判斷狀態。 */
function outcomeLabel(outcome: UnderwritingOutcomeOption): string {
  const decision = `${outcome.decisionCode} ${outcome.decisionDescription}`
  const stage = `${outcome.newContractStageCode} ${outcome.newContractStageDescriptionZhTw}`
  const contract = `${outcome.contractStatusCode} ${outcome.contractStatusDescription}`
  return `${decision}｜${stage}｜${contract}`
}

/** 以新臺幣或外幣格式顯示保險金額及保費。 */
function money(currencyCode: string, value: string): string {
  return `${currencyCode} ${Number(value).toLocaleString('zh-TW')}`
}

/** 分頁讀取新契約受理檔中 NS 照會結束、等待核保審查的案件。 */
async function loadCases(page = 1) {
  listLoading.value = true
  error.value = null
  try {
    reviewPage.value = await underwritingReviewApi.list(
      appliedQuery.value,
      page,
      reviewPage.value.pageSize,
      `${sortField.value},${sortDirection.value}`,
    )
  } catch (e) {
    error.value = e instanceof Error ? e.message : '讀取待核保審查清單失敗'
  } finally {
    listLoading.value = false
  }
}

/** 套用要保書、正式保單或核保案件完整號碼，從第一頁查詢 NS 待審案件。 */
function searchCases() {
  appliedQuery.value = queryInput.value.trim()
  void loadCases(1)
}

/** 清除核保審查查詢條件並回復全部 NS 待審案件。 */
function clearSearch() {
  queryInput.value = ''
  appliedQuery.value = ''
  void loadCases(1)
}

/** 由共用排序表頭切換欄位與方向，並從第一頁重新向後端查詢。 */
function changeSort(field: string, direction: 'asc' | 'desc') {
  sortField.value = field
  sortDirection.value = direction
  void loadCases(1)
}

/** 變更共用每頁筆數後回到第一頁重新取得待核保審查案件。 */
function changePageSize(pageSize: number) {
  reviewPage.value.pageSize = pageSize
  void loadCases(1)
}

/** 點選清單案件後讀取最新版本，再開啟核保審查彈跳視窗。 */
async function openCase(item: UnderwritingReviewSummary) {
  detailLoading.value = true
  error.value = null
  message.value = null
  try {
    preview.value = await underwritingReviewApi.find(item.applicationNo)
    reasonCode.value = ''
    reasonDescription.value = ''
    decisionCode.value = outcomes.value[0]?.decisionCode ?? ''
    reviewDialog.value?.showModal()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '讀取核保案件失敗'
    await loadCases(reviewPage.value.page)
  } finally {
    detailLoading.value = false
  }
}

/** 關閉核保審查視窗並清除案件資料，避免下次短暫顯示前一案件。 */
function closeDialog() {
  reviewDialog.value?.close()
  preview.value = null
}

/** 將核保結果修改送交 Maker-Checker 覆核，核准前不修改案件。 */
async function submit() {
  if (!preview.value || !selectedOutcome.value) return
  submitting.value = true
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
    closeDialog()
    await loadCases(reviewPage.value.page)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '送覆核失敗'
  } finally {
    submitting.value = false
  }
}

/** 進入畫面時同時取得待辦清單及後端正式核保結果。 */
onMounted(async () => {
  try {
    outcomes.value = await underwritingReviewApi.outcomes()
    decisionCode.value = outcomes.value[0]?.decisionCode ?? ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '讀取核保結果失敗'
  }
  await loadCases()
})
</script>

<template>
  <section class="content-page underwriting-review-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">UNDERWRITING REVIEW</p>
        <h2>核保審查作業</h2>
        <p>清單僅顯示新契約受理檔中 NS 照會結束、需要核保審查的案件。</p>
      </div>
      <span class="status-chip">待審查 {{ reviewPage.totalItems }} 件</span>
    </header>

    <QueryListPanels>
      <template #query>
        <SingleQueryForm
          v-model="queryInput"
          button-label="查詢核保案件"
          description="可輸入完整要保書號碼、正式保單號碼或核保案件號碼；留白查詢全部 NS 待審案件"
          field-label="要保書／保單號碼"
          :loading="listLoading"
          :max-length="32"
          @submit="searchCases"
          @clear="clearSearch"
        />
      </template>
      <template #list>
        <div class="panel-title responsive-split-row">
          <div>
            <h3>待核保審查清單</h3>
            <small>點選案件後，以彈跳視窗檢視投保資料並送交核保決定覆核</small>
          </div>
          <button
            class="secondary-button"
            :disabled="listLoading"
            @click="loadCases(reviewPage.page)"
          >
            {{ listLoading ? '讀取中…' : '重新整理' }}
          </button>
        </div>
        <div class="candidate-table-scope">
          <table class="data-table candidate-table">
            <thead>
              <tr>
                <th scope="col">操作</th>
                <SortableTableHeader
                  field="applicationNo"
                  label="要保書號碼"
                  :active-field="sortField"
                  :direction="sortDirection"
                  @sort="changeSort"
                />
                <SortableTableHeader
                  field="policyNo"
                  label="正式保單號碼"
                  :active-field="sortField"
                  :direction="sortDirection"
                  @sort="changeSort"
                />
                <SortableTableHeader
                  field="productCode"
                  label="商品代碼"
                  :active-field="sortField"
                  :direction="sortDirection"
                  @sort="changeSort"
                />
                <th scope="col">要保日期</th>
                <th scope="col">預定生效日</th>
                <th scope="col">核保階段</th>
                <th scope="col">新增人員</th>
                <th scope="col">建立時間</th>
                <th scope="col">修改人員</th>
                <th scope="col">修改時間</th>
                <th scope="col">覆核人員</th>
                <th scope="col">覆核時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in reviewPage.items" :key="item.underwritingCaseNo">
                <td>
                  <button
                    class="secondary-button"
                    :disabled="detailLoading"
                    @click="openCase(item)"
                  >
                    進入審查
                  </button>
                </td>
                <td>{{ item.applicationNo }}</td>
                <td>{{ item.policyNo || '—' }}</td>
                <td>{{ item.productCode }}</td>
                <td>{{ item.applicationDate }}</td>
                <td>{{ item.requestedEffectiveDate }}</td>
                <td>
                  {{ item.newContractStageCode }}｜{{ item.newContractStageDescriptionZhTw }}｜{{
                    item.newContractStageNameEn
                  }}
                </td>
                <td>{{ item.createdBy }}</td>
                <td>{{ item.createdAt }}</td>
                <td>{{ item.updatedBy }}</td>
                <td>{{ item.updatedAt }}</td>
                <td>{{ item.reviewerId || '尚未覆核' }}</td>
                <td>{{ item.reviewedAt || '尚未覆核' }}</td>
              </tr>
              <tr v-if="!listLoading && reviewPage.items.length === 0">
                <td colspan="13">目前沒有 NS 照會結束、需要核保審查的案件。</td>
              </tr>
            </tbody>
          </table>
        </div>
        <PageNavigator
          v-if="reviewPage.totalPages > 1"
          :model-value="reviewPage.page - 1"
          :total="reviewPage.totalPages"
          :page-size="reviewPage.pageSize"
          prefix="待核保審查清單"
          @update:model-value="loadCases($event + 1)"
          @update:page-size="changePageSize"
        />
      </template>
    </QueryListPanels>

    <p v-if="message" class="status-message success section-gap">{{ message }}</p>
    <p v-if="error" class="status-message error section-gap">{{ error }}</p>

    <dialog
      ref="reviewDialog"
      class="review-dialog"
      aria-labelledby="review-dialog-title"
      @cancel="closeDialog"
    >
      <div v-if="preview" class="dialog-content">
        <header class="dialog-header">
          <div>
            <p class="eyebrow">UNDERWRITING CASE</p>
            <h3 id="review-dialog-title">核保案件 {{ preview.underwritingCaseNo }}</h3>
          </div>
          <button
            type="button"
            class="dialog-close"
            aria-label="關閉核保審查視窗"
            @click="closeDialog"
          >
            ×
          </button>
        </header>

        <section>
          <h4>案件與投保摘要</h4>
          <div class="summary-table-scope">
            <table class="data-table summary-table">
              <thead>
                <tr>
                  <th scope="col">欄位</th>
                  <th scope="col">內容</th>
                  <th scope="col">欄位</th>
                  <th scope="col">內容</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">要保書號碼</th>
                  <td>{{ preview.applicationNo }}</td>
                  <th scope="row">正式保單號碼</th>
                  <td>{{ preview.policyNo || '—' }}</td>
                </tr>
                <tr>
                  <th scope="row">商品代碼</th>
                  <td>{{ preview.productCode }}</td>
                  <th scope="row">要保日期</th>
                  <td>{{ preview.applicationDate }}</td>
                </tr>
                <tr>
                  <th scope="row">預定生效日</th>
                  <td>{{ preview.requestedEffectiveDate }}</td>
                  <th scope="row">保險金額</th>
                  <td>{{ money(preview.currencyCode, preview.sumAssuredAmount) }}</td>
                </tr>
                <tr>
                  <th scope="row">首期保險費</th>
                  <td>{{ money(preview.currencyCode, preview.premiumAmount) }}</td>
                  <th scope="row">目前核保階段</th>
                  <td>
                    {{ preview.newContractStageCode }}｜{{
                      preview.newContractStageDescriptionZhTw
                    }}｜{{ preview.newContractStageNameEn }}
                  </td>
                </tr>
                <tr>
                  <th scope="row">目前核保結果</th>
                  <td>{{ preview.currentDecisionCode || '尚未決定' }}</td>
                  <th scope="row">目前契約狀態</th>
                  <td>
                    {{ preview.currentContractStatusCode || 'NULL' }}
                    {{ preview.currentContractStatusDescription }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section class="audit-section">
          <h4>資料責任與覆核資訊</h4>
          <div class="summary-table-scope">
            <table class="data-table audit-table">
              <thead>
                <tr>
                  <th scope="col">新增人員</th>
                  <th scope="col">建立時間</th>
                  <th scope="col">修改人員</th>
                  <th scope="col">修改時間</th>
                  <th scope="col">覆核人員</th>
                  <th scope="col">覆核時間</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{{ preview.createdBy }}</td>
                  <td>{{ preview.createdAt }}</td>
                  <td>{{ preview.updatedBy }}</td>
                  <td>{{ preview.updatedAt }}</td>
                  <td>{{ preview.reviewerId || '尚未覆核' }}</td>
                  <td>{{ preview.reviewedAt || '尚未覆核' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section class="decision-section">
          <h4>核保決定</h4>
          <div class="field-grid">
            <label class="wide-field"
              >核保結果＊
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
            <div
              v-if="selectedOutcome"
              class="decision-preview wide-field"
              :class="{ insurable: selectedOutcome.insurable }"
            >
              <strong>{{ selectedOutcome.insurable ? '往下承保' : '不進入承保流程' }}</strong>
              <span
                >新契約階段：{{ selectedOutcome.newContractStageCode }}｜{{
                  selectedOutcome.newContractStageDescriptionZhTw
                }}｜{{ selectedOutcome.newContractStageNameEn }}</span
              >
              <span
                >契約狀態：{{ selectedOutcome.contractStatusCode }}
                {{ selectedOutcome.contractStatusDescription }}</span
              >
            </div>
            <label
              >原因代碼＊
              <input
                v-model.trim="reasonCode"
                maxlength="32"
                placeholder="例：STANDARD、MEDICAL_RISK"
              />
            </label>
            <label class="wide-field"
              >核保說明＊
              <textarea
                v-model.trim="reasonDescription"
                maxlength="500"
                rows="4"
                placeholder="請說明核保判斷與承保條件；不得輸入健康告知原文"
              />
            </label>
          </div>
        </section>
        <div class="form-actions">
          <button
            type="button"
            class="secondary-button"
            :disabled="submitting"
            @click="closeDialog"
          >
            取消
          </button>
          <button
            class="primary-button"
            :disabled="submitting || !decisionCode || !reasonCode || reasonDescription.length < 5"
            @click="submit"
          >
            {{ submitting ? '處理中…' : '送交覆核' }}
          </button>
        </div>
      </div>
    </dialog>
  </section>
</template>

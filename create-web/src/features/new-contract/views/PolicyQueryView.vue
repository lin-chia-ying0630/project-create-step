<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import QueryListPanels from '../../../shared/components/QueryListPanels.vue'
import SingleQueryForm from '../../../shared/components/SingleQueryForm.vue'
import SortableTableHeader from '../../../shared/components/SortableTableHeader.vue'
import { applicationEntryApi } from '../api/applicationEntryApi'
import type { ApplicationQueryPage, ApplicationQueryResult } from '../types/applicationEntry'

const query = ref(''),
  results = ref<ApplicationQueryResult[]>([]),
  loading = ref(false),
  error = ref<string | null>(null),
  activePage = ref(0)
const detailDialog = ref<HTMLDialogElement | null>(null)
const queryPage = ref<ApplicationQueryPage>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const appliedQuery = ref('')
const sortField = ref('applicationNo')
const sortDirection = ref<'asc' | 'desc'>('asc')
const pages = [
  '要保書主檔',
  '契約關係人',
  '客戶聯絡檔',
  '客戶地址檔',
  '保障內容檔',
  '受益人檔',
  '健康告知檔',
  '聲明同意檔',
  '簽署檔',
  '首期保費應收檔',
]
async function search() {
  appliedQuery.value = query.value.trim()
  await loadPolicies(1)
}

/** 清除單一查詢值並回復完整保單清單。 */
function clearSearch() {
  query.value = ''
  appliedQuery.value = ''
  void loadPolicies(1)
}

/** 從後端載入保單清單，頁碼、筆數與排序均不在前端二次處理。 */
async function loadPolicies(page = queryPage.value.page) {
  loading.value = true
  error.value = null
  results.value = []
  try {
    queryPage.value = await applicationEntryApi.queryPage(
      appliedQuery.value,
      page,
      queryPage.value.pageSize,
      `${sortField.value},${sortDirection.value}`,
    )
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查詢失敗'
  } finally {
    loading.value = false
  }
}

/** 點選清單第一欄操作後才載入保單完整檔案內容。 */
async function openDetail(applicationNo: string) {
  loading.value = true
  error.value = null
  activePage.value = 0
  try {
    results.value = await applicationEntryApi.query(applicationNo)
    await nextTick()
    detailDialog.value?.showModal()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保單明細查詢失敗'
  } finally {
    loading.value = false
  }
}

/** 關閉保單檔案明細視窗並清除前次查詢內容。 */
function closeDetail() {
  detailDialog.value?.close()
  results.value = []
}

/** 切換前三個資料欄位排序後回到第一頁。 */
function changeSort(field: string, direction: 'asc' | 'desc') {
  sortField.value = field
  sortDirection.value = direction
  void loadPolicies(1)
}

/** 變更共用每頁筆數後回到第一頁。 */
function changePageSize(pageSize: number) {
  queryPage.value.pageSize = pageSize
  void loadPolicies(1)
}
const money = (value: string | null) =>
  value == null ? '—' : Number(value).toLocaleString('zh-TW')
const display = (value: unknown) =>
  value === null || value === undefined || value === '' ? '—' : String(value)

onMounted(() => loadPolicies(1))
</script>

<template>
  <section class="content-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">POLICY LOOKUP</p>
        <h2>保單資料查詢</h2>
        <p>可使用要保人 ID、被保險人 ID、要保書號碼或正式保單號碼查詢。</p>
      </div>
    </header>
    <QueryListPanels>
      <template #query>
        <SingleQueryForm
          v-model="query"
          button-label="查詢保單"
          description="可輸入完整要保人 ID、被保險人 ID、要保書號碼或正式保單號碼；留白查詢全部資料"
          field-label="要保書／保單號碼"
          :loading="loading"
          :max-length="36"
          @submit="search"
          @clear="clearSearch"
        />
      </template>

      <template #list>
        <div class="panel-title responsive-split-row">
          <h3>保單資料清單</h3>
          <span>共 {{ queryPage.totalItems }} 筆</span>
        </div>
        <div class="data-table-scope">
          <table class="data-table">
            <thead>
              <tr>
                <th>操作</th>
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
                <th>要保日期</th>
                <th>預定生效日</th>
                <th>狀態</th>
                <th>新增人員</th>
                <th>建立時間</th>
                <th>修改人員</th>
                <th>修改時間</th>
                <th>覆核人員</th>
                <th>覆核時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in queryPage.items" :key="item.applicationNo">
                <td>
                  <button class="secondary-button" @click="openDetail(item.applicationNo)">
                    查詢保單
                  </button>
                </td>
                <td>{{ item.applicationNo }}</td>
                <td>{{ item.policyNo || '—' }}</td>
                <td>{{ item.productCode }}</td>
                <td>{{ item.applicationDate }}</td>
                <td>{{ item.requestedEffectiveDate }}</td>
                <td>{{ item.applicationStatus }} {{ item.applicationStatusDescription }}</td>
                <td>{{ item.createdBy }}</td>
                <td>{{ item.createdAt }}</td>
                <td>{{ item.updatedBy }}</td>
                <td>{{ item.updatedAt }}</td>
                <td>{{ item.reviewerId || '尚未覆核' }}</td>
                <td>{{ item.reviewedAt || '尚未覆核' }}</td>
              </tr>
              <tr v-if="!loading && queryPage.items.length === 0">
                <td colspan="13">查無保單資料。</td>
              </tr>
            </tbody>
          </table>
        </div>
        <PageNavigator
          v-if="queryPage.totalPages > 0"
          :model-value="queryPage.page - 1"
          :total="queryPage.totalPages"
          :page-size="queryPage.pageSize"
          prefix="保單資料清單"
          @update:model-value="loadPolicies($event + 1)"
          @update:page-size="changePageSize"
        />
      </template>
    </QueryListPanels>

    <dialog ref="detailDialog" class="review-dialog" @cancel="closeDetail">
      <article
        v-for="detail in results"
        :key="detail.applicationNo"
        class="dialog-content data-table-scope"
      >
        <div class="result-heading responsive-split-row">
          <div>
            <p class="eyebrow">{{ detail.newContractStageDescription }}</p>
            <h3>{{ detail.applicationStatusDescription }}</h3>
            <p>
              要保書 {{ detail.applicationNo }}／正式保單號碼 {{ detail.policyNo || '尚未配置' }}
            </p>
          </div>
          <div class="responsive-split-row">
            <span class="inquiry-status">{{ detail.contractStatusDescription }}</span>
            <button
              type="button"
              class="dialog-close"
              aria-label="關閉保單明細"
              @click="closeDetail"
            >
              ×
            </button>
          </div>
        </div>
        <nav class="file-pages" aria-label="要保書檔案分頁">
          <button
            v-for="(page, index) in pages"
            :key="page"
            :class="{ active: activePage === index }"
            @click="activePage = index"
          >
            <small>第 {{ index + 1 }} 頁</small>{{ page }}
          </button>
        </nav>

        <section v-if="activePage === 0" class="file-page">
          <h3>insurance_application｜要保書主檔</h3>
          <table>
            <thead>
              <tr>
                <th>欄位</th>
                <th>資料內容</th>
                <th>欄位</th>
                <th>資料內容</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th scope="row">要保書號碼</th>
                <td>{{ detail.applicationNo }}</td>
                <th scope="row">正式保單號碼</th>
                <td>{{ display(detail.policyNo) }}</td>
              </tr>
              <tr>
                <th scope="row">要保日期</th>
                <td>{{ detail.applicationDate }}</td>
                <th scope="row">預定生效日</th>
                <td>{{ detail.requestedEffectiveDate }}</td>
              </tr>
              <tr>
                <th scope="row">通路</th>
                <td>{{ detail.channelCode }}</td>
                <th scope="row">分支機構</th>
                <td>{{ display(detail.branchCode) }}</td>
              </tr>
              <tr>
                <th scope="row">業務員代碼</th>
                <td>{{ display(detail.insuranceAgentCode) }}</td>
                <th scope="row">商品／版本</th>
                <td>{{ detail.productCode }}／{{ detail.productVersion }}</td>
              </tr>
              <tr>
                <th scope="row">繳別</th>
                <td>{{ detail.paymentModeCode }}</td>
                <th scope="row">保險金額</th>
                <td>{{ detail.currencyCode }} {{ money(detail.sumAssuredAmount) }}</td>
              </tr>
              <tr>
                <th scope="row">保費</th>
                <td>{{ detail.currencyCode }} {{ money(detail.premiumAmount) }}</td>
                <th scope="row">契約狀態</th>
                <td>{{ detail.contractStatusDescription }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 1" class="file-page">
          <h3>application_party｜契約關係人檔</h3>
          <table>
            <thead>
              <tr>
                <th>角色</th>
                <th>姓名</th>
                <th>客戶識別</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>要保人</td>
                <td>{{ detail.applicantName }}</td>
                <td>{{ detail.applicantCustomerId }}</td>
              </tr>
              <tr>
                <td>被保險人</td>
                <td>{{ detail.insuredName }}</td>
                <td>{{ detail.insuredCustomerId }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 2" class="file-page">
          <h3>customer_contact｜客戶聯絡檔</h3>
          <table>
            <thead>
              <tr>
                <th>角色</th>
                <th>聯絡類型</th>
                <th>聯絡資料</th>
                <th>主要</th>
                <th>驗證狀態</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in detail.customerContacts"
                :key="row.partyRoleCode + row.contactTypeCode"
              >
                <td>{{ row.partyRoleCode }}</td>
                <td>{{ row.contactTypeCode }}</td>
                <td>{{ row.contactValueMasked }}</td>
                <td>{{ row.primaryContact ? '是' : '否' }}</td>
                <td>{{ row.verificationStatus }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 3" class="file-page">
          <h3>customer_address｜客戶地址檔</h3>
          <table>
            <thead>
              <tr>
                <th>角色</th>
                <th>地址類型</th>
                <th>郵遞區號</th>
                <th>地址</th>
                <th>生效日</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in detail.customerAddresses"
                :key="row.partyRoleCode + row.addressTypeCode"
              >
                <td>{{ row.partyRoleCode }}</td>
                <td>{{ row.addressTypeCode }}</td>
                <td>{{ row.postalCode }}</td>
                <td>{{ row.addressMasked }}</td>
                <td>{{ row.effectiveFrom }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 4" class="file-page">
          <h3>application_coverage｜保障內容檔</h3>
          <table>
            <thead>
              <tr>
                <th>序號</th>
                <th>主附約</th>
                <th>商品／版本</th>
                <th>保額</th>
                <th>保費</th>
                <th>保障／繳費年期</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detail.coverages" :key="row.coverageItemSeq">
                <td>{{ row.coverageItemSeq }}</td>
                <td>{{ row.coverageItemType }}</td>
                <td>{{ row.productCode }}／{{ row.productVersion }}</td>
                <td>{{ row.currencyCode }} {{ money(row.sumAssuredAmount) }}</td>
                <td>{{ row.currencyCode }} {{ money(row.premiumAmount) }}</td>
                <td>
                  {{ display(row.coverageTermYears) }}／{{ display(row.premiumPaymentTermYears) }}
                </td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 5" class="file-page">
          <h3>application_beneficiary｜受益人檔</h3>
          <table>
            <thead>
              <tr>
                <th>類型／序號</th>
                <th>受益人</th>
                <th>順位</th>
                <th>比例</th>
                <th>與被保險人關係</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="row in detail.beneficiaries"
                :key="row.beneficiaryTypeCode + row.beneficiarySeq"
              >
                <td>{{ row.beneficiaryTypeCode }}／{{ row.beneficiarySeq }}</td>
                <td>{{ row.beneficiaryCustomerReference || row.beneficiaryDesignationCode }}</td>
                <td>{{ row.priorityNo }}</td>
                <td>{{ display(row.allocationPercentage) }}%</td>
                <td>{{ display(row.relationshipToInsuredCode) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 6" class="file-page">
          <h3>health_disclosure｜健康告知檔</h3>
          <table>
            <thead>
              <tr>
                <th>題組版本</th>
                <th>問題代碼</th>
                <th>答案</th>
                <th>補充說明</th>
                <th>確認時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detail.healthDisclosures" :key="row.questionCode">
                <td>{{ row.questionSetCode }}／{{ row.questionSetVersion }}</td>
                <td>{{ row.questionCode }}</td>
                <td>{{ row.answerCode }}</td>
                <td>{{ display(row.supplementalDetail) }}</td>
                <td>{{ display(row.confirmedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 7" class="file-page">
          <h3>application_declaration｜聲明同意檔</h3>
          <table>
            <thead>
              <tr>
                <th>聲明類型</th>
                <th>版本</th>
                <th>確認角色</th>
                <th>確認方式</th>
                <th>確認時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detail.declarations" :key="row.declarationTypeCode">
                <td>{{ row.declarationTypeCode }}</td>
                <td>{{ row.declarationVersion }}</td>
                <td>{{ row.confirmedByPartyRole }}</td>
                <td>{{ row.confirmationMethod }}</td>
                <td>{{ row.confirmedAt }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else-if="activePage === 8" class="file-page">
          <h3>application_signature｜簽署檔</h3>
          <table>
            <thead>
              <tr>
                <th>簽署角色</th>
                <th>客戶識別</th>
                <th>簽署方式</th>
                <th>簽署時間</th>
                <th>驗證時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detail.signatures" :key="row.signerPartyRole">
                <td>{{ row.signerPartyRole }}</td>
                <td>{{ row.signerCustomerReference }}</td>
                <td>{{ row.signatureMethod }}</td>
                <td>{{ row.signedAt }}</td>
                <td>{{ display(row.verifiedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <section v-else class="file-page">
          <h3>initial_premium_due｜首期保費應收檔</h3>
          <table>
            <thead>
              <tr>
                <th>應收編號</th>
                <th>應繳金額</th>
                <th>計算規則</th>
                <th>應收狀態</th>
                <th>計算時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in detail.premiumDues" :key="row.premiumDueId">
                <td>{{ row.premiumDueId }}</td>
                <td>{{ row.currencyCode }} {{ money(row.calculatedPremiumAmount) }}</td>
                <td>{{ row.calculationRuleVersion }}</td>
                <td>{{ row.dueStatus }}</td>
                <td>{{ row.calculatedAt }}</td>
              </tr>
            </tbody>
          </table>
        </section>
        <PageNavigator v-model="activePage" :total="pages.length" />
      </article>
    </dialog>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>

<style scoped>
.file-pages {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin: 18px 0;
  padding-bottom: 8px;
}
.file-pages button {
  min-width: 130px;
  border: 1px solid #cbd5e1;
  background: #fff;
  border-radius: 7px;
  padding: 9px;
  color: #475569;
}
.file-pages button.active {
  border-color: #0f766e;
  background: #e6f5f3;
  color: #0f766e;
  font-weight: 700;
}
.file-pages small {
  display: block;
}
.file-page {
  overflow-x: auto;
}
.file-page h3 {
  margin: 8px 0 16px;
}
@media (max-width: 760px) {
  .file-pages button {
    min-width: 112px;
  }
  .file-page h3 {
    font-size: 1rem;
    overflow-wrap: anywhere;
  }
}
</style>

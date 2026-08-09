<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import { codeDefinitionApi } from '../../../shared/api/codeDefinitionApi'
import type { CodeDefinitionOption } from '../../../shared/types/codeDefinition'
import { applicationEntryApi } from '../api/applicationEntryApi'
import type { BeneficiaryInput, CoverageInput } from '../types/applicationEntry'
import '../../../application-entry.css'
const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Taipei' }).format(new Date())
const form = reactive({
  applicationNo: '',
  applicationDate: today,
  channelCode: 'AGENT',
  branchCode: '',
  insuranceAgentCode: '',
  applicantCustomerId: '',
  insuredCustomerId: '',
  applicantRelationshipToInsuredCode: 'SELF',
  currencyCode: 'TWD',
  paymentModeCode: 'ANNUAL',
  requestedEffectiveDate: today,
  electronicPolicy: true,
  fundsSourceCode: 'SALARY',
  insurancePurposeCode: 'PROTECTION',
  coverages: [
    {
      coverageItemType: 'BASE',
      productCode: 'LIFE-DEMO',
      productVersion: '1.0',
      sumAssuredAmount: '1000000',
      premiumAmount: '12000',
      coverageTermYears: 20,
      premiumPaymentTermYears: 20,
    },
  ] as CoverageInput[],
  beneficiaries: [
    {
      beneficiaryTypeCode: 'DEATH',
      beneficiaryCustomerId: null,
      beneficiaryDesignationCode: 'LEGAL_HEIRS',
      priorityNo: 1,
      allocationPercentage: null,
      relationshipToInsuredCode: null,
    },
  ] as BeneficiaryInput[],
  healthDisclosures: [
    { questionCode: 'DEMO_CURRENT_TREATMENT', answerCode: 'NO', supplementalDetail: null },
    { questionCode: 'DEMO_RECENT_HOSPITALIZATION', answerCode: 'NO', supplementalDetail: null },
    { questionCode: 'DEMO_CHRONIC_CONDITION', answerCode: 'NO', supplementalDetail: null },
  ],
  truthfulDisclosureConfirmed: false,
  personalDataConsentConfirmed: false,
  termsReviewedConfirmed: false,
  applicantSignatureConfirmed: false,
  insuredSignatureConfirmed: false,
  signatureMethod: 'ELECTRONIC',
})
const loading = ref(false),
  codeLoading = ref(false),
  currencyOptions = ref<CodeDefinitionOption[]>([]),
  sourceOfFundsOptions = ref<CodeDefinitionOption[]>([]),
  insurancePurposeOptions = ref<CodeDefinitionOption[]>([]),
  message = ref<string | null>(null),
  error = ref<string | null>(null),
  activePage = ref(0)
const applicationPages = ['要保事項', '契約關係人', '投保內容', '受益人', '健康告知', '聲明與簽署']
const totalPremium = computed(() =>
  form.coverages.reduce((n, c) => n + (Number(c.premiumAmount) || 0), 0).toLocaleString('zh-TW'),
)
const totalSum = computed(() =>
  form.coverages.reduce((n, c) => n + (Number(c.sumAssuredAmount) || 0), 0).toLocaleString('zh-TW'),
)

/** 載入資料庫維護的幣別、資金來源與投保目的，選單只顯示代碼與中文。 */
async function loadCodeDefinitionOptions() {
  codeLoading.value = true
  try {
    const [currencies, sourcesOfFunds, insurancePurposes] = await Promise.all([
      codeDefinitionApi.findActiveOptions('new-contract', 'currency_code'),
      codeDefinitionApi.findActiveOptions('customer-kyc', 'source_of_funds_code'),
      codeDefinitionApi.findActiveOptions('customer-kyc', 'insurance_purpose_code'),
    ])
    currencyOptions.value = currencies
    sourceOfFundsOptions.value = sourcesOfFunds
    insurancePurposeOptions.value = insurancePurposes
  } catch (e) {
    error.value = e instanceof Error ? e.message : '幣別代碼載入失敗'
  } finally {
    codeLoading.value = false
  }
}
function addCoverage() {
  form.coverages.push({
    coverageItemType: 'RIDER',
    productCode: '',
    productVersion: '1.0',
    sumAssuredAmount: '',
    premiumAmount: '',
    coverageTermYears: 20,
    premiumPaymentTermYears: 20,
  })
}
function addBeneficiary() {
  form.beneficiaries.push({
    beneficiaryTypeCode: 'DEATH',
    beneficiaryCustomerId: '',
    beneficiaryDesignationCode: null,
    priorityNo: 1,
    allocationPercentage: '',
    relationshipToInsuredCode: '',
  })
}
function sameAsApplicant() {
  form.insuredCustomerId = form.applicantCustomerId
  form.applicantRelationshipToInsuredCode = 'SELF'
}
async function submit() {
  loading.value = true
  message.value = null
  error.value = null
  try {
    const payload = JSON.parse(JSON.stringify(form))
    payload.beneficiaries = payload.beneficiaries.map((b: BeneficiaryInput) => ({
      ...b,
      beneficiaryCustomerId: b.beneficiaryCustomerId || null,
      beneficiaryDesignationCode: b.beneficiaryDesignationCode || null,
      allocationPercentage: b.allocationPercentage || null,
      relationshipToInsuredCode: b.relationshipToInsuredCode || null,
    }))
    payload.healthDisclosures = payload.healthDisclosures.map(
      (h: { supplementalDetail: string | null }) => ({
        ...h,
        supplementalDetail: h.supplementalDetail || null,
      }),
    )
    const result = await applicationEntryApi.create(payload)
    message.value = `保單登打已送覆核，覆核編號：${result.reviewId}`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '建立失敗'
  } finally {
    loading.value = false
  }
}

onMounted(loadCodeDefinitionOptions)
</script>
<template>
  <section class="content-page application-page">
    <header class="page-header application-document-header">
      <div>
        <p class="eyebrow">LIFE INSURANCE APPLICATION</p>
        <h2>人身保險要保書</h2>
        <p>
          依主管機關示範內容，分為基本資料、告知事項及聲明事項；請由要保人及被保險人確認後送件。
        </p>
      </div>
      <span class="status-chip">草稿</span>
    </header>
    <nav class="application-page-tabs" aria-label="要保書頁次">
      <button
        v-for="(page, index) in applicationPages"
        :key="page"
        :class="{ active: activePage === index }"
        @click="activePage = index"
      >
        <small>第 {{ index + 1 }} 頁</small><b>{{ page }}</b>
      </button>
    </nav>
    <form @submit.prevent="submit">
      <article v-show="activePage === 0" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>1</b>要保事項與通路資料</h3>
          <small>＊為必填</small>
        </div>
        <div class="field-grid three-cols">
          <label
            >要保書號碼＊<input
              v-model.trim="form.applicationNo"
              maxlength="32"
              required
              placeholder="NC-20260808-001" /></label
          ><label>要保日期＊<input v-model="form.applicationDate" type="date" required /></label
          ><label
            >預定生效日＊<input v-model="form.requestedEffectiveDate" type="date" required /></label
          ><label
            >通路＊<select v-model="form.channelCode">
              <option value="AGENT">業務員</option>
              <option value="BANK">銀行保險</option>
              <option value="WEB">網路投保</option>
            </select></label
          ><label>分公司／服務據點<input v-model.trim="form.branchCode" /></label
          ><label>招攬人員登錄字號<input v-model.trim="form.insuranceAgentCode" /></label>
        </div>
      </article>
      <article v-show="activePage === 1" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>2</b>要保人與被保險人</h3>
          <small>姓名、證號、地址及聯絡資料由客戶主檔帶入</small>
        </div>
        <div class="field-grid">
          <label
            >要保人客戶編號＊<input
              v-model.trim="form.applicantCustomerId"
              required
              placeholder="自然人或公司／行號客戶編號" /></label
          ><label
            >要保人與被保險人關係＊<select v-model="form.applicantRelationshipToInsuredCode">
              <option value="SELF">本人</option>
              <option value="SPOUSE">配偶</option>
              <option value="PARENT">父母</option>
              <option value="CHILD">子女</option>
              <option value="OTHER">其他</option>
            </select></label
          ><label>被保險人客戶編號＊<input v-model.trim="form.insuredCustomerId" required /></label>
          <div class="inline-action">
            <button type="button" class="secondary-button" @click="sameAsApplicant">
              同要保人
            </button>
          </div>
        </div>
        <p class="hint">
          要保人可為自然人或公司／行號；被保險人為自然人。送件時保存客戶主檔版本參照。
        </p>
      </article>
      <article v-show="activePage === 2" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>3</b>保險商品、保險金額與保險費</h3>
          <button type="button" class="secondary-button" @click="addCoverage">＋新增附約</button>
        </div>
        <div v-for="(c, i) in form.coverages" :key="i" class="repeat-card">
          <div class="row-heading">
            <strong>{{ c.coverageItemType === 'BASE' ? '主約' : '附約' }} {{ i + 1 }}</strong
            ><button
              v-if="c.coverageItemType === 'RIDER'"
              type="button"
              class="text-button danger"
              @click="form.coverages.splice(i, 1)"
            >
              移除
            </button>
          </div>
          <div class="field-grid three-cols">
            <label
              >保障類型＊<select v-model="c.coverageItemType">
                <option value="BASE">主約</option>
                <option value="RIDER">附約</option>
              </select></label
            ><label>商品代碼＊<input v-model.trim="c.productCode" required /></label
            ><label>商品版本＊<input v-model.trim="c.productVersion" required /></label
            ><label
              >保險金額＊<input
                v-model="c.sumAssuredAmount"
                type="number"
                min="0.0001"
                step="0.0001"
                required /></label
            ><label
              >首期保費＊<input
                v-model="c.premiumAmount"
                type="number"
                min="0"
                step="0.0001"
                required /></label
            ><label
              >保障年期<input v-model.number="c.coverageTermYears" type="number" min="1" /></label
            ><label
              >繳費年期<input v-model.number="c.premiumPaymentTermYears" type="number" min="1"
            /></label>
          </div>
        </div>
        <div class="summary-strip">
          <span
            >總保額 <strong>{{ form.currencyCode }} {{ totalSum }}</strong></span
          ><span
            >首期應繳 <strong>{{ form.currencyCode }} {{ totalPremium }}</strong></span
          ><label
            >幣別<select v-model="form.currencyCode" :disabled="codeLoading" required>
              <option
                v-for="currency in currencyOptions"
                :key="currency.code"
                :value="currency.code"
              >
                {{ currency.code }}｜{{ currency.description }}
              </option>
            </select></label
          ><label
            >繳別<select v-model="form.paymentModeCode">
              <option value="ANNUAL">年繳</option>
              <option value="SEMI_ANNUAL">半年繳</option>
              <option value="QUARTERLY">季繳</option>
              <option value="MONTHLY">月繳</option>
            </select></label
          >
        </div>
      </article>
      <article v-show="activePage === 3" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>4</b>身故受益人</h3>
          <button type="button" class="secondary-button" @click="addBeneficiary">
            ＋新增受益人
          </button>
        </div>
        <div v-for="(b, i) in form.beneficiaries" :key="i" class="repeat-card">
          <div class="row-heading">
            <strong>受益人 {{ i + 1 }}</strong
            ><button
              v-if="form.beneficiaries.length > 1"
              type="button"
              class="text-button danger"
              @click="form.beneficiaries.splice(i, 1)"
            >
              移除
            </button>
          </div>
          <div class="field-grid three-cols">
            <label
              >指定方式＊<select
                v-model="b.beneficiaryDesignationCode"
                @change="b.beneficiaryCustomerId = b.beneficiaryDesignationCode ? null : ''"
              >
                <option :value="null">指定客戶</option>
                <option value="LEGAL_HEIRS">法定繼承人</option>
              </select></label
            ><label v-if="!b.beneficiaryDesignationCode"
              >受益人客戶編號＊<input v-model.trim="b.beneficiaryCustomerId" required /></label
            ><label
              >順位＊<input v-model.number="b.priorityNo" type="number" min="1" required /></label
            ><label v-if="!b.beneficiaryDesignationCode"
              >分配比例（%）＊<input
                v-model="b.allocationPercentage"
                type="number"
                min="0.0001"
                max="100"
                required /></label
            ><label v-if="!b.beneficiaryDesignationCode"
              >與被保險人關係<input v-model.trim="b.relationshipToInsuredCode"
            /></label>
          </div>
        </div>
      </article>
      <article v-show="activePage === 4" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>5</b>被保險人告知事項</h3>
          <small>正式商品應使用核准題庫與適用期間</small>
        </div>
        <div v-for="(h, i) in form.healthDisclosures" :key="h.questionCode" class="disclosure-row">
          <span
            >{{ i + 1 }}.
            {{
              [
                '目前是否接受診療、用藥或追蹤檢查？',
                '近期是否曾住院、手術或接受重大檢查？',
                '是否曾被診斷慢性疾病或重大疾病？',
              ][i]
            }}</span
          ><select v-model="h.answerCode">
            <option value="NO">否</option>
            <option value="YES">是</option></select
          ><textarea
            v-if="h.answerCode === 'YES'"
            v-model.trim="h.supplementalDetail"
            required
            rows="2"
            placeholder="請補充疾病名稱、日期、醫療院所與目前狀況"
          ></textarea>
        </div>
        <p class="privacy-note">健康資料屬敏感個資，送出後加密保存，不寫入 log。</p>
      </article>
      <article v-show="activePage === 5" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>6</b>聲明、授權與簽署</h3>
        </div>
        <div class="field-grid">
          <label
            >保費資金來源＊<select v-model="form.fundsSourceCode" :disabled="codeLoading">
              <option
                v-for="option in sourceOfFundsOptions"
                :key="option.code"
                :value="option.code"
              >
                {{ option.code }}｜{{ option.description }}
              </option>
            </select></label
          ><label
            >投保目的＊<select v-model="form.insurancePurposeCode" :disabled="codeLoading">
              <option
                v-for="option in insurancePurposeOptions"
                :key="option.code"
                :value="option.code"
              >
                {{ option.code }}｜{{ option.description }}
              </option>
            </select></label
          ><label
            >簽署方式＊<select v-model="form.signatureMethod">
              <option value="ELECTRONIC">電子簽署</option>
              <option value="PAPER">紙本簽署</option>
            </select></label
          ><label class="consent-row"
            ><input v-model="form.electronicPolicy" type="checkbox" />同意電子保單</label
          >
        </div>
        <div class="declaration-list">
          <label
            ><input
              v-model="form.truthfulDisclosureConfirmed"
              type="checkbox"
              required
            />被保險人確認告知內容完整且據實。</label
          ><label
            ><input
              v-model="form.personalDataConsentConfirmed"
              type="checkbox"
              required
            />要保人同意個人資料蒐集、處理及利用。</label
          ><label
            ><input
              v-model="form.termsReviewedConfirmed"
              type="checkbox"
              required
            />要保人已審閱商品條款、投保人須知及重要事項。</label
          ><label
            ><input
              v-model="form.applicantSignatureConfirmed"
              type="checkbox"
              required
            />要保人已親自確認並完成簽署。</label
          ><label
            ><input
              v-model="form.insuredSignatureConfirmed"
              type="checkbox"
              required
            />被保險人已親自確認並完成簽署。</label
          >
        </div>
        <div class="form-actions">
          <button class="primary-button" :disabled="loading">
            {{ loading ? '送件中…' : '儲存整份要保書' }}
          </button>
        </div>
      </article>
      <PageNavigator v-model="activePage" :total="applicationPages.length" prefix="第" />
    </form>
    <p v-if="message" class="status-message success">{{ message }}</p>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>

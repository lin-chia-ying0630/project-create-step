<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import SectionTabNavigator from '../../../shared/components/SectionTabNavigator.vue'
import CodeDefinitionSelect from '../../../shared/components/CodeDefinitionSelect.vue'
import { codeDefinitionApi } from '../../../shared/api/codeDefinitionApi'
import type { CodeDefinitionOption } from '../../../shared/types/codeDefinition'
import { applicationEntryApi } from '../api/applicationEntryApi'
import { productDefinitionApi } from '../api/productDefinitionApi'
import type { BeneficiaryInput, CoverageInput } from '../types/applicationEntry'
import type { ProductDefinitionOption } from '../types/productDefinition'
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
  investmentProduct: false,
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
  initialPremiumAuthorization: {
    authorizationTypeCode: 'B',
    payerRoleCode: 'APPLICANT',
    payerCustomerId: '',
    payerRelationshipCode: 'SELF',
    payerName: '',
    institutionCode: '',
    branchCode: '',
    paymentToken: '',
    maskedNumber: '',
    expiryMonth: null,
    expiryYear: null,
    authorizationDate: today,
    authorizationVersion: '1.0',
    confirmed: false,
  },
  crossSellingConsent: {
    applicable: false,
    agreed: false,
    consentVersion: '1.0',
    recipientCompanies: '',
    dataScopeCodes: 'BASIC',
    stopMethodAcknowledged: false,
  },
  investmentRisk: {
    applicable: false,
    questionnaireVersion: '1.0',
    customerRiskLevel: 'R1',
    productRiskLevel: 'R1',
    riskScore: 0,
    suitable: true,
    allocationSummary: '',
    disclosureConfirmed: false,
    proposalDelivered: false,
    recordingRequired: false,
    recordingReference: '',
  },
  attachments: [
    {
      attachmentTypeCode: 'APP',
      ownerPartyRole: 'APPLICANT',
      documentNoMasked: '',
      fileName: '',
      fileReference: '',
      fileHash: '',
      fileSizeBytes: null as number | null,
      pageCount: 1,
      issueDate: today,
      expiryDate: null,
    },
    {
      attachmentTypeCode: 'PAY',
      ownerPartyRole: 'PAYER',
      documentNoMasked: '',
      fileName: '',
      fileReference: '',
      fileHash: '',
      fileSizeBytes: null as number | null,
      pageCount: 1,
      issueDate: today,
      expiryDate: null,
    },
  ],
})
const paymentInstrumentNumber = ref('')
const loading = ref(false),
  codeLoading = ref(false),
  currencyOptions = ref<CodeDefinitionOption[]>([]),
  sourceOfFundsOptions = ref<CodeDefinitionOption[]>([]),
  insurancePurposeOptions = ref<CodeDefinitionOption[]>([]),
  attachmentTypeOptions = ref<CodeDefinitionOption[]>([]),
  customerRiskOptions = ref<CodeDefinitionOption[]>([]),
  productRiskOptions = ref<CodeDefinitionOption[]>([]),
  productOptions = ref<ProductDefinitionOption[]>([]),
  message = ref<string | null>(null),
  error = ref<string | null>(null),
  activePage = ref(0)
const applicationPages = [
  '受理與通路',
  '契約關係人',
  '投保內容',
  '受益人',
  '健康告知',
  '聲明與簽署',
  '首期保費授權',
  '跨售同意',
  '投資風險',
  '附件資料',
]
const applicationTabItems = applicationPages.map((label, index) => ({
  value: index,
  label,
  caption: `第 ${index + 1} 頁`,
}))
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
    const [currencies, sourcesOfFunds, insurancePurposes, attachmentTypes, customerRisks, productRisks, products] =
      await Promise.all([
      codeDefinitionApi.findActiveOptions('new-contract', 'currency_code'),
      codeDefinitionApi.findActiveOptions('customer-kyc', 'source_of_funds_code'),
      codeDefinitionApi.findActiveOptions('customer-kyc', 'insurance_purpose_code'),
      codeDefinitionApi.findActiveOptions('new-contract', 'attachment_type_code'),
      codeDefinitionApi.findActiveOptions('new-contract', 'customer_risk_level_code'),
      codeDefinitionApi.findActiveOptions('new-contract', 'product_risk_level_code'),
        productDefinitionApi.findActiveProducts(),
      ])
    currencyOptions.value = currencies
    sourceOfFundsOptions.value = sourcesOfFunds
    insurancePurposeOptions.value = insurancePurposes
    attachmentTypeOptions.value = attachmentTypes
    customerRiskOptions.value = customerRisks
    productRiskOptions.value = productRisks
    productOptions.value = products
  } catch (e) {
    error.value = e instanceof Error ? e.message : '幣別代碼載入失敗'
  } finally {
    codeLoading.value = false
  }
}
/** 商品選擇後由商品定義帶入版本、幣別與投資型判斷，不接受人工另選商品類型。 */
function selectProduct(coverage: CoverageInput, selectedKey: string) {
  const product = productOptions.value.find(
    (option) => `${option.productCode}::${option.productVersion}` === selectedKey,
  )
  if (!product) return
  coverage.productCode = product.productCode
  coverage.productVersion = product.productVersion
  if (coverage.coverageItemType === 'BASE') {
    form.currencyCode = product.currencyCode
    form.investmentProduct = product.investmentProduct
    form.investmentRisk.applicable = product.investmentProduct
    form.investmentRisk.productRiskLevel = product.productRiskLevelCode ?? ''
  }
}
/** 將完整帳號或卡號送往一次性驗證端點，畫面只保留 Token 與遮罩值。 */
async function validatePaymentInstrument() {
  error.value = null
  try {
    const authorization = form.initialPremiumAuthorization
    const result = await applicationEntryApi.validatePaymentInstrument({
      instrumentTypeCode: authorization.authorizationTypeCode as 'B' | 'C',
      instrumentNumber: paymentInstrumentNumber.value,
      bankCode: authorization.institutionCode || null,
      branchCode: authorization.branchCode || null,
      expiryMonth: authorization.expiryMonth,
      expiryYear: authorization.expiryYear,
    })
    authorization.paymentToken = result.paymentToken
    authorization.maskedNumber = result.maskedNumber
    paymentInstrumentNumber.value = ''
    message.value = `付款號碼格式驗證完成：${result.maskedNumber}`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '付款號碼驗證失敗'
  }
}
/** 切換付款工具時清除前一次驗證結果，避免沿用不同帳號或卡號的 Token。 */
function resetPaymentValidation() {
  form.initialPremiumAuthorization.paymentToken = ''
  form.initialPremiumAuthorization.maskedNumber = ''
  paymentInstrumentNumber.value = ''
}
/** 新增一筆附件 metadata；檔案內容由受控檔案服務的參照識別。 */
function addAttachment() {
  form.attachments.push({
    attachmentTypeCode: 'OTH',
    ownerPartyRole: 'APPLICANT',
    documentNoMasked: '',
    fileName: '',
    fileReference: '',
    fileHash: '',
    fileSizeBytes: null as number | null,
    pageCount: 1,
    issueDate: today,
    expiryDate: null,
  })
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
    if (!form.initialPremiumAuthorization.paymentToken) {
      activePage.value = 6
      throw new Error('請先完成銀行帳號或信用卡號驗證')
    }
    const authorizationAttachment = form.attachments.find(
      (attachment) => attachment.attachmentTypeCode === 'PAY',
    )
    if (!authorizationAttachment?.fileName || !authorizationAttachment.fileReference) {
      activePage.value = 6
      throw new Error('請登打首期保費授權書檔名及受控檔案參照')
    }
    if (form.attachments.some((attachment) => !attachment.fileName || !attachment.fileReference)) {
      activePage.value = 9
      throw new Error('請完成所有附件的檔案名稱及受控檔案參照')
    }
    form.initialPremiumAuthorization.payerCustomerId ||= form.applicantCustomerId
    form.investmentRisk.applicable = form.investmentProduct
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
        <h2>保單登打新增</h2>
        <p>
          登打人身保險要保書、首期保費授權、同意書、投資風險資料及附件；完整確認後送交覆核。
        </p>
      </div>
      <span class="status-chip">新增草稿｜10 頁</span>
    </header>
    <SectionTabNavigator
      :model-value="activePage"
      :items="applicationTabItems"
      navigation-label="要保書頁次"
      @update:model-value="activePage = Number($event)"
    />
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
            ><label
              >保險商品＊<select
                :value="`${c.productCode}::${c.productVersion}`"
                required
                @change="selectProduct(c, ($event.target as HTMLSelectElement).value)"
              >
                <option value="" disabled>請選擇商品</option>
                <option
                  v-for="product in productOptions.filter(
                    (option) => option.coverageItemType === c.coverageItemType,
                  )"
                  :key="`${product.productCode}::${product.productVersion}`"
                  :value="`${product.productCode}::${product.productVersion}`"
                >
                  {{ product.productCode }}｜{{ product.productName }}｜{{
                    product.productTypeDescription
                  }}
                </option>
              </select></label
            ><label>商品版本<input :value="c.productVersion" readonly /></label
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
          ><CodeDefinitionSelect
            v-model="form.currencyCode"
            label="幣別"
            :options="currencyOptions"
            :disabled="codeLoading"
            required
          /><label
            >繳別<select v-model="form.paymentModeCode">
              <option value="ANNUAL">年繳</option>
              <option value="SEMI_ANNUAL">半年繳</option>
              <option value="QUARTERLY">季繳</option>
              <option value="MONTHLY">月繳</option>
            </select></label
          >
        </div>
        <p class="hint">
          商品類型由商品定義檔判斷：{{
            form.investmentProduct ? 'I｜投資型保險' : 'L｜傳統型壽險'
          }}。
        </p>
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
      </article>
      <article v-show="activePage === 6" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>7</b>首期保費授權</h3>
          <small>完整號碼驗證後不保存於要保書</small>
        </div>
        <div class="field-grid three-cols">
          <label
            >授權方式＊<select
              v-model="form.initialPremiumAuthorization.authorizationTypeCode"
              @change="resetPaymentValidation"
            >
              <option value="B">B｜銀行帳戶</option>
              <option value="C">C｜信用卡</option>
            </select></label
          >
          <label
            >繳款人客戶編號＊<input
              v-model.trim="form.initialPremiumAuthorization.payerCustomerId"
              required
          /></label>
          <label
            >授權人姓名＊<input v-model.trim="form.initialPremiumAuthorization.payerName" required
          /></label>
          <label
            >繳款人身分＊<select v-model="form.initialPremiumAuthorization.payerRoleCode">
              <option value="APPLICANT">要保人</option>
              <option value="INSURED">被保險人</option>
              <option value="OTHER">其他關係人</option>
            </select></label
          >
          <label
            >與要保人關係＊<input
              v-model.trim="form.initialPremiumAuthorization.payerRelationshipCode"
              required
          /></label>
          <label
            >銀行代碼＊<input
              v-model.trim="form.initialPremiumAuthorization.institutionCode"
              maxlength="3"
              inputmode="numeric"
              required
          /></label>
          <label v-if="form.initialPremiumAuthorization.authorizationTypeCode === 'B'"
            >分行代碼<input
              v-model.trim="form.initialPremiumAuthorization.branchCode"
              maxlength="4"
              inputmode="numeric"
          /></label>
          <label
            >{{
              form.initialPremiumAuthorization.authorizationTypeCode === 'B'
                ? '銀行帳號'
                : '信用卡號'
            }}＊<input
              v-model.trim="paymentInstrumentNumber"
              inputmode="numeric"
              autocomplete="off"
              :placeholder="form.initialPremiumAuthorization.maskedNumber || '輸入後按驗證'"
          /></label>
          <template v-if="form.initialPremiumAuthorization.authorizationTypeCode === 'C'"
            ><label
              >有效月＊<input
                v-model.trim="form.initialPremiumAuthorization.expiryMonth"
                maxlength="2"
                placeholder="MM" /></label
            ><label
              >有效年＊<input
                v-model.trim="form.initialPremiumAuthorization.expiryYear"
                maxlength="4"
                placeholder="YYYY" /></label
          ></template>
          <label
            >授權日期＊<input
              v-model="form.initialPremiumAuthorization.authorizationDate"
              type="date"
              required
          /></label>
          <label
            >授權書版本＊<input
              v-model.trim="form.initialPremiumAuthorization.authorizationVersion"
              required
          /></label>
        </div>
        <div class="form-actions">
          <button
            type="button"
            class="secondary-button"
            :disabled="!paymentInstrumentNumber"
            @click="validatePaymentInstrument"
          >
            驗證付款號碼
          </button>
        </div>
        <p v-if="form.initialPremiumAuthorization.maskedNumber" class="privacy-note">
          已驗證：{{
            form.initialPremiumAuthorization.maskedNumber
          }}；最終扣款仍以金融機構授權回覆為準。
        </p>
        <label class="consent-row"
          ><input
            v-model="form.initialPremiumAuthorization.confirmed"
            type="checkbox"
            required
          />授權人確認首期保費付款授權內容及附件正確。</label
        >
        <div class="panel-title subsection-title">
          <h4>首期保費授權書附件</h4>
          <small>正式檔案存放於受控文件服務，畫面只保存參照</small>
        </div>
        <div class="field-grid">
          <label
            >授權書檔案名稱＊<input
              v-model.trim="form.attachments[1].fileName"
              required
              placeholder="例如：首期保費授權書.pdf"
          /></label>
          <label
            >受控檔案參照＊<input
              v-model.trim="form.attachments[1].fileReference"
              required
              placeholder="DMS／物件儲存識別碼"
          /></label>
          <label
            >授權書頁數<input
              v-model.number="form.attachments[1].pageCount"
              type="number"
              min="1"
          /></label>
          <label
            >檔案雜湊<input
              v-model.trim="form.attachments[1].fileHash"
              placeholder="SHA-256（選填）"
          /></label>
        </div>
      </article>
      <article v-show="activePage === 7" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>8</b>共同行銷／跨售同意書</h3>
          <small>不同意不得影響本次保險要保</small>
        </div>
        <label class="consent-row"
          ><input
            v-model="form.crossSellingConsent.applicable"
            type="checkbox"
          />本案涉及金融控股公司子公司間共同行銷</label
        >
        <div v-if="form.crossSellingConsent.applicable" class="field-grid">
          <label
            >同意選擇＊<select v-model="form.crossSellingConsent.agreed">
              <option :value="false">不同意</option>
              <option :value="true">同意</option>
            </select></label
          >
          <label
            >同意書版本＊<input v-model.trim="form.crossSellingConsent.consentVersion" required
          /></label>
          <label v-if="form.crossSellingConsent.agreed"
            >接收資料公司＊<input
              v-model.trim="form.crossSellingConsent.recipientCompanies"
              required
          /></label>
          <label v-if="form.crossSellingConsent.agreed"
            >資料範圍代碼＊<input
              v-model.trim="form.crossSellingConsent.dataScopeCodes"
              placeholder="BASIC,TRANSACTION,INSURANCE"
              required
          /></label>
        </div>
        <label v-if="form.crossSellingConsent.applicable" class="consent-row"
          ><input
            v-model="form.crossSellingConsent.stopMethodAcknowledged"
            type="checkbox"
            required
          />已告知客戶可隨時要求停止資料交互運用的方法。</label
        >
      </article>
      <article v-show="activePage === 8" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>9</b>投資風險與商品適合度</h3>
          <small>{{ form.investmentProduct ? '投資型商品必填' : '非投資型商品不適用' }}</small>
        </div>
        <div v-if="form.investmentProduct" class="field-grid three-cols">
          <label
            >問卷版本＊<input v-model.trim="form.investmentRisk.questionnaireVersion" required
          /></label>
          <label
            >評估分數＊<input
              v-model.number="form.investmentRisk.riskScore"
              type="number"
              min="0"
              required
          /></label>
          <label
            >客戶風險等級＊<CodeDefinitionSelect
              v-model="form.investmentRisk.customerRiskLevel"
              label="客戶風險等級"
              :options="customerRiskOptions"
              placeholder="請選擇客戶風險等級"
              required
            /></label
          >
          <label
            >商品風險等級＊<CodeDefinitionSelect
              v-model="form.investmentRisk.productRiskLevel"
              label="商品風險等級"
              :options="productRiskOptions"
              placeholder="由商品定義帶入"
              required
              disabled
            /></label
          >
          <label
            >適合度結果＊<select v-model="form.investmentRisk.suitable">
              <option :value="true">適合</option>
              <option :value="false">不適合</option>
            </select></label
          >
          <label class="wide-field"
            >投資標的與配置（合計100%）＊<textarea
              v-model.trim="form.investmentRisk.allocationSummary"
              rows="3"
              required
            />
          </label>
          <label
            >錄音錄影／電子軌跡參照<input v-model.trim="form.investmentRisk.recordingReference"
          /></label>
        </div>
        <div v-if="form.investmentProduct" class="declaration-list">
          <label
            ><input
              v-model="form.investmentRisk.disclosureConfirmed"
              type="checkbox"
              required
            />已確認投資損益、匯率、費用及解約風險。</label
          ><label
            ><input
              v-model="form.investmentRisk.proposalDelivered"
              type="checkbox"
              required
            />商品說明書、建議書及風險預告書已交付。</label
          ><label
            ><input
              v-model="form.investmentRisk.recordingRequired"
              type="checkbox"
            />本案須保存錄音、錄影或電子軌跡。</label
          >
        </div>
        <p v-else class="hint">非投資型商品不建立風險適合度資料。</p>
      </article>
      <article v-show="activePage === 9" class="panel form-section application-sheet">
        <div class="panel-title">
          <h3><b>10</b>附件資料</h3>
          <button type="button" class="secondary-button" @click="addAttachment">＋新增附件</button>
        </div>
        <div v-for="(attachment, index) in form.attachments" :key="index" class="repeat-card">
          <div class="row-heading">
            <strong>附件 {{ index + 1 }}</strong
            ><button
              v-if="form.attachments.length > 1"
              type="button"
              class="text-button danger"
              @click="form.attachments.splice(index, 1)"
            >
              移除
            </button>
          </div>
          <div class="field-grid three-cols">
            <CodeDefinitionSelect
              v-model="attachment.attachmentTypeCode"
              label="附件類型"
              :options="attachmentTypeOptions"
              required
            />
            <label
              >所屬角色＊<select v-model="attachment.ownerPartyRole">
                <option value="APPLICANT">要保人</option>
                <option value="INSURED">被保險人</option>
                <option value="PAYER">繳款人</option>
                <option value="APPLICATION">要保案件</option>
              </select></label
            >
            <label>文件編號遮罩值<input v-model.trim="attachment.documentNoMasked" /></label>
            <label>檔案名稱＊<input v-model.trim="attachment.fileName" required /></label>
            <label
              >受控檔案參照＊<input
                v-model.trim="attachment.fileReference"
                placeholder="DMS／物件儲存識別碼"
                required
            /></label>
            <label>檔案雜湊<input v-model.trim="attachment.fileHash" /></label>
            <label
              >檔案大小（Bytes）<input
                v-model.number="attachment.fileSizeBytes"
                type="number"
                min="1"
                max="10485760"
            /></label>
            <label>頁數<input v-model.number="attachment.pageCount" type="number" min="1" /></label>
            <label>發證日<input v-model="attachment.issueDate" type="date" /></label>
            <label>到期日<input v-model="attachment.expiryDate" type="date" /></label>
          </div>
        </div>
        <div class="form-actions">
          <button class="primary-button" :disabled="loading">
            {{ loading ? '送件中…' : '儲存整份要保書並送覆核' }}
          </button>
        </div>
      </article>
      <PageNavigator v-model="activePage" :total="applicationPages.length" prefix="第" />
    </form>
    <p v-if="message" class="status-message success">{{ message }}</p>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>

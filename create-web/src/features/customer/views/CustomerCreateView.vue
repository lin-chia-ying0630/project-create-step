<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { customerApi } from '../api/customerApi'
import { customerCodeDefinitionApi } from '../api/customerCodeDefinitionApi'
import { postalCodeApi } from '../../postal-code/api/postalCodeApi'
import type { PostalCodeArea } from '../../postal-code/types/postalCode'
import type { CodeDefinitionOption } from '../types/customer'
const form = reactive({
  customerTypeCode: 'PERSON' as 'PERSON' | 'ORGANIZATION',
  identityTypeCode: 'NATIONAL_ID',
  identityNo: '',
  customerName: '',
  genderCode: '',
  birthDate: '',
  establishmentDate: '',
  responsiblePersonName: '',
  industryCode: '',
  organizationTypeCode: 'COMPANY',
  nationalityCode: 'TW',
  residencyCountryCode: 'TW',
  mobilePhone: '',
  email: '',
  postalCode: '',
  contactAddress: '',
  occupationCode: '',
  sourceOfFundsCode: 'SALARY',
  insurancePurposeCode: 'PROTECTION',
  consentVersion: '2026-01',
})
const consent = ref(false),
  loading = ref(false),
  codeLoading = ref(false),
  postalLoading = ref(false),
  postalArea = ref<PostalCodeArea | null>(null),
  occupationOptions = ref<CodeDefinitionOption[]>([]),
  sourceOfFundsOptions = ref<CodeDefinitionOption[]>([]),
  insurancePurposeOptions = ref<CodeDefinitionOption[]>([]),
  message = ref<string | null>(null),
  error = ref<string | null>(null)
/** 載入新契約自己維護的 KYC 代碼與繁體中文說明。 */
async function loadKycCodeDefinitions() {
  codeLoading.value = true
  try {
    const [occupations, sourcesOfFunds, insurancePurposes] = await Promise.all([
      customerCodeDefinitionApi.findOccupations(),
      customerCodeDefinitionApi.findSourcesOfFunds(),
      customerCodeDefinitionApi.findInsurancePurposes(),
    ])
    occupationOptions.value = occupations
    sourceOfFundsOptions.value = sourcesOfFunds
    insurancePurposeOptions.value = insurancePurposes
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'KYC 代碼載入失敗'
  } finally {
    codeLoading.value = false
  }
}
function switchType() {
  if (form.customerTypeCode === 'ORGANIZATION') {
    form.identityTypeCode = 'BUSINESS_REGISTRATION_NO'
    form.genderCode = ''
    form.birthDate = ''
    form.occupationCode = 'BUSINESS_ENTITY'
  } else {
    form.identityTypeCode = 'NATIONAL_ID'
    form.organizationTypeCode = 'COMPANY'
    form.responsiblePersonName = ''
    form.industryCode = ''
    form.establishmentDate = ''
    form.occupationCode = ''
  }
}
function selectCustomerType(type: 'PERSON' | 'ORGANIZATION') {
  form.customerTypeCode = type
  switchType()
  message.value = null
  error.value = null
}
async function lookupPostalCode() {
  form.postalCode = form.postalCode.replace(/\D/g, '').slice(0, 6)
  postalArea.value = null
  if (form.postalCode.length !== 3 && form.postalCode.length !== 6) return
  postalLoading.value = true
  try {
    postalArea.value = await postalCodeApi.find(form.postalCode)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '郵遞區號查詢失敗'
  } finally {
    postalLoading.value = false
  }
}
async function submit() {
  loading.value = true
  message.value = null
  error.value = null
  try {
    if (!postalArea.value) throw new Error('請輸入有效的 3 或 6 碼郵遞區號')
    const payload = {
      ...form,
      contactAddress: postalArea.value.addressPrefix + form.contactAddress,
      genderCode: form.customerTypeCode === 'PERSON' ? form.genderCode : null,
      birthDate: form.customerTypeCode === 'PERSON' ? form.birthDate : null,
      establishmentDate:
        form.customerTypeCode === 'ORGANIZATION' && form.establishmentDate
          ? form.establishmentDate
          : null,
      responsiblePersonName:
        form.customerTypeCode === 'ORGANIZATION' ? form.responsiblePersonName : null,
      industryCode: form.customerTypeCode === 'ORGANIZATION' ? form.industryCode : null,
      organizationTypeCode:
        form.customerTypeCode === 'ORGANIZATION' ? form.organizationTypeCode : null,
    }
    const result = await customerApi.create(payload)
    message.value = `客戶 ${result.customerName} 已建立，客戶編號：${result.customerId}，識別號碼：${result.maskedIdentityNo}`
    form.identityNo = ''
    form.customerName = ''
  } catch (e) {
    error.value = e instanceof Error ? e.message : '客戶建立失敗'
  } finally {
    loading.value = false
  }
}
onMounted(loadKycCodeDefinitions)
</script>
<template>
  <section class="content-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">CUSTOMER 360</p>
        <h2>{{ form.customerTypeCode === 'PERSON' ? '自然人客戶建立' : '公司／行號客戶建立' }}</h2>
        <p>
          {{
            form.customerTypeCode === 'PERSON'
              ? '建立自然人身分、聯絡方式與個人 KYC 資料。'
              : '建立法人或商號的統一編號、負責人、聯絡方式與法人 KYC 資料。'
          }}
        </p>
      </div>
      <span class="status-chip">{{
        form.customerTypeCode === 'PERSON' ? '自然人' : '公司／行號'
      }}</span>
    </header>
    <nav class="customer-type-switch" aria-label="客戶建立類型">
      <button
        type="button"
        :class="{ active: form.customerTypeCode === 'PERSON' }"
        @click="selectCustomerType('PERSON')"
      >
        <b>自然人客戶</b><small>身分證、居留證或護照</small></button
      ><button
        type="button"
        :class="{ active: form.customerTypeCode === 'ORGANIZATION' }"
        @click="selectCustomerType('ORGANIZATION')"
      >
        <b>公司／行號客戶</b><small>公司、商號或非營利組織</small>
      </button>
    </nav>
    <form class="panel" @submit.prevent="submit">
      <div class="panel-title">
        <h3>
          {{ form.customerTypeCode === 'PERSON' ? '自然人身分與基本資料' : '公司／行號基本資料' }}
        </h3>
        <small>＊為必填欄位</small>
      </div>
      <div class="field-grid">
        <label
          >客戶類型＊<select v-model="form.customerTypeCode" @change="switchType">
            <option value="PERSON">自然人</option>
            <option value="ORGANIZATION">公司／行號</option>
          </select></label
        ><label
          >識別類型＊<select
            v-model="form.identityTypeCode"
            :disabled="form.customerTypeCode === 'ORGANIZATION'"
          >
            <option value="NATIONAL_ID">國民身分證</option>
            <option value="RESIDENT_CERTIFICATE">居留證</option>
            <option value="PASSPORT">護照</option>
            <option value="BUSINESS_REGISTRATION_NO">統一編號</option>
          </select></label
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '證件號碼' : '統一編號' }}＊<input
            v-model.trim="form.identityNo"
            maxlength="20"
            autocomplete="off"
            required /></label
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '姓名' : '公司／行號名稱' }}＊<input
            v-model.trim="form.customerName"
            maxlength="100"
            required /></label
        ><template v-if="form.customerTypeCode === 'PERSON'"
          ><label
            >性別＊<select v-model="form.genderCode" required>
              <option value="" disabled>請選擇</option>
              <option value="M">男性</option>
              <option value="F">女性</option>
              <option value="X">其他／未指定</option>
            </select></label
          ><label
            >出生日期＊<input v-model="form.birthDate" type="date" required /></label></template
        ><template v-else
          ><label
            >組織類型＊<select v-model="form.organizationTypeCode">
              <option value="COMPANY">公司</option>
              <option value="BUSINESS">商號／行號</option>
              <option value="NONPROFIT">非營利組織</option>
            </select></label
          ><label
            >負責人姓名＊<input
              v-model.trim="form.responsiblePersonName"
              maxlength="100"
              required /></label
          ><label>設立日期<input v-model="form.establishmentDate" type="date" /></label
          ><label
            >行業別代碼＊<input
              v-model.trim="form.industryCode"
              maxlength="32"
              required
              placeholder="例：FINANCIAL_SERVICES" /></label></template
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '國籍' : '登記國家' }}＊<input
            v-model.trim="form.nationalityCode"
            maxlength="2"
            required /></label
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '居住國家' : '營業所在國' }}＊<input
            v-model.trim="form.residencyCountryCode"
            maxlength="2"
            required /></label
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '行動電話' : '公司電話' }}＊<input
            v-model.trim="form.mobilePhone"
            maxlength="30"
            autocomplete="off"
            required /></label
        ><label
          >電子郵件＊<input
            v-model.trim="form.email"
            type="email"
            maxlength="254"
            autocomplete="off"
            required /></label
        ><label
          >郵遞區號＊<input
            v-model.trim="form.postalCode"
            maxlength="6"
            inputmode="numeric"
            placeholder="3 或 6 碼"
            required
            @input="lookupPostalCode"
          /><small v-if="postalLoading">查詢中…</small></label
        ><label
          >縣市／行政區<input
            :value="postalArea?.addressPrefix ?? ''"
            readonly
            placeholder="依郵遞區號自動帶入" /></label
        ><label class="wide-field"
          >{{
            form.customerTypeCode === 'PERSON'
              ? '通訊地址（路街門牌）'
              : '登記／通訊地址（路街門牌）'
          }}＊<input
            v-model.trim="form.contactAddress"
            maxlength="280"
            autocomplete="off"
            required
            placeholder="請輸入路、街、段、巷、弄、號、樓"
        /></label>
      </div>
      <div class="panel-title section-gap"><h3>KYC 基本資料</h3></div>
      <div class="field-grid">
        <label
          >職業＊<select v-model="form.occupationCode" :disabled="codeLoading" required>
            <option value="" disabled>{{ codeLoading ? '代碼載入中…' : '請選擇' }}</option>
            <option v-for="option in occupationOptions" :key="option.code" :value="option.code">
              {{ option.description }}（{{ option.code }}）
            </option>
          </select></label
        ><label
          >資金來源＊<select v-model="form.sourceOfFundsCode" :disabled="codeLoading" required>
            <option v-for="option in sourceOfFundsOptions" :key="option.code" :value="option.code">
              {{ option.description }}（{{ option.code }}）
            </option>
          </select></label
        ><label
          >投保目的＊<select v-model="form.insurancePurposeCode" :disabled="codeLoading" required>
            <option
              v-for="option in insurancePurposeOptions"
              :key="option.code"
              :value="option.code"
            >
              {{ option.description }}（{{ option.code }}）
            </option>
          </select></label
        >
      </div>
      <label class="consent-row"
        ><input
          v-model="consent"
          type="checkbox"
          required
        />已取得客戶個人資料蒐集、處理及利用同意</label
      >
      <div class="form-actions">
        <button class="primary-button" :disabled="loading || !consent">
          {{ loading ? '建立中…' : '建立客戶資料' }}
        </button>
      </div>
    </form>
    <p v-if="message" class="status-message success">{{ message }}</p>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>
<style scoped>
.customer-type-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0 0 20px;
}
.customer-type-switch button {
  display: grid;
  gap: 5px;
  text-align: left;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  background: #fff;
  color: #475569;
  padding: 16px 18px;
  cursor: pointer;
}
.customer-type-switch button.active {
  border: 2px solid #0f766e;
  background: #e9f7f5;
  color: #0f766e;
}
.customer-type-switch b {
  font-size: 1.05rem;
}
.customer-type-switch small {
  font-weight: 400;
}
@media (max-width: 700px) {
  .customer-type-switch {
    grid-template-columns: 1fr;
  }
}
</style>

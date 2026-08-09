<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { customerApi } from '../api/customerApi'
import { customerCodeDefinitionApi } from '../api/customerCodeDefinitionApi'
import { codeDefinitionApi } from '../../../shared/api/codeDefinitionApi'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import SortableTableHeader from '../../../shared/components/SortableTableHeader.vue'
import type { CodeDefinitionOption } from '../../../shared/types/codeDefinition'
import type { CustomerPage, CustomerSummary } from '../types/customer'
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
  occupationOptions = ref<CodeDefinitionOption[]>([]),
  sourceOfFundsOptions = ref<CodeDefinitionOption[]>([]),
  insurancePurposeOptions = ref<CodeDefinitionOption[]>([]),
  countryOptions = ref<CodeDefinitionOption[]>([]),
  postalCodeOptions = ref<CodeDefinitionOption[]>([]),
  message = ref<string | null>(null),
  error = ref<string | null>(null)
const customerPage = ref<CustomerPage>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const sortField = ref('customerId')
const sortDirection = ref<'asc' | 'desc'>('asc')
const selectedCustomer = ref<CustomerSummary | null>(null)
const customerDialog = ref<HTMLDialogElement | null>(null)

const selectedPostalCode = computed(() =>
  postalCodeOptions.value.find((option) => option.code === form.postalCode),
)

/** 載入客戶建檔使用的 KYC、國家與郵遞區號代碼定義。 */
async function loadKycCodeDefinitions() {
  codeLoading.value = true
  try {
    const [occupations, sourcesOfFunds, insurancePurposes, countries, postalCodes] =
      await Promise.all([
        customerCodeDefinitionApi.findOccupations(),
        customerCodeDefinitionApi.findSourcesOfFunds(),
        customerCodeDefinitionApi.findInsurancePurposes(),
        codeDefinitionApi.findActiveOptions('common', 'country_code'),
        codeDefinitionApi.findActiveOptions('customer-contact', 'postal_code3'),
      ])
    occupationOptions.value = occupations
    sourceOfFundsOptions.value = sourcesOfFunds
    insurancePurposeOptions.value = insurancePurposes
    countryOptions.value = countries
    postalCodeOptions.value = postalCodes
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
    form.occupationCode = ''
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
async function submit() {
  loading.value = true
  message.value = null
  error.value = null
  try {
    if (!selectedPostalCode.value) throw new Error('請選擇有效的郵遞區號')
    const payload = {
      ...form,
      contactAddress: selectedPostalCode.value.description + form.contactAddress,
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
    message.value = `客戶建立已送覆核，覆核編號：${result.reviewId}`
    form.identityNo = ''
    form.customerName = ''
    await loadCustomers(1)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '客戶建立失敗'
  } finally {
    loading.value = false
  }
}

/** 以共用後端分頁規格取得客戶摘要，不載入證件、聯絡方式或地址。 */
async function loadCustomers(page = customerPage.value.page) {
  try {
    customerPage.value = await customerApi.findPage(
      page,
      customerPage.value.pageSize,
      `${sortField.value},${sortDirection.value}`,
    )
  } catch (e) {
    error.value = e instanceof Error ? e.message : '客戶清單載入失敗'
  }
}

/** 切換前三個資料欄位排序後回到第一頁。 */
function changeSort(field: string, direction: 'asc' | 'desc') {
  sortField.value = field
  sortDirection.value = direction
  void loadCustomers(1)
}

/** 變更共用每頁筆數後回到第一頁。 */
function changePageSize(pageSize: number) {
  customerPage.value.pageSize = pageSize
  void loadCustomers(1)
}

/** 開啟客戶摘要明細；清單不揭露證件、聯絡方式或地址等敏感資料。 */
function openCustomer(item: CustomerSummary) {
  selectedCustomer.value = item
  customerDialog.value?.showModal()
}

/** 關閉客戶摘要明細視窗。 */
function closeCustomer() {
  customerDialog.value?.close()
  selectedCustomer.value = null
}

onMounted(() => Promise.all([loadKycCodeDefinitions(), loadCustomers(1)]))
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
    <article class="panel">
      <div class="panel-title responsive-split-row">
        <h3>客戶資料清單</h3>
        <span>共 {{ customerPage.totalItems }} 筆</span>
      </div>
      <div class="data-table-scope">
        <table class="data-table">
          <thead>
            <tr>
              <th>操作</th>
              <SortableTableHeader
                field="customerId"
                label="客戶 ID"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <SortableTableHeader
                field="customerTypeCode"
                label="客戶類型"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <SortableTableHeader
                field="customerName"
                label="姓名／名稱"
                :active-field="sortField"
                :direction="sortDirection"
                @sort="changeSort"
              />
              <th>國籍</th>
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
            <tr v-for="item in customerPage.items" :key="item.customerId">
              <td>
                <button type="button" class="secondary-button" @click="openCustomer(item)">
                  查看
                </button>
              </td>
              <td>{{ item.customerId }}</td>
              <td>{{ item.customerTypeCode }}</td>
              <td>{{ item.customerName }}</td>
              <td>{{ item.nationalityCode }}</td>
              <td>{{ item.recordStatus }}</td>
              <td>{{ item.createdBy }}</td>
              <td>{{ item.createdAt }}</td>
              <td>{{ item.updatedBy }}</td>
              <td>{{ item.updatedAt }}</td>
              <td>{{ item.reviewerId || '尚未覆核' }}</td>
              <td>{{ item.reviewedAt || '尚未覆核' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <PageNavigator
        v-if="customerPage.totalPages > 0"
        :model-value="customerPage.page - 1"
        :total="customerPage.totalPages"
        :page-size="customerPage.pageSize"
        prefix="客戶資料清單"
        @update:model-value="loadCustomers($event + 1)"
        @update:page-size="changePageSize"
      />
    </article>
    <dialog ref="customerDialog" class="review-dialog" @cancel="closeCustomer">
      <article v-if="selectedCustomer" class="dialog-content">
        <header class="dialog-header">
          <div>
            <p class="eyebrow">CUSTOMER SUMMARY</p>
            <h3>客戶資料 {{ selectedCustomer.customerId }}</h3>
          </div>
          <button
            type="button"
            class="dialog-close"
            aria-label="關閉客戶明細"
            @click="closeCustomer"
          >
            ×
          </button>
        </header>
        <div class="data-table-scope">
          <table class="data-table summary-table">
            <tbody>
              <tr>
                <th scope="row">客戶 ID</th>
                <td>{{ selectedCustomer.customerId }}</td>
                <th scope="row">客戶類型</th>
                <td>{{ selectedCustomer.customerTypeCode }}</td>
              </tr>
              <tr>
                <th scope="row">姓名／名稱</th>
                <td>{{ selectedCustomer.customerName }}</td>
                <th scope="row">國籍</th>
                <td>{{ selectedCustomer.nationalityCode }}</td>
              </tr>
              <tr>
                <th scope="row">狀態</th>
                <td>{{ selectedCustomer.recordStatus }}</td>
                <th scope="row">新增人員</th>
                <td>{{ selectedCustomer.createdBy }}</td>
              </tr>
              <tr>
                <th scope="row">建立時間</th>
                <td>{{ selectedCustomer.createdAt }}</td>
                <th scope="row">修改人員</th>
                <td>{{ selectedCustomer.updatedBy }}</td>
              </tr>
              <tr>
                <th scope="row">修改時間</th>
                <td>{{ selectedCustomer.updatedAt }}</td>
                <th scope="row">覆核人員</th>
                <td>{{ selectedCustomer.reviewerId || '尚未覆核' }}</td>
              </tr>
              <tr>
                <th scope="row">覆核時間</th>
                <td colspan="3">{{ selectedCustomer.reviewedAt || '尚未覆核' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>
    </dialog>
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
          >{{ form.customerTypeCode === 'PERSON' ? '國籍' : '登記國家' }}＊<select
            v-model="form.nationalityCode"
            :disabled="codeLoading"
            required
          >
            <option v-for="country in countryOptions" :key="country.code" :value="country.code">
              {{ country.code }}｜{{ country.description }}
            </option>
          </select></label
        ><label
          >{{ form.customerTypeCode === 'PERSON' ? '居住國家' : '營業所在國' }}＊<select
            v-model="form.residencyCountryCode"
            :disabled="codeLoading"
            required
          >
            <option v-for="country in countryOptions" :key="country.code" :value="country.code">
              {{ country.code }}｜{{ country.description }}
            </option>
          </select></label
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
          >郵遞區號＊<select v-model="form.postalCode" :disabled="codeLoading" required>
            <option value="" disabled>請選擇</option>
            <option v-for="postal in postalCodeOptions" :key="postal.code" :value="postal.code">
              {{ postal.code }}｜{{ postal.description }}
            </option>
          </select></label
        ><label
          >縣市／行政區<input
            :value="selectedPostalCode?.description ?? ''"
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
              {{ option.code }}｜{{ option.description }}
            </option>
          </select></label
        ><label
          >資金來源＊<select v-model="form.sourceOfFundsCode" :disabled="codeLoading" required>
            <option v-for="option in sourceOfFundsOptions" :key="option.code" :value="option.code">
              {{ option.code }}｜{{ option.description }}
            </option>
          </select></label
        ><label
          >投保目的＊<select v-model="form.insurancePurposeCode" :disabled="codeLoading" required>
            <option
              v-for="option in insurancePurposeOptions"
              :key="option.code"
              :value="option.code"
            >
              {{ option.code }}｜{{ option.description }}
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

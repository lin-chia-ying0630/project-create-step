<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageNavigator from '../../../shared/components/PageNavigator.vue'
import QueryListPanels from '../../../shared/components/QueryListPanels.vue'
import SortableTableHeader from '../../../shared/components/SortableTableHeader.vue'
import { policyReversalApi } from '../api/policyReversalApi'
import type { PolicyReversalPage, PolicyReversalPreview } from '../types/policyReversal'

const policyNo = ref('')
const reasonCode = ref('WRONG_ISSUANCE')
const reasonDescription = ref('')
const confirmed = ref(false)
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const preview = ref<PolicyReversalPreview | null>(null)
const policyPage = ref<PolicyReversalPage>({
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
})
const sortField = ref('policyNo')
const sortDirection = ref<'asc' | 'desc'>('asc')

const canExecute = computed(
  () =>
    preview.value &&
    preview.value.blockers.length === 0 &&
    reasonCode.value &&
    reasonDescription.value.trim().length >= 10 &&
    confirmed.value &&
    !loading.value,
)

async function loadPreview() {
  loading.value = true
  error.value = null
  success.value = null
  confirmed.value = false
  try {
    preview.value = await policyReversalApi.preview(policyNo.value.trim())
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查詢失敗'
  } finally {
    loading.value = false
  }
}

/** 初次進入即從後端列出契約狀態 01 的承保撤回候選。 */
async function loadPolicies(page = policyPage.value.page) {
  loading.value = true
  error.value = null
  try {
    policyPage.value = await policyReversalApi.list(
      page,
      policyPage.value.pageSize,
      `${sortField.value},${sortDirection.value}`,
    )
  } catch (e) {
    error.value = e instanceof Error ? e.message : '承保撤回清單載入失敗'
  } finally {
    loading.value = false
  }
}

/** 從操作欄選取保單後載入撤回影響預覽。 */
function openPolicy(selectedPolicyNo: string) {
  policyNo.value = selectedPolicyNo
  void loadPreview()
}

/** 切換前三個資料欄位排序後回到第一頁。 */
function changeSort(field: string, direction: 'asc' | 'desc') {
  sortField.value = field
  sortDirection.value = direction
  void loadPolicies(1)
}

/** 變更共用每頁筆數後回到第一頁。 */
function changePageSize(pageSize: number) {
  policyPage.value.pageSize = pageSize
  void loadPolicies(1)
}

async function executeReversal() {
  if (!preview.value || !canExecute.value) return
  loading.value = true
  error.value = null
  try {
    const p = preview.value
    const result = await policyReversalApi.execute(
      {
        policyNo: p.policyNo,
        reasonCode: reasonCode.value,
        reasonDescription: reasonDescription.value.trim(),
        expectedPolicyVersion: p.policyVersion,
        expectedApplicationVersion: p.applicationVersion,
        expectedUnderwritingVersion: p.underwritingVersion,
        confirmToken: p.confirmToken,
      },
      crypto.randomUUID(),
    )
    success.value = `承保撤回已送覆核，覆核編號：${result.reviewId}`
    preview.value = null
    await loadPolicies(1)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '承保撤回失敗'
  } finally {
    loading.value = false
  }
}
onMounted(() => loadPolicies(1))
</script>

<template>
  <section class="content-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">POLICY REVERSAL</p>
        <h2>承保撤回</h2>
        <p>承保撤回只處理契約狀態 01；覆核核准後保留正式保單，契約狀態改為空白。</p>
      </div>
      <span class="status-chip">契約狀態 01</span>
    </header>

    <QueryListPanels>
      <template #query>
        <div class="panel-title">
          <h3>承保撤回查詢</h3>
          <small>輸入正式保單號碼查詢可撤回資料</small>
        </div>
        <div class="search-row">
          <label>保單號碼<input v-model="policyNo" maxlength="32" autocomplete="off" /></label>
          <button
            class="primary-button"
            :disabled="!policyNo.trim() || loading"
            @click="loadPreview"
          >
            查詢承保案件
          </button>
        </div>
      </template>

      <template #list>
        <div class="panel-title responsive-split-row">
          <h3>契約狀態 01 保單清單</h3>
          <span>共 {{ policyPage.totalItems }} 筆</span>
        </div>
        <div class="data-table-scope">
          <table class="data-table">
            <thead>
              <tr>
                <th>操作</th>
                <SortableTableHeader
                  field="policyNo"
                  label="正式保單號碼"
                  :active-field="sortField"
                  :direction="sortDirection"
                  @sort="changeSort"
                />
                <SortableTableHeader
                  field="applicationNo"
                  label="要保書號碼"
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
                <th>契約狀態</th>
                <th>生效日</th>
                <th>新增人員</th>
                <th>建立時間</th>
                <th>修改人員</th>
                <th>修改時間</th>
                <th>覆核人員</th>
                <th>覆核時間</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in policyPage.items" :key="item.policyNo">
                <td>
                  <button class="reversal-button" @click="openPolicy(item.policyNo)">撤回</button>
                </td>
                <td>{{ item.policyNo }}</td>
                <td>{{ item.applicationNo }}</td>
                <td>{{ item.productCode }}</td>
                <td>{{ item.contractStatusCode }} 有效</td>
                <td>{{ item.effectiveDate }}</td>
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
          v-if="policyPage.totalPages > 0"
          :model-value="policyPage.page - 1"
          :total="policyPage.totalPages"
          :page-size="policyPage.pageSize"
          prefix="承保撤回清單"
          @update:model-value="loadPolicies($event + 1)"
          @update:page-size="changePageSize"
        />
      </template>
    </QueryListPanels>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>

    <template v-if="preview">
      <section class="panel section-gap grid">
        <div>
          <span>保單號碼</span><strong>{{ preview.policyNo }}</strong>
        </div>
        <div>
          <span>要保書號碼</span><strong>{{ preview.applicationNo }}</strong>
        </div>
        <div>
          <span>核保案件</span><strong>{{ preview.underwritingCaseNo }}</strong>
        </div>
        <div>
          <span>保單狀態</span><strong>{{ preview.policyStatus }}</strong>
        </div>
        <div>
          <span>預定生效日</span><strong>{{ preview.effectiveDate }}</strong>
        </div>
      </section>

      <section class="panel section-gap">
        <h2>預計異動資料</h2>
        <table class="data-table">
          <tbody>
            <tr v-for="(count, table) in preview.deleteCounts" :key="table">
              <th>{{ table }}</th>
              <td>{{ count }} 筆</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section v-if="preview.blockers.length" class="panel section-gap blocked">
        <h2>禁止執行原因</h2>
        <ul>
          <li v-for="item in preview.blockers" :key="item">{{ item }}</li>
        </ul>
      </section>

      <section class="panel section-gap form">
        <label
          >原因代碼<select v-model="reasonCode">
            <option value="WRONG_ISSUANCE">錯誤出單</option>
            <option value="DUPLICATE_ISSUANCE">重複出單</option>
            <option value="DATA_CORRECTION">資料修正重送</option>
          </select></label
        >
        <label
          >原因說明<textarea
            v-model="reasonDescription"
            maxlength="500"
            rows="4"
            placeholder="至少輸入 10 個字，請勿填寫健康或完整個資"
          ></textarea>
        </label>
        <label class="confirm"
          ><input v-model="confirmed" type="checkbox" />我已確認將保留正式保單並把契約狀態 01
          改為空白</label
        >
        <button class="reversal-button" :disabled="!canExecute" @click="executeReversal">
          送出契約狀態撤回覆核
        </button>
      </section>
    </template>
  </section>
</template>

<style scoped>
.warning,
.blocked {
  border-left: 4px solid #b42318;
  background: #fff2f0;
  padding: 1rem;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 1rem;
}
.grid div,
.form label {
  display: grid;
  gap: 0.35rem;
}
.grid span {
  color: #5c667a;
  font-size: 0.85rem;
}
input,
select,
textarea {
  padding: 0.65rem;
  border: 1px solid #9aa4b2;
  border-radius: 0.4rem;
}
.form {
  display: grid;
  gap: 1rem;
}
.confirm {
  display: flex !important;
  align-items: flex-start;
}
.error {
  color: #b42318;
}
.success {
  color: #087443;
}
@media (max-width: 760px) {
  .search-row button,
  .danger {
    width: 100%;
    min-height: 44px;
  }
}
</style>

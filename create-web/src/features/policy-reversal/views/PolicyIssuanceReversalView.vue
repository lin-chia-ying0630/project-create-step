<script setup lang="ts">
import { computed, ref } from 'vue'
import { policyReversalApi } from '../api/policyReversalApi'
import type { PolicyReversalPreview } from '../types/policyReversal'

const policyNo = ref('')
const reasonCode = ref('WRONG_ISSUANCE')
const reasonDescription = ref('')
const confirmed = ref(false)
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)
const preview = ref<PolicyReversalPreview | null>(null)

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
  } catch (e) {
    error.value = e instanceof Error ? e.message : '承保撤回失敗'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page">
    <h1>承保撤回</h1>
    <p class="warning">
      僅限錯誤出單且尚未形成有效權利義務的案件。有效保單或已有下游交易時禁止刪除。
    </p>

    <section class="card search-row">
      <label>保單號碼<input v-model="policyNo" maxlength="32" autocomplete="off" /></label>
      <button :disabled="!policyNo.trim() || loading" @click="loadPreview">查詢影響</button>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>

    <template v-if="preview">
      <section class="card grid">
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

      <section class="card">
        <h2>預計刪除資料</h2>
        <table class="data-table">
          <tbody>
            <tr v-for="(count, table) in preview.deleteCounts" :key="table">
              <th>{{ table }}</th>
              <td>{{ count }} 筆</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section v-if="preview.blockers.length" class="card blocked">
        <h2>禁止執行原因</h2>
        <ul>
          <li v-for="item in preview.blockers" :key="item">{{ item }}</li>
        </ul>
      </section>

      <section class="card form">
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
          ><input
            v-model="confirmed"
            type="checkbox"
          />我已確認此保單尚未生效且沒有收費、保全、理賠或其他下游資料</label
        >
        <button class="danger" :disabled="!canExecute" @click="executeReversal">
          刪除正式保單並退回未承保
        </button>
      </section>
    </template>
  </main>
</template>

<style scoped>
.page {
  max-width: 64rem;
  margin: 2rem auto;
  padding: 0 1rem;
  font-family: system-ui, sans-serif;
  color: #172033;
}
.warning,
.blocked {
  border-left: 4px solid #b42318;
  background: #fff2f0;
  padding: 1rem;
}
.card {
  margin: 1rem 0;
  padding: 1.25rem;
  border: 1px solid #d7dce5;
  border-radius: 0.75rem;
  background: white;
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
button {
  padding: 0.7rem 1rem;
  border: 0;
  border-radius: 0.4rem;
  background: #174ea6;
  color: white;
}
.danger {
  background: #b42318;
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
  .page {
    margin: 1rem auto;
    padding: 0;
  }
  .search-row button,
  .danger {
    width: 100%;
    min-height: 44px;
  }
  .card {
    padding: 1rem;
  }
}
</style>

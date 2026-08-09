<script setup lang="ts">
import { computed, ref } from 'vue'
import { premiumPaymentApi } from '../api/premiumPaymentApi'
import type { PremiumDuePreview } from '../types/premiumPayment'
const localNow = () => {
  const d = new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
  return d.toISOString().slice(0, 16)
}
const applicationNo = ref(''),
  due = ref<PremiumDuePreview | null>(null),
  receivedAmount = ref(''),
  paymentReceiptNo = ref(''),
  collectionReference = ref(''),
  paymentChannelCode = ref('BANK_TRANSFER'),
  receivedAt = ref(localNow()),
  payerRoleCode = ref('APPLICANT'),
  message = ref<string | null>(null),
  error = ref<string | null>(null),
  loading = ref(false)
const difference = computed(() => {
  if (!due.value || !receivedAmount.value) return null
  const scale = 10000n
  const parse = (v: string) => {
    const [a, b = ''] = v.split('.')
    return BigInt(a) * scale + BigInt((b + '0000').slice(0, 4))
  }
  try {
    return Number(parse(receivedAmount.value) - parse(due.value.calculatedPremiumAmount)) / 10000
  } catch {
    return null
  }
})
const differenceLabel = computed(() =>
  difference.value === null
    ? '尚未輸入'
    : difference.value === 0
      ? '金額相符'
      : difference.value < 0
        ? '短收'
        : '溢收',
)
async function search() {
  loading.value = true
  error.value = null
  message.value = null
  due.value = null
  try {
    due.value = await premiumPaymentApi.getDue(applicationNo.value.trim())
    receivedAmount.value = due.value.calculatedPremiumAmount
  } catch (e) {
    error.value = e instanceof Error ? e.message : '查詢失敗'
  } finally {
    loading.value = false
  }
}
async function reconcile() {
  if (!due.value) return
  loading.value = true
  error.value = null
  message.value = null
  try {
    const r = await premiumPaymentApi.registerAndReconcile(
      {
        applicationNo: due.value.applicationNo,
        paymentReceiptNo: paymentReceiptNo.value,
        paymentChannelCode: paymentChannelCode.value,
        collectionReference: collectionReference.value,
        currencyCode: due.value.currencyCode,
        receivedAmount: receivedAmount.value,
        receivedAt: receivedAt.value,
        payerRoleCode: payerRoleCode.value || null,
      },
      crypto.randomUUID(),
    )
    message.value = `首期保費資料已送覆核，覆核編號：${r.reviewId}`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '首期保險費收款失敗'
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <section class="content-page payment-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">INITIAL PREMIUM COLLECTION</p>
        <h2>首期保險費收款與銷帳</h2>
        <p>依要保書號碼或預編保單號碼查詢應收首期保險費，登錄實際收款資料後執行銷帳。</p>
      </div>
      <span class="status-chip">新契約收費</span>
    </header>
    <article class="panel">
      <div class="panel-title">
        <h3><b>1</b>查詢待收案件</h3>
        <small>可輸入要保書號碼或預編保單號碼</small>
      </div>
      <div class="search-row">
        <label
          >要保書／保單號碼＊<input
            v-model.trim="applicationNo"
            maxlength="32"
            placeholder="例：NC-20260808-001"
            @keyup.enter="search" /></label
        ><button
          class="primary-button"
          :disabled="!applicationNo.trim() || loading"
          @click="search"
        >
          查詢應收保險費
        </button>
      </div>
    </article>
    <article v-if="due" class="panel section-gap">
      <div class="panel-title">
        <h3><b>2</b>確認應收首期保險費</h3>
        <span class="payment-status">{{ due.dueStatusDescription }}</span>
      </div>
      <div class="amount-board">
        <div>
          <small>要保書號碼</small><strong>{{ due.applicationNo }}</strong>
        </div>
        <div>
          <small>應收首期保險費</small
          ><strong
            >{{ due.currencyCode }}
            {{ Number(due.calculatedPremiumAmount).toLocaleString('zh-TW') }}</strong
          >
        </div>
        <div>
          <small>保費計算版本</small><strong>{{ due.calculationRuleVersion }}</strong>
        </div>
      </div>
    </article>
    <article v-if="due" class="panel section-gap">
      <div class="panel-title">
        <h3><b>3</b>登錄繳費資料</h3>
        <small>＊為必填欄位</small>
      </div>
      <div class="field-grid">
        <label
          >繳費憑證號＊<input
            v-model.trim="paymentReceiptNo"
            maxlength="50"
            required
            placeholder="收據、繳款單或代收憑證號" /></label
        ><label
          >繳費管道＊<select v-model="paymentChannelCode">
            <option value="BANK_TRANSFER">銀行轉帳</option>
            <option value="CREDIT_CARD">信用卡</option>
            <option value="CASH">現金</option>
            <option value="CONVENIENCE_STORE">超商代收</option>
            <option value="DIRECT_DEBIT">帳戶扣款</option>
          </select></label
        ><label
          >收款交易序號＊<input
            v-model.trim="collectionReference"
            maxlength="100"
            required
            placeholder="銀行交易序號或代收機構回覆碼" /></label
        ><label>收款時間＊<input v-model="receivedAt" type="datetime-local" required /></label
        ><label
          >繳款人身分＊<select v-model="payerRoleCode">
            <option value="APPLICANT">要保人</option>
            <option value="INSURED">被保險人</option>
            <option value="BENEFICIARY">受益人</option>
            <option value="OTHER">其他關係人</option>
          </select></label
        ><label
          >實收保險費＊
          <div class="money-input">
            <span>{{ due.currencyCode }}</span
            ><input v-model="receivedAmount" inputmode="decimal" required /></div
        ></label>
      </div>
      <div
        class="reconcile-preview"
        :class="{ balanced: difference === 0, warning: difference !== null && difference !== 0 }"
      >
        <div>
          <small>銷帳試算</small><strong>{{ differenceLabel }}</strong>
        </div>
        <div>
          <small>收付差額</small
          ><strong
            >{{ due.currencyCode }} {{ difference === null ? '—' : difference.toFixed(4) }}</strong
          >
        </div>
        <p>
          {{
            difference === 0
              ? '應收與實收一致，銷帳後可進入核保。'
              : '短收、溢收或幣別不符時，不得直接承保。'
          }}
        </p>
      </div>
      <div class="form-actions">
        <button
          class="primary-button"
          :disabled="
            loading || !paymentReceiptNo || !collectionReference || !receivedAmount || !receivedAt
          "
          @click="reconcile"
        >
          確認收款並執行銷帳
        </button>
      </div>
    </article>
    <p v-if="message" class="status-message success">{{ message }}</p>
    <p v-if="error" class="status-message error">{{ error }}</p>
  </section>
</template>
<style scoped>
.payment-page {
  max-width: 960px;
}
.panel-title h3 {
  display: flex;
  align-items: center;
  gap: 9px;
}
.panel-title h3 b {
  display: inline-grid;
  place-items: center;
  width: 27px;
  height: 27px;
  border-radius: 50%;
  background: #0f766e;
  color: #fff;
  font-size: 0.85rem;
}
.amount-board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1px;
  background: #dbe4ea;
  border: 1px solid #dbe4ea;
  border-radius: 7px;
  overflow: hidden;
}
.amount-board div {
  display: grid;
  gap: 7px;
  background: #f8fafb;
  padding: 18px;
}
.amount-board small,
.reconcile-preview small {
  color: #647281;
}
.amount-board strong:nth-child(2) {
  font-size: 1.15rem;
}
.payment-status {
  background: #fff7d6;
  color: #7a5b00;
  border-radius: 999px;
  padding: 6px 10px;
}
.money-input {
  display: flex;
}
.money-input span {
  display: grid;
  place-items: center;
  border: 1px solid #cbd5df;
  border-right: 0;
  border-radius: 6px 0 0 6px;
  background: #eef2f5;
  padding: 0 12px;
}
.money-input input {
  flex: 1;
  border-radius: 0 6px 6px 0;
}
.reconcile-preview {
  display: grid;
  grid-template-columns: 1fr 1fr 2fr;
  gap: 18px;
  align-items: center;
  margin-top: 20px;
  border-left: 4px solid #647281;
  background: #f5f7f9;
  padding: 14px;
}
.reconcile-preview div {
  display: grid;
  gap: 5px;
}
.reconcile-preview p {
  margin: 0;
}
.reconcile-preview.balanced {
  border-color: #0f766e;
  background: #ecfdf5;
}
.reconcile-preview.warning {
  border-color: #d97706;
  background: #fff7ed;
}
@media (max-width: 760px) {
  .amount-board,
  .reconcile-preview {
    grid-template-columns: 1fr;
  }
  .money-input {
    min-width: 0;
  }
  .money-input input {
    width: 100%;
    min-width: 0;
  }
  .payment-status {
    justify-self: start;
  }
}
</style>

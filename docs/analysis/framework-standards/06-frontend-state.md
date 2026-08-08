# 前端狀態管理規範

> 適用範圍：Vue 3 + TypeScript + Pinia 的 Store 命名、目錄結構、資料分類與 API 呼叫統一模式。

## 1. 目錄結構

```
src/
  api/           ← API 呼叫函式（typed client）
    policyApi.ts
    claimApi.ts
    changeCaseApi.ts
    underwritingApi.ts
    premiumApi.ts
    codeApi.ts   ← 代碼定義
  stores/        ← Pinia store
    auth/
      authStore.ts        ← 使用者身份、functionCode 授權清單
    policy/
      policyStore.ts      ← 保單查詢、保單摘要
    change/
      addressChangeStore.ts
      amountChangeStore.ts
      changeCaseStore.ts  ← 保全案件列表與狀態
    claim/
      claimStore.ts
    underwriting/
      underwritingStore.ts
    shared/
      codeStore.ts        ← 代碼定義（全域快取）
      uiStore.ts          ← 全域 loading、toast 訊息
  types/         ← TypeScript 型別定義（對應後端 API schema）
    policy.ts
    claim.ts
    changeCase.ts
    common.ts    ← ResponseBodyDto、PageResult、ErrorDetail
```

## 2. Store 命名規則

- 檔名：`{domain}Store.ts`，camelCase。
- Store ID：與檔名相同，例如 `defineStore('policyStore', ...)`。
- 不使用 `useXxxStore()` 的 `Xxx` 作為 store 內部 state key 名稱，避免混淆。

## 3. 哪些資料放 Store

### 必須放 Store

| 資料 | Store | 原因 |
|---|---|---|
| 登入使用者資訊（userId、userName） | `authStore` | 跨多個頁面需要 |
| 使用者 functionCode 授權清單 | `authStore` | 控制按鈕顯示與 API 呼叫前檢查 |
| 代碼定義（code_definition） | `codeStore` | 全域共用，避免重複 API 呼叫 |
| 目前編輯中的保全案件草稿 | `changeCaseStore` | 多步驟表單跨頁面保持狀態 |

### 不放 Store（用 component local state）

| 資料 | 原因 |
|---|---|
| 單一畫面的表單輸入值 | 不需跨頁面，用 `ref`/`reactive` 即可 |
| Modal 開關狀態 | 局部 UI 狀態 |
| 分頁與排序參數 | 由 URL query string 管理，不重複存 store |

## 4. API 呼叫統一模式

所有 API 呼叫透過 `src/api/` 的 typed 函式，不在 store 或 component 直接用 `axios`/`fetch`。

### API 函式格式

```typescript
// src/api/policyApi.ts
import { apiClient } from './apiClient'
import type { PageResult } from '@/types/common'
import type { PolicySummaryResponse, PolicyQueryRequest } from '@/types/policy'

export const policyApi = {
  queryPolicies(req: PolicyQueryRequest): Promise<PageResult<PolicySummaryResponse>> {
    return apiClient.get('/api/v1/policies', { params: req })
  },
  getPolicyDetail(policyNo: string): Promise<PolicyDetailResponse> {
    return apiClient.get(`/api/v1/policies/${policyNo}`)
  }
}
```

### Store 呼叫 API 的標準模式

每個非同步操作必須管理 `loading`、`error`、`data` 三個狀態：

```typescript
// src/stores/policy/policyStore.ts
export const usePolicyStore = defineStore('policyStore', () => {
  const policies = ref<PageResult<PolicySummaryResponse> | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function queryPolicies(req: PolicyQueryRequest) {
    loading.value = true
    error.value = null
    try {
      policies.value = await policyApi.queryPolicies(req)
    } catch (e) {
      error.value = '系統連線異常，請稍後再試'
      useUiStore().showToast({ type: 'error', message: '系統連線異常，請稍後再試' })
    } finally {
      loading.value = false
    }
  }

  return { policies, loading, error, queryPolicies }
})
```

### 禁止事項

- 禁止在 component `<script setup>` 內直接呼叫 `axios` 或 `fetch`。
- 禁止在 component 內處理 `try/catch` 的 API 錯誤（統一由 store 處理）。
- 禁止在 store 寫死中文標籤（中文顯示名稱由後端 metadata 提供）。

## 5. 代碼定義快取策略

```typescript
// src/stores/shared/codeStore.ts
export const useCodeStore = defineStore('codeStore', () => {
  const codeMap = ref<Map<string, CodeDefinition[]>>(new Map())

  async function getCodesByGroup(codeGroup: string): Promise<CodeDefinition[]> {
    if (codeMap.value.has(codeGroup)) {
      return codeMap.value.get(codeGroup)!   // 已快取，直接回傳
    }
    const codes = await codeApi.getByGroup(codeGroup)
    codeMap.value.set(codeGroup, codes)
    return codeMap.value.get(codeGroup) ?? []
  }

  function clearCache() {
    codeMap.value.clear()   // 登出時呼叫
  }

  return { getCodesByGroup, clearCache }
})
```

- 代碼定義在使用時才載入（lazy loading），不在應用啟動時全量預載。
- 登出時必須呼叫 `codeStore.clearCache()` 清除快取，避免不同使用者看到相同代碼快取。

## 6. 授權控制規則

### 按鈕與功能顯示

```typescript
// composable
export function useAuth() {
  const authStore = useAuthStore()
  const hasPermission = (functionCode: string) =>
    authStore.authorizedFunctionCodes.includes(functionCode)
  return { hasPermission }
}

// template
<button v-if="hasPermission('CHG_SUBMIT')" @click="submit">送審</button>
```

- 前端隱藏按鈕不等於授權，後端每個 API 仍須驗證 `userId + functionCode`。
- `functionCode` 清單由登入後 API 回傳，存入 `authStore`，不在前端寫死判斷邏輯。

## 7. 全域 uiStore（Loading / Toast）

```typescript
export const useUiStore = defineStore('uiStore', () => {
  const globalLoading = ref(false)
  const toasts = ref<Toast[]>([])

  function showToast(toast: Omit<Toast, 'id'>) {
    toasts.value.push({ ...toast, id: Date.now().toString() })
    setTimeout(() => removeToast(toast), 5000)
  }

  function removeToast(toast: Toast) {
    toasts.value = toasts.value.filter(t => t.id !== toast.id)
  }

  return { globalLoading, toasts, showToast, removeToast }
})
```

- 跨頁面 loading 狀態（例如初始資料載入）用 `uiStore.globalLoading`。
- 各模組 API 操作的局部 loading 用各自 store 的 `loading`，不影響全域。

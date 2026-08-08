# 測試策略規範

> 適用範圍：後端 JUnit 5、MyBatis Mapper 測試、前端 Vitest、API contract test 與覆蓋率要求。

## 1. 測試分層

```
                    ┌─────────────────────────────┐
                    │   E2E / SIT / UAT（手動）      │  ← 業務驗收
                    ├─────────────────────────────┤
                    │   API Integration Test       │  ← Spring Boot Test + 真實 MySQL
                    ├─────────────────────────────┤
                    │   Unit Test（Service / 計算）  │  ← JUnit 5 + Mockito
                    └─────────────────────────────┘
前端：Vitest（Component + Store） + Playwright E2E（關鍵流程）
```

## 2. 後端單元測試

### 適用對象

- Service 層業務邏輯（狀態機轉換、業務規則驗證、保費計算）
- 純計算 Helper（保費試算、年齡計算、日期計算）
- Mapper/Adapter（DTO 轉換）

### 工具

- 測試框架：JUnit 5
- Mock 框架：Mockito（`@ExtendWith(MockitoExtension.class)`）
- 斷言：AssertJ（`assertThat`）

### 覆蓋率要求

| 層級 | 最低行覆蓋率 | 重點 |
|---|---|---|
| 保費計算（`PremiumCalculationService`） | **90%** | 費率公式、年齡計算、附加費率 |
| 業務規則驗證（`InsuranceBusinessValidator`） | **85%** | 狀態機、防重、職務分離 |
| 一般 Service | **70%** | 主要流程路徑 |
| Controller | 不強制 | 由 Integration Test 涵蓋 |

### 命名規範

```java
// 格式：{被測方法}_{情境描述}_{預期結果}
@Test
void calculatePremium_givenZeroInsuredAmount_throwsValidationException() { ... }

@Test
void approveReview_givenSameMakerAndReviewer_throwsBusinessRuleException() { ... }
```

## 3. MyBatis Mapper Integration Test

### 工具

- `@SpringBootTest` + `@Transactional`（每個 test 後自動 rollback）
- 使用真實 MySQL（Testcontainers）
- **不使用 H2**：保險業務依賴 MySQL 特有行為（DECIMAL、觸發器、FOR UPDATE）

```java
@SpringBootTest
@Testcontainers
@Transactional  // 每個 test 自動 rollback，不汙染資料
class PolicyContractDaoTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("pos_test");

    @Test
    void findPendingForUpdate_givenExistingPendingCase_returnsLockedRow() {
        // Given: 插入一筆 P 狀態的覆核資料
        // When: 呼叫 findPendingForUpdate
        // Then: 回傳資料且 SELECT FOR UPDATE 不拋例外
    }
}
```

### 必須測試的關鍵 SQL

| 測試項目 | 說明 |
|---|---|
| Unique constraint 觸發 | 相同防重鍵第二次 INSERT 應拋 `DuplicateKeyException` |
| 樂觀鎖版本不符 | `record_version` 不符時 `affectedRows == 0` |
| SELECT FOR UPDATE | 並發建立覆核時只有一方成功 |
| DECIMAL 精度 | 保費金額儲存與讀取精度應一致 |
| 稽核 append-only | 對該模組業務稽核表執行 UPDATE 應被 DB 權限或 trigger 阻擋 |

## 4. API Integration Test（Controller 層）

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class PolicyInquiryControllerTest {

    @Test
    void getPolicy_givenValidPolicyNo_returns200WithPolicyDetail() { ... }

    @Test
    void getPolicy_givenUnauthorizedUser_returns403() { ... }

    @Test
    void getPolicy_givenNonExistentPolicyNo_returns404() { ... }
}
```

### 每個 API 必須涵蓋的情境

| 情境 | HTTP Status |
|---|---|
| 正常成功 | 200 / 201 |
| 輸入驗證失敗 | 400 |
| 無權限（缺 functionCode） | 403 |
| 資源不存在 | 404 |
| 業務規則衝突 | 409 / 422 |
| （可選）系統內部錯誤模擬 | 500 |

## 5. 前端測試

### 工具

- 單元 / 元件測試：**Vitest** + `@vue/test-utils`
- E2E：**Playwright**（只涵蓋關鍵業務流程）

### 測試範圍

| 對象 | 工具 | 優先順序 |
|---|---|---|
| Pinia Store 業務邏輯 | Vitest | **高**：API 成功/失敗/loading 狀態轉換 |
| 表單驗證 composable | Vitest | **高**：必填、格式、業務規則 |
| API typed client | Vitest + MSW mock | **中**：序列化/反序列化 |
| 複雜 Component | Vitest + test-utils | **中**：動態欄位顯示、分頁 |
| 保全送審流程（E2E） | Playwright | **高** |
| 保單查詢流程（E2E） | Playwright | **中** |

### Store 測試範例

```typescript
// src/stores/policy/__tests__/policyStore.test.ts
import { setActivePinia, createPinia } from 'pinia'
import { usePolicyStore } from '../policyStore'
import { vi } from 'vitest'
import * as policyApi from '@/api/policyApi'

describe('policyStore', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('queryPolicies: 成功時設定 policies 並清除 error', async () => {
    vi.spyOn(policyApi, 'queryPolicies').mockResolvedValue({
      items: [], totalItems: 0, page: 1, pageSize: 20, totalPages: 0
    })
    const store = usePolicyStore()
    await store.queryPolicies({ page: 1, pageSize: 20 })
    expect(store.policies?.totalItems).toBe(0)
    expect(store.error).toBeNull()
  })

  it('queryPolicies: API 失敗時設定 error', async () => {
    vi.spyOn(policyApi, 'queryPolicies').mockRejectedValue(new Error('Network Error'))
    const store = usePolicyStore()
    await store.queryPolicies({ page: 1, pageSize: 20 })
    expect(store.error).toBe('系統連線異常，請稍後再試')
  })
})
```

## 6. 測試資料規則

- 所有測試資料使用完全虛構內容：
  - 一般流程身分識別占位值：`TEST-ID-0001`（明確不是真實證號）
  - 姓名：`測試要保人`、`虛構被保險人`
  - 保單號碼：`TEST-0000001`
  - 銀行帳號：`000-000000000`
- **禁止複製任何正式環境個資、保單資料或財務資料**進測試。
- 只有驗證身分證格式／檢核碼的專項測試可使用測試環境限定的確定性產生器；產物不得作為範例客戶資料或送往外部服務。
- 測試資料庫（Testcontainers）每次測試後 rollback，不保存持久資料。

## 7. CI 整合

```yaml
# CI pipeline（GitHub Actions / 其他）
- name: 後端測試
  run: ./mvnw test -Dtest.groups=unit,integration

- name: 前端測試
  run: npx vitest run --coverage

- name: 覆蓋率門檻檢查
  run: |
    # 保費計算 service 90%、業務驗證 85%、整體 70%
    ./mvnw verify -Djacoco.minimum.coverage=0.70
```

- PR merge 前必須所有測試通過。
- 覆蓋率低於門檻時 CI 失敗，需補測試或申請豁免（需 Tech Lead 核准）。
- Testcontainers 需要 Docker；CI 環境需確認 Docker daemon 可用。

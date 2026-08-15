---
name: test-case-generator
description: Generate traceable JUnit, integration, SIT, and UAT test cases from requirements, business rules, source code, APIs, and database contracts. Use for test design, regression coverage, boundary analysis, workflow validation, or acceptance criteria.
---

# 測試案例產生

## 工作流程

1. 完整 SA 流程時先讀取同一 `<domain>/<feature>` 可用的 `00-requirements.md`、`02-business-rules.md`、`03-specification.md` 與 `04-impact-analysis.md`。
2. 萃取需求並指派 rule 或 acceptance-criterion ID。
3. 分成 happy path、boundary、invalid、authorization、state、concurrency、retry 與 failure recovery。
4. 選擇能證明行為的最低測試層；ORM、SQL、lock、migration 與 precision 使用真實資料庫。
5. 定義可重現的前置資料、輸入、步驟、預期 response、DB／audit 影響與 cleanup。
6. 每個案例回鏈來源規則，並列出尚未覆蓋的規則。

## 保險系統必測

涵蓋生效日、商品版本、保單狀態、角色、重複送件、覆核狀態轉換、金額 rounding、null／blank 差異、dynamic code 狀態與 rollback。不得使用正式個資。

## 測試分層與工具規範

### 後端

| 測試層 | 工具 | 使用時機 |
|---|---|---|
| 業務規則單元測試 | JUnit 5 + Mockito + AssertJ | Service 邏輯、狀態機、驗證規則 |
| Mapper 整合測試 | Testcontainers MySQL（不使用 H2） | constraint、DECIMAL、FOR UPDATE、trigger |
| API 整合測試 | `@SpringBootTest` + MockMvc | Controller 路徑、HTTP status、ResponseBodyDto |

**禁止使用 H2**：保險業務依賴 MySQL 特有行為（DECIMAL 精度、FOR UPDATE、稽核 trigger）。

### 覆蓋率底線

| 範圍 | 最低指令覆蓋率 |
|---|---|
| 保費計算 Service | 90% |
| 業務驗證 Service | 85% |
| 一般 Service | 70% |
| Controller | 由 API 整合測試涵蓋，不另設 unit 門檻 |

CI JaCoCo `check` goal 強制執行；低於門檻時 build 失敗，不得以豁免取代補測試。

### 前端

| 對象 | 工具 |
|---|---|
| Pinia Store 邏輯 | Vitest + MSW（mock API） |
| 表單驗證 composable | Vitest |
| 關鍵業務 E2E | Playwright |

### 測試資料規則

- 所有測試資料使用完全虛構且可辨識的內容，例如業務鍵 `TEST-ENTITY-0001`；只建立案例真正需要的欄位。
- **禁止複製正式環境個資、保單資料或財務資料**進測試或 fixture。
- Testcontainers 每次測試後 `@Transactional` rollback，不保存持久資料。

## 必測情境清單

後端每支 API 必須涵蓋：成功（200/201）、輸入驗證失敗（400）、無授權（403）、資源不存在（404）、業務衝突（409/422）。

Mapper 測試必須涵蓋：Unique constraint 觸發、樂觀鎖版本不符（affectedRows=0）、SELECT FOR UPDATE 並發保護、稽核 trigger 阻擋 UPDATE/DELETE。

## 產出

提供 traceability matrix；使用者要求時提供可執行的 JUnit／integration test。測試名稱描述可觀察行為，格式 `{方法}_{情境}_{預期結果}`，不依賴 private implementation，並區分 automation、SIT 與 UAT 責任。完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/05-test-cases.md`，只有 `Confirmed` 規則可直接成為既有行為的確定預期。

# GitHub Copilot 專案指令

## 回覆與文件

- 所有解說、規劃、程式碼註解、測試名稱、commit 建議與 PR 摘要使用繁體中文。
- 程式碼識別字與通用技術名詞保留英文；保險名詞採用臺灣常見用語。
- 回覆先說明結果，再補充必要的決策、驗證與風險。

## 開始工作前

1. 讀取根目錄 `AGENTS.md`、`README.md` 及任務涉及的 `SKILL.md`。
2. 盤點現有目錄、建置方式、測試與使用者尚未提交的變更。
3. 區分已確認需求、合理假設與必須由使用者決定的事項。
4. 保險專案開案或領域功能設計時，遵循 `skills/start-insurance-project/SKILL.md`。
5. 舊系統分析依序使用 `legacy-code-explainer → business-rule-extractor → spec-generator → impact-analysis → test-case-generator`，但一次只載入目前階段所需能力。
6. 新保險需求使用 `insurance-requirement-modeler`；外部檔案或訊息使用 `insurance-interface-mapper`；批次筆數、金額及重跑問題使用 `insurance-reconciliation-analyzer`。
7. 新增、重構或檢查 Java/MyBatis 功能時，先讀 `skills/enforce-mybatis-three-layer/SKILL.md`。
8. 所有程式建立、修改與 review 都使用 `skills/enforce-code-writing-standards/SKILL.md`；不得產生多個 method／statement 擠在同一行的程式。採用設計模式前先說明實際變化點與測試邊界，不得產生只有一個實作且沒有替換需求的空殼 Strategy、Factory 或 Adapter。
9. 新增抽象、核保規則、外部介接、狀態轉換或複雜條件重構時，使用 `skills/design-pattern-guide/SKILL.md`；先輸出問題與變化點，再選模式。

## 分析文件

- 分析預設唯讀；未取得明確實作指示，不修改產品程式、migration、設定或部署檔。
- 新需求可先寫入 `00-requirements.md`；舊系統分析使用 `01-legacy-explanation.md` 至 `05-test-cases.md`。不適用階段可省略，但須記錄原因。
- 使用 `Confirmed`、`Inferred`、`Unknown` 標示證據；推測與缺少來源的內容不得變成正式需求或測試預期。
- 分開記錄正式需求的預期行為、程式／SQL 的目前行為、schema 的資料契約及其 gap。
- 文件使用繁體中文，原始技術名稱保留不翻譯；規則、欄位、API 與測試使用穩定追蹤 ID。

## 實作要求

- 使用 Java + MyBatis 實作後端、Vue 3 + TypeScript 實作前端，並以 Docker 提供可重現的本機執行環境。
- 不得擅自改用 JPA/Hibernate、JavaScript 或其他前端框架。
- 新專案預設沿用既有基準：Spring Boot 3、Java 17、Maven Wrapper、MyBatis、MySQL、Flyway、Vue 3、TypeScript、Pinia、Vite 與 Docker Compose。實際建立時確認相容的最新 patch 版本，不跨 major 升級。
- 先建立資料庫 → Entity → Request/Response DTO → API/OpenAPI → 前端型別 → UI metadata 的唯一契約。
- Entity、Create Request、Update Request 與 Query Response 分開；不得直接以 Entity 作為 API contract。
- 一次完成一條可驗證的垂直功能，包含資料、後端、API、前端、測試與文件。
- 固定封閉的代碼、狀態與繁中說明使用所屬 `domain` enum，enum 必須提供 `code`、`description` 及嚴格 `fromCode`；禁止在 Controller、Service、SQL 或 Vue 重複 switch／mapping。只有可由營運維護的動態代碼才由資料庫 code table 提供。
- 固定錯誤代碼與繁中訊息集中於領域 `ErrorCode` enum；throw 處只傳 enum，不直接寫代碼／固定訊息。
- 每個 Java、Vue、TypeScript function 都要有目的註解；public／protected Java method 使用 Javadoc，private 與前端 function 至少說明規則、狀態變化或副作用。禁止只重述方法名稱的空洞註解。
- 金額使用精確十進位型別；日期、時間、時區、生效日與終止日須明確定義。
- schema 只透過向前演進的 versioned migration 修改，不重寫已套用 migration。
- MyBatis mapper、SQL、result mapping 與 Entity 欄位必須逐一對應；動態 SQL 使用參數綁定，不拼接使用者輸入。
- **後端三層架構**：Controller 只負責接收請求與回傳 `ResponseBodyDto<T>`，不含業務邏輯；Service 負責業務規則、交易邊界與例外拋出；DAO（MyBatis Mapper）負責資料存取，不得在 Controller 或 Service 直接撰寫 SQL 字串。三層間只能透過 DTO/Entity 傳遞資料，不得跨層直接存取。
- **固定目錄**：以業務功能為第一層，使用 `<feature>/controller`、`dto`、`domain`（選用）、`service`、`service/impl`、`persistence`。不得把 Controller、Service、Mapper 混放在 `<feature>/` 根目錄，也不得把業務功能塞入 `common/`。
- **固定依賴**：`Controller → Service interface ← ServiceImpl → Mapper → MySQL`。Controller 只能注入 Service interface；`service/` 只放 interface，Spring concrete class 放 `service/impl/`；Mapper 不得依賴 Service 或 `ResponseBodyDto`。
- **MyBatis XML SQL**：本專案所有 SQL 只放 `src/main/resources/mapper/<feature>/*Mapper.xml`。任何 Java class 都不得包含 `SELECT`、`INSERT`、`UPDATE`、`DELETE` SQL、JDBC 或 SQL annotation；Java Mapper 只留 interface method、`@Mapper` 與必要 `@Param`。XML `namespace` 對應完整 interface 類名，statement `id` 對應 method；資料值只用 `#{}`，禁止 `${}` 接收使用者輸入。
- **工具類獨立管理**：只有無狀態、無領域決策的純技術邏輯才放入 `util/`；單一功能使用者放 `<feature>/util/`，真正跨功能者才放 `common/util/`。工具類不得注入 Spring Bean、呼叫資料庫或決定保險業務內容。
- **前端元件共用**：具有相同職責的 UI 片段（分頁列、排序標頭、表單欄位、狀態標籤、確認 Modal、錯誤提示、個資遮罩顯示）必須抽取為 `create-web/src/shared/components/` 下的共用元件；業務頁面只透過 props / slots 使用，不重複實作相同互動邏輯。新增功能前先確認 `shared/` 是否已有可複用的元件。
- **前端視覺共用**：表格、按鈕、表單、分頁、狀態訊息、色彩與間距等跨頁風格統一放 `create-web/src/shared/styles/style.scss`；feature View 不得再建立相同用途的第二套樣式。
- **裝置無關響應式**：所有裝置使用同一份 route、component、DOM、API 與功能，只依 viewport 自動重排；禁止 user-agent／裝置型號分支、手機專用頁或因寬度隱藏必要功能。至少支援 320px 且不得產生整頁水平捲動，主要按鈕高度至少 44px。
- **瀏覽器渲染入口**：保留 `index.html` 的 `lang="zh-Hant-TW"`、UTF-8、`width=device-width`、`initial-scale=1`、`viewport-fit=cover` 與 theme color；共用 layout 使用 `dvh` 與 safe-area 適應可用區域。
- Vue 元件負責呈現與互動，store 管理客戶端狀態，後端 service 管理保險業務規則與資料正確性。
- Docker image、Compose service、container port、host port、health check 與 README 必須一致。
- 所有業務應用 API 回覆都使用 `ResponseBodyDto<T>`，包括成功、驗證錯誤、權限錯誤、刪除與未分類例外；前端只在 HTTP client 解開 `data`。Health、OpenAPI、串流與外部協定端點只允許明確列出的例外。
- 保全／契約變更的受理中正式代碼使用小寫 `p`，顯示為「受理中」；其他保險領域先確認自己的正式代碼。
- 後端繁中 metadata 是欄位名稱、型別、長度、選項與可見性的唯一來源；前端不得硬編碼第二份。
- 詳細頁面採一格一欄的 label + value 顯示，不把多個欄位塞進同一格文字。
- 正式異動須考慮 transaction、concurrency、idempotency、audit trail 與權限。
- 不得輸出或提交個資、健康資料、財務資料、密碼、token 或其他秘密資訊。
- ACORD、BPMN、CMMN、DMN、ISO 20022 與 FIBO 只作有來源的參考框架；未確認版本、授權或交易夥伴要求時，不宣稱符合標準。

## 驗證與回報

- 依專案既有方式執行 format、lint、compile、unit test、integration test 與 build。
- 不把編譯成功視為功能完成；關鍵流程需驗證真實資料庫、API 與 UI 行為。
- 清楚列出已執行、未執行及因外部條件無法執行的驗證。
- 保留使用者既有變更，不修改與目前任務無關的檔案。

## 框架技術規範參照

實作前依任務類型載入對應規範（`docs/analysis/framework-standards/`）：

- **例外處理**（任何 API / Service）：`01-exception-handling.md`
  - 業務規則違反 → 422；資源衝突 → 409；驗證失敗 → 400；錯誤碼格式 `{模組}-{四位數字}`
  - 錯誤訊息不得含個資；系統錯誤不得回傳 stack trace

- **交易與併發**（多表寫入 / 覆核 / 樂觀鎖）：`02-transaction-concurrency.md`
  - 多表寫入必須 `@Transactional(REQUIRED)`；正式異動與成功稽核同一交易，失敗嘗試／資安事件才可在 rollback 後獨立寫入
  - Deadlock 最多重試 3 次（`@Retryable`）；樂觀鎖版本不符拋 `ResourceConflictException`
  - 防重須鎖定穩定 lock row 並以唯一鍵保證；不得假設 MySQL 支援 partial unique index

- **API 版本控管**（新增或修改 API）：`03-api-versioning.md`
  - 路徑格式 `/api/v{N}/`；改欄位名稱或移除欄位才需要新版本
  - 棄用期最短 3 個月，回應加 `Deprecation` Header

- **分頁與排序**（列表查詢）：`04-pagination-sorting.md`
  - 參數：`page`（從 1 起）、`pageSize`（上限 100）
  - 回傳 `PageResult<T>` 含 `totalItems`、`totalPages`
  - 排序欄位白名單驗證，禁止直接拼入 ORDER BY

- **稽核 Log**（POST / PUT / DELETE / 覆核 / 個資查詢）：`05-audit-log.md`
  - 寫入、覆核與敏感資料查詢寫入模組定義的 append-only 稽核事件；保存期限由法遵核定來源決定
  - Log 禁止輸出：身分證號、銀行帳號、健康告知內容、密碼、token
  - 每個請求必須有 `requestId`（MDC 注入）

- **前端狀態管理**（Vue 3 / Pinia / API client）：`06-frontend-state.md`
  - API 呼叫統一透過 `src/features/<feature>/api/` typed 函式；跨功能 HTTP client 放 `src/shared/api/`，禁止 component 直接使用 fetch／axios
  - Store 統一管理 loading / error / data；代碼定義 lazy 快取，登出清除
  - 授權以後端回傳的 `functionCode` 清單控制，不寫死前端判斷邏輯

- **測試策略**（任何測試撰寫）：`07-testing-strategy.md`
  - 覆蓋率：保費計算 ≥ 90%，業務驗證 ≥ 85%，一般 Service ≥ 70%
  - Mapper 測試用 Testcontainers MySQL，不用 H2
  - 測試資料全部虛構，禁止複製正式個資

# 保險專案代理規範

## 溝通語言

- 所有說明、文件與使用者可見文字使用繁體中文。
- 程式碼識別字、指令、檔名與既有專有名詞維持原文。
- 保險名詞優先使用臺灣常見用語，例如「要保人、被保險人、受益人、保單、核保、理賠、契約變更」。
- 首次出現可能混淆的中英文保險名詞時，標示對應英文或正式定義。

## 工作方式

1. 先讀取本檔、根目錄 `README.md`，以及任務涉及的 `SKILL.md`。
2. 先盤點現況與限制，再提出或執行變更；不得假設尚未選定的技術棧。
3. 變更應形成可驗證的完整切片，包含必要的文件、設定、實作與檢查。
4. 保留使用者既有變更，不修改與任務無關的檔案。
5. 完成前執行與風險相稱的驗證，並清楚回報已驗證與未驗證項目。

## 分析與實作邊界

- 分析、規格、影響評估與測試設計預設為唯讀，不直接修改產品程式。
- 只有使用者明確要求實作後，才可修改程式、設定、資料庫 migration 或部署檔。
- 舊系統分析依序使用 `legacy-code-explainer → business-rule-extractor → spec-generator → impact-analysis → test-case-generator`。
- 沒有舊程式的新需求先使用 `insurance-requirement-modeler`，再依需要進入 `spec-generator → impact-analysis → test-case-generator`。
- 每一階段先產出可檢查的證據文件，前一階段未釐清的推測不得變成下一階段的確定需求。
- 分析產物放在 `docs/analysis/<domain>/<feature>/`；`domain` 與 `feature` 必須依實際任務命名，不使用固定示例名稱。

## GitHub Copilot

- GitHub Copilot 必須先讀取 `.github/copilot-instructions.md` 與本檔。
- Copilot 的解說、規劃、commit 建議、PR 摘要及程式碼註解使用繁體中文。
- 產生程式碼前先確認需求、領域名詞與跨層契約；不得只完成單一技術層。
- 不確定的保險規則、代碼或法規要標示假設並要求確認，不得自行杜撰。

## 保險系統原則

- 開案或保險功能設計時，使用 `skills/start-insurance-project/SKILL.md`。
- 預設技術棧為 Java、MyBatis、Docker、Vue 3 與 TypeScript；不得無理由替換 ORM 或前端框架。
- 新專案預設沿用既有 POS 專案基準：Spring Boot 3、Java 17、Maven Wrapper、MyBatis、MySQL、Flyway、Vue 3、TypeScript、Pinia、Vite 與 Docker Compose；建立時再確認版本是否需要升級。
- 先定義資料庫、Entity、Create/Update Request、Query Response、API JSON、前端型別與 UI metadata 的一致契約。
- 要保人、被保險人、受益人、保單、保障、保費、核保、理賠與契約變更必須是清楚分離的概念。
- 金額、日期、時區、保單效期、狀態生效日與異動歷程必須明確，不使用浮點數儲存金額。
- 動態業務代碼由後端或資料庫管理；前端不得另建一份相同代碼及翻譯。
- 個資、健康資料、財務資料、身分證件與憑證不得寫入 log、範例資料或版本控制。
- 法規、商品條款與核保規則依適用地區及正式來源確認；未提供時不得猜測。
- 所有業務應用 API（成功、錯誤、刪除及未分類例外）都使用 `ResponseBodyDto<T>` 統一包裝；health、OpenAPI、檔案串流、外部 webhook 或框架協定端點只有在明確 allowlist 中才可例外。
- 保全／契約變更案件的受理中狀態維持小寫正式代碼 `p`，顯示為「受理中」；不得套用到未確認代碼契約的核保、理賠或新契約系統。
- 動態欄位顯示使用後端繁中 metadata；明細畫面採「一格一個欄位」的 label + value 呈現。

## 證據與權威來源

- 使用 `Confirmed`、`Inferred`、`Unknown` 標示舊程式與業務規則的證據等級。
- 只有 `Confirmed` 可以直接描述目前實作行為；`Inferred` 與 `Unknown` 必須列入待確認事項。
- 正式核准需求／商品規則代表預期行為；實際程式與 SQL 代表目前行為；真實 schema／constraint 代表目前資料契約；測試代表既有驗證意圖。
- README、註解與 SVN log 只作補充證據，不單獨裁決。
- 來源矛盾時分開記錄預期行為、目前行為與 gap，不自行選一邊覆蓋。
- 規格內容使用繁體中文；原始 class、method、column、API field 保留原名；追蹤 ID 使用 `BR-001`、`FLD-001`、`API-001`、`TC-001` 等穩定格式。

## 業務建模框架

- 使用 ACORD 作為保險能力、詞彙與交換訊息的參考來源，不宣稱相容，也不複製需要會員或授權的內容。
- 使用 BPMN 表達可預先定義的固定流程，使用 CMMN 表達順序會依案件資料與人工判斷改變的核保、理賠或覆核案件，使用 DMN 表達資格、費率、核保及給付決策。
- ISO 20022 只用於實際涉及收款、扣款、退款或給付的金融訊息介接，不作為整個保險核心資料模型。
- FIBO 只用於詞彙、資料字典與語意對照，不直接取代既有 MySQL 關聯式模型。
- 採用任何外部標準前確認適用版本、授權、地區實務與交易夥伴要求，並記錄 mapping 與偏離原因。

## Skill 設計規範

- 專案 skills 統一放在 `skills/<skill-name>/`。
- 每個 skill 必須包含 `SKILL.md` 與 `agents/openai.yaml`。
- skill 名稱只使用小寫英文字母、數字與連字號，並盡量以動詞開頭。
- `SKILL.md` frontmatter 只放 `name` 與 `description`；description 必須同時描述能力與觸發情境。
- 核心流程留在 `SKILL.md`，大量細節放到一層深度的 `references/`。
- 只有重複且需要確定性的操作才新增 `scripts/`；新增後必須實際執行測試。
- skill 內不建立額外 README、安裝指南或變更紀錄，避免多個規範來源。
- 新增或重新設計 skill 後，同步更新根目錄 `README.md` 的技能清單。
- 完成前使用 skill-creator 的 `quick_validate.py` 驗證每個 skill。

## 文件維護

- README 說明專案目的、目錄結構、現有 skills 與驗證方式。
- 內容與實際檔案不一致時，以修正兩者使其一致為目標，不保留失效說明。

## 框架技術規範

以下文件構成跨專案技術基準。先讀 `00-development-readiness.md`，再依任務載入適用規範；未涉及的領域不必機械套用。規範檔位於 `docs/analysis/framework-standards/`：

| 規範檔 | 必讀時機 |
|---|---|
| `00-development-readiness.md` | 開案、實作前檢查及交付判定 |
| `01-exception-handling.md` | 任何 API 或 Service 實作 |
| `02-transaction-concurrency.md` | 任何多表寫入、覆核、樂觀鎖操作 |
| `03-api-versioning.md` | 新增或修改 API 端點 |
| `04-pagination-sorting.md` | 任何列表查詢 API 與前端查詢頁面 |
| `05-audit-log.md` | 任何新增、修改、刪除、覆核、個資查詢操作 |
| `06-frontend-state.md` | 任何 Vue 3 / Pinia store / API client 實作 |
| `07-testing-strategy.md` | 任何測試撰寫或 CI 設定 |

### 核心強制規則（不得違反）

- 例外分層：業務規則 422、資源衝突 409、驗證失敗 400、不存在 404、系統錯誤 500；錯誤碼格式 `{模組}-{四位數字}`（如 `CHG-3001`）。
- 交易邊界：多表寫入必須 `@Transactional`；正式業務異動與其成功稽核同一交易，失敗嘗試／資安存取事件才可在 rollback 後以獨立交易記錄；Deadlock 最多重試 3 次。
- 防重：Maker 建立前鎖定穩定的 lock row，並以資料庫唯一鍵保證同 `functionCode + uniqueKey` 只有一筆待審鎖；不得假設 MySQL 支援 partial unique index。
- API 版本：路徑格式 `/api/v{N}/`；舊版本棄用期最短 3 個月；棄用時加 `Deprecation` Header。
- 分頁：`page`（從 1 起）、`pageSize`（上限 100）；回傳 `PageResult<T>`（含 `totalItems`、`totalPages`）；排序欄位必須白名單驗證，禁止直接拼 ORDER BY。
- 稽核：寫入、覆核狀態變更與敏感資料查詢必須寫入模組所定義的 append-only 稽核事件；保存期限須由適用地區、資料分類及法遵核准來源決定。Log 禁止輸出身分證號、銀行帳號、健康告知原始值。
- 前端：API 呼叫統一透過 `src/api/` typed 函式；Store 操作統一管理 loading / error / data 三狀態；代碼定義 lazy 快取、登出必須清除。
- 測試：保費計算 Service 覆蓋率 ≥ 90%；業務驗證 Service ≥ 85%；一般 Service ≥ 70%；Mapper 測試使用 Testcontainers MySQL，不使用 H2；測試資料禁止使用正式個資。
- 三層架構：Controller 只接收 Request DTO 與回傳 `ResponseBodyDto<Response DTO>`；Service 負責業務規則與交易；DAO 負責 Entity／持久化模型。Entity 不得成為 API contract，也不得跨過 Controller 邊界。
- SQL 不直接控制：所有 SQL 必須定義在 MyBatis XML Mapper 或標注中；Service 與 Controller 不得含任何 SQL 片段或 JDBC 直接呼叫；動態條件一律用 MyBatis 標籤與 `#{}` 參數綁定。
- 工具類獨立管理：只有無狀態、無領域決策的純技術共用邏輯才放入 `util/`；保費、資格、狀態等規則留在 domain／service。工具類不得注入 Bean 或呼叫資料庫。
- 前端元件共用：分頁列、排序標頭、表單欄位、狀態標籤、確認 Modal 等相同職責的 UI 片段統一放 `src/components/shared/`；業務頁面只透過 props/slots 使用，新增功能前先確認是否已有可複用元件。

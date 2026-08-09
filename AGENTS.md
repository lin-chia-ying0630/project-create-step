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
6. 建立、修改或 review 程式時讀取 `skills/enforce-code-writing-standards/SKILL.md`，套用 Java、MyBatis XML、Vue、TypeScript、SCSS、設計模式、測試與文件撰寫規則；使用設計模式前必須指出實際變化點，不得為模式而抽象。
7. 新增抽象、擴充核保規則、整合外部系統、設計狀態轉換或重構複雜條件時，使用 `skills/design-pattern-guide/SKILL.md` 完成模式選用與 review。

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
- 業務主檔的封閉類別與狀態優先保存一碼代碼，完整英文識別與繁體中文說明集中於 `new_contract.code_definition`，前後端以該定義驗證與顯示。ISO／交易夥伴標準代碼（例如國家 `TW`、幣別 `TWD`）及已核准的核保兩碼階段碼維持原碼，但輸入畫面仍須使用資料庫代碼定義的「代碼｜中文」下拉選單。
- 個資、健康資料、財務資料、身分證件與憑證不得寫入 log、範例資料或版本控制。
- 法規、商品條款與核保規則依適用地區及正式來源確認；未提供時不得猜測。
- 所有業務應用 API（成功、錯誤、刪除及未分類例外）都使用 `ResponseBodyDto<T>` 統一包裝；health、OpenAPI、檔案串流、外部 webhook 或框架協定端點只有在明確 allowlist 中才可例外。
- 保全／契約變更案件的受理中狀態維持小寫正式代碼 `p`，顯示為「受理中」；不得套用到未確認代碼契約的核保、理賠或新契約系統。
- 動態欄位顯示使用後端繁中 metadata；明細畫面採「一格一個欄位」的 label + value 呈現。
- 所有業務清單與明細畫面固定顯示新增人員／時間、最後修改人員／時間、覆核人員／時間；尚未覆核時顯示「尚未覆核」，不得隱藏欄位或由前端推測。
- 所有正式業務資料表 DDL 固定包含 `created_by`、`created_at`、`updated_by`、`updated_at`、`reviewer_id`、`reviewed_at`；前四欄不得為 NULL，尚未覆核時後兩欄可為 NULL，覆核核准套用異動時必須與正式資料同交易寫入。

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

## MyBatis 三層架構與目錄

- 新增、重構或檢查後端功能時，必須讀取 `skills/enforce-mybatis-three-layer/SKILL.md`。
- 採用 package-by-feature；每個業務功能固定放在 `<feature>/controller`、`dto`、`domain`（需要時）、`service`、`service/impl`、`persistence`。
- 依賴方向固定為 `Controller → Service interface ← ServiceImpl → Mapper → MySQL`。Controller 不得注入 Mapper；Mapper 不得依賴 Service 或 HTTP DTO。
- `service/` 只放 interface，具體 Spring `@Service` class 只放 `service/impl/`；多表寫入的 `@Transactional` 放在 implementation 的公開業務方法。
- 本專案 SQL 一律放在 `src/main/resources/mapper/<feature>/*Mapper.xml`；Java Mapper 只保留 interface method、`@Mapper` 與必要的 `@Param`。任何 Java class 都不得出現 `SELECT`、`INSERT`、`UPDATE`、`DELETE` SQL、JDBC、動態查詢字串或 SQL annotation。
- Mapper XML 的 `namespace` 必須等於 Mapper interface 完整類名，statement `id` 必須等於 method 名；動態條件使用 MyBatis XML 標籤，資料值一律用 `#{}` 綁定，禁止以 `${}` 接收使用者輸入。
- 功能 Java class 不得直接放在 `<feature>/` 根目錄；`common/` 只接受真正跨功能的技術契約，不得成為無法分類檔案的暫存區。
- 固定且封閉的 `code + 繁中說明` 必須定義在所屬 `domain` enum，enum 同時提供 `code`、`description` 與嚴格的 `fromCode`；Controller、Service、SQL、Vue 不得各寫一份 switch 或對照表。可由營運維護的動態代碼才使用資料庫 code table，並由 API 回傳繁中說明。
- 固定錯誤代碼與繁中錯誤訊息必須集中在所屬領域的 `ErrorCode` enum；`BusinessException` 只接受 enum，不得在 throw 處直接填代碼與固定訊息字串。
- 每個 function 必須有能解釋目的的註解。Java public／protected method 使用 Javadoc 說明業務目的、參數、回傳、例外及交易副作用；private method 至少說明規則或轉換目的。Vue／TypeScript function 說明使用者動作、狀態變化及 API 副作用。禁止只把方法名稱翻成中文的無資訊註解；單純 accessor 可由類別或欄位註解涵蓋。
- 完成前執行 `python3 skills/enforce-mybatis-three-layer/scripts/check_layers.py create-api/src/main/java/tw/com/insurance/api` 與 `create-api/mvnw test`。

### 核心強制規則（不得違反）

- 例外分層：業務規則 422、資源衝突 409、驗證失敗 400、不存在 404、系統錯誤 500；錯誤碼格式 `{模組}-{四位數字}`（如 `CHG-3001`）。
- 交易邊界：多表寫入必須 `@Transactional`；正式業務異動與其成功稽核同一交易，失敗嘗試／資安存取事件才可在 rollback 後以獨立交易記錄；Deadlock 最多重試 3 次。
- 防重：Maker 建立前鎖定穩定的 lock row，並以資料庫唯一鍵保證同 `functionCode + uniqueKey` 只有一筆待審鎖；不得假設 MySQL 支援 partial unique index。
- API 版本：路徑格式 `/api/v{N}/`；舊版本棄用期最短 3 個月；棄用時加 `Deprecation` Header。
- 分頁：`page`（從 1 起）、`pageSize`（上限 100）；回傳 `PageResult<T>`（含 `totalItems`、`totalPages`）；排序欄位必須白名單驗證，禁止直接拼 ORDER BY。
- 稽核：寫入、覆核狀態變更與敏感資料查詢必須寫入模組所定義的 append-only 稽核事件；保存期限須由適用地區、資料分類及法遵核准來源決定。Log 禁止輸出身分證號、銀行帳號、健康告知原始值。
- 前端：API 呼叫統一透過 `src/features/<feature>/api/` typed 函式；跨功能 HTTP client 才放 `src/shared/api/`。Store 操作統一管理 loading / error / data 三狀態；代碼定義 lazy 快取、登出必須清除。
- 測試：保費計算 Service 覆蓋率 ≥ 90%；業務驗證 Service ≥ 85%；一般 Service ≥ 70%；Mapper 測試使用 Testcontainers MySQL，不使用 H2；測試資料禁止使用正式個資。
- 三層架構：Controller 只接收 Request DTO 與回傳 `ResponseBodyDto<Response DTO>`；Service 負責業務規則與交易；DAO 負責 Entity／持久化模型。Entity 不得成為 API contract，也不得跨過 Controller 邊界。
- SQL 不直接控制：所有 SQL 只能定義在 `src/main/resources/mapper/<feature>/*Mapper.xml`；Java Mapper 禁止 `@Select`、`@Insert`、`@Update`、`@Delete`。Service、Controller、util 不得含 SQL 或 JDBC；動態條件使用 MyBatis XML 標籤與 `#{}`，禁止使用者輸入 `${}`。
- 工具類獨立管理：只有無狀態、無領域決策的純技術邏輯才放入 `util/`；單一功能使用者放 `<feature>/util/`，確實跨功能者才放 `common/util/`。工具類不得注入 Bean、呼叫資料庫或決定核保、保費、資格、狀態等業務內容。
- 後端共用邏輯：兩個以上功能重複使用的純技術方法、資料轉換、分頁／排序驗證、API 包裝與稽核欄位處理，必須提取至職責明確的 `common/` 契約或共用元件；領域狀態轉換與業務決策仍留在所屬 feature，不得以共用為名混入萬用 Service 或 util。
- 前端元件共用：分頁列、排序標頭、表單欄位、狀態標籤、確認 Modal 等相同職責的 UI 片段統一放 `create-web/src/shared/components/`；業務頁面只透過 props/slots 使用，新增功能前先確認是否已有可複用元件。
- 前端視覺共用：表格、按鈕、表單、分頁、狀態訊息、色彩與間距等跨頁視覺契約統一放 `create-web/src/shared/styles/style.scss`；feature 不得重複定義相同用途的樣式。
- 清單列顯示：所有資料清單固定一筆資料一列，表頭與儲存格預設不得自動換行；可用寬度不足時，只能由表格外層容器提供水平捲軸，不得讓整個頁面水平溢出，也不得為了塞入畫面而把同一筆資料拆成多列。
- 查詢清單版型：同時具有「查詢條件」與「查詢結果清單」的頁面，統一使用 `create-web/src/shared/components/QueryListPanels.vue`；上方查詢 Panel 與下方清單 Panel 必須是頁面內容層的同層區塊，不得互相巢狀，也不得將其中一區放在 Panel 外。標題列、邊框、間距、表格捲軸及分頁位置沿用共用樣式。
- 查詢欄位契約：查詢 Panel 內統一使用 `create-web/src/shared/components/QueryConditionForm.vue` 控制標題、說明、欄位列寬度與間距；只有一個文字查詢條件時再使用其封裝元件 `SingleQueryForm.vue`。單一選填條件不得顯示序號或必填 `＊`，留白代表查詢全部資料。
- 查詢 KEY 說明：共用查詢元件只統一版型，不得以模糊泛用文案取代業務條件；每頁必須明列可輸入的完整 KEY（如客戶 ID、要保書號碼、正式保單號碼或照會單號）及留白行為。
- 建立與查詢分離：資料建立表單與資料查詢清單必須使用不同 route 與側邊選單入口；同一 feature 可共用型別與元件，但不得在「建立」頁混入查詢清單。
- 多區段頁籤：登打、覆核等需要切換多個業務區段時，統一使用 `create-web/src/shared/components/SectionTabNavigator.vue` 的「頁次＋功能名稱」卡片頁籤；禁止 feature 自行建立膠囊頁籤或重複樣式，寬度不足時只在頁籤導航內水平捲動。
- 共用優先檢查：新增或修改前後端功能前，必須先搜尋既有共用元件、方法與樣式；發現兩處以上相同職責的實作時，本次變更即應完成提取並補測試，不得留下複製版本等待後續整理。
- 前端響應式設計：所有裝置使用同一份 route、Vue component、DOM、資料契約、API 與業務功能，只依可用 viewport 自動重排；禁止依 user-agent、裝置品牌或型號切換另一套頁面或邏輯。
- 響應式範圍：所有頁面至少支援 320px viewport，不得造成整頁水平捲動；空間不足時多欄表單自動成為單欄，導覽與分頁可在自身容器橫向捲動，寬表格只在表格容器內捲動，主要操作按鈕高度至少 44px。
- 響應式驗收：前端變更完成前，至少以 320×568、390×844 及桌面 viewport 驗證相同內容、欄位、狀態與操作均存在且可用，不得因 viewport 不同隱藏必要功能。
- 瀏覽器渲染入口：`create-web/index.html` 必須包含 UTF-8、`lang="zh-Hant-TW"`、`width=device-width`、`initial-scale=1` 與 `viewport-fit=cover`；共用 layout 使用 `dvh` 與 `env(safe-area-inset-*)` 適應瀏覽器可用區域。

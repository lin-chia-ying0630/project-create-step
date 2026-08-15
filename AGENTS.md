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
- 非保險專案或技術棧尚未決定的一般開案規劃使用 `skills/plan-project-creation/SKILL.md`；不得套用保險領域與既定 Java／MyBatis／Vue 技術要求。
- 每一階段先產出可檢查的證據文件，前一階段未釐清的推測不得變成下一階段的確定需求。
- 分析產物放在 `docs/analysis/<domain>/<feature>/`；`domain` 與 `feature` 必須依實際任務命名，不使用固定示例名稱。

## GitHub Copilot

- GitHub Copilot 必須先讀取 `.github/copilot-instructions.md` 與本檔。
- Copilot 的解說、規劃、commit 建議、PR 摘要及程式碼註解使用繁體中文。
- 產生程式碼前先確認需求、領域名詞與跨層契約；不得只完成單一技術層。
- 不確定的規則、代碼或法規要標示假設並要求確認，不得自行杜撰。

## 專案通用原則

- 先依實際專案現況確認技術棧、模組名稱、資料契約與驗證入口，不因範例或其他專案假設存在未核實的 class、API、資料表、Store、畫面或部署設定。
- 任何需求、分析或修改都應先區分 `Confirmed`、`Inferred`、`Unknown`，避免將推測當成現況。
- 新增或修改功能時，先確認是否已有同職責的共用元件/方法/型別；只有在實際有兩個以上用途相同且需要共用時，才抽取 shared/common。
- 任何正式異動都必須評估 transaction、concurrency、idempotency、audit trail 與權限，不以單一 layer 的假設取代整體風險評估。
- 不得輸出或提交個資、健康資料、財務資料、密碼、token 或其他秘密資訊。

## 技術棧條件化原則

- 若專案實際採用 Java / Spring / MyBatis，請遵循 package-by-feature 的三層架構，維持 Controller → Service → Mapper 的依賴方向與明確資料契約。
- 若專案實際採用 Vue 3 / TypeScript，前端透過 typed API client 與 store 管理狀態，UI component 只負責呈現與互動，不直接決策正式業務規則。
- 若專案實際使用 MyBatis XML，則 SQL 需放在對應 mapper XML，並保留 `namespace`、statement `id` 與 `#{}` 的安全綁定規則。
- 若專案不是上述技術棧，則以實際技術棧為準，不保留互相衝突的框架要求。

## 證據與權威來源

- 使用 `Confirmed`、`Inferred`、`Unknown` 標示舊程式與業務規則的證據等級。
- 只有 `Confirmed` 可以直接描述目前實作行為；`Inferred` 與 `Unknown` 必須列入待確認事項。
- 正式核准需求代表預期行為；實際程式與 SQL 代表目前行為；真實 schema／constraint 代表目前資料契約；測試代表既有驗證意圖。
- README、註解與 SVN log 只作補充證據，不單獨裁決。
- 來源矛盾時分開記錄預期行為、目前行為與 gap，不自行選一邊覆蓋。
- 規格內容使用繁體中文；原始 class、method、column、API field 保留原名；追蹤 ID 使用 `BR-001`、`FLD-001`、`API-001`、`TC-001` 等穩定格式。

## 業務建模框架

- 若專案為保險或金融領域，可參考 ACORD、BPMN、CMMN、DMN、FIBO 等標準，但只在有明確來源、版本與授權的前提下引用。
- ISO 20022 只在涉及收款、扣款、退款或給付等金融訊息介接時使用，不作為整個核心資料模型的唯一來源。
- 採用任何外部標準前確認適用版本、授權、地區實務與交易夥伴要求，並記錄 mapping 與偏離原因。

## 專案搬移準則

- 本文件是治理模板，不是某一個現有專案的產品快照。
- 搬到下一個專案時，必須先刪除本專案的具體功能、API path、資料表、狀態碼、部署 URL、畫面名稱與產品文案；僅保留通用治理規則與工作流。
- 新專案若不屬於保險領域，就不保留保險特定 Skill；若不是 Java/MyBatis 或 Vue 3，就不保留該技術棧的強制要求。
- 使用明確佔位符（如 `<feature>`、`<resource>`、`<backend-module>`）描述未來將填入的內容，不要假設已存在。
- 任何被搬移的 Skill、規範與產物，應依新專案實際現況重新核對與收斂，不得無條件沿用。

## Skill 設計規範

- 專案 skills 統一放在 `skills/<skill-name>/`。
- `skills/` 是本專案 Skill 的唯一維護來源；需要 Codex repository 自動探索時，才由 `.agents/skills/<skill-name>` 建立指向來源目錄的 symlink，不得複製第二份內容造成漂移。
- 每個 skill 必須包含 `SKILL.md` 與 `agents/openai.yaml`。
- skill 名稱只使用小寫英文字母、數字與連字號，並盡量以動詞開頭。
- 每個 skill 只負責一個可辨識工作；跨任務協調留在根目錄 `WORKFLOWS.md`，不得建立與既有單一能力重複的組合 skill。
- `SKILL.md` frontmatter 只放 `name` 與 `description`；description 先寫核心能力，再寫適用觸發，容易與其他 skill 重疊時必須補上不適用邊界。
- Skill 步驟使用命令式文字，明確寫出前置條件、輸入、輸出、停止條件及驗證證據；不得只有抽象原則而沒有可判定的完成方式。
- 核心流程留在 `SKILL.md`，大量細節放到一層深度的 `references/`。
- 只有重複且需要確定性的操作才新增 `scripts/`；新增後必須實際執行測試。
- 新增或大幅修改 skill 時至少驗證正向觸發、不得觸發及與鄰近 skill 重疊三種提示；不能只驗證 Markdown 格式。
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
- 完成前先從既有 Java `package` 宣告確認 `<java-package-root>`，再執行 `python3 skills/enforce-mybatis-three-layer/scripts/check_layers.py <backend-module>/src/main/java/<java-package-root>` 與 `<backend-module>/mvnw test`；不得把佔位符當成實際路徑。

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
- 前端元件共用：分頁列、排序標頭、表單欄位、狀態標籤、確認 Modal 等相同職責的 UI 片段統一放 `<frontend-module>/src/shared/components/`；業務頁面只透過 props/slots 使用，新增功能前先確認是否已有可複用元件。
- 代碼欄位共用：資料庫代碼下拉統一使用 `CodeDefinitionSelect.vue`，一般欄位外框使用 `FormField.vue`；元件只負責 label、必填標示與「代碼｜中文」顯示，業務驗證仍由所屬 Service 負責。
- 前端視覺共用：表格、按鈕、表單、分頁、狀態訊息、色彩與間距等跨頁視覺契約統一放 `<frontend-module>/src/shared/styles/style.scss`；feature 不得重複定義相同用途的樣式。
- 清單列顯示：所有資料清單固定一筆資料一列，表頭與儲存格預設不得自動換行；可用寬度不足時，只能由表格外層容器提供水平捲軸，不得讓整個頁面水平溢出，也不得為了塞入畫面而把同一筆資料拆成多列。
- 查詢清單版型：同時具有「查詢條件」與「查詢結果清單」的頁面，統一使用 `<frontend-module>/src/shared/components/QueryListPanels.vue`；上方查詢 Panel 與下方清單 Panel 必須是頁面內容層的同層區塊，不得互相巢狀，也不得將其中一區放在 Panel 外。標題列、邊框、間距、表格捲軸及分頁位置沿用共用樣式。
- 查詢欄位契約：查詢 Panel 內統一使用 `<frontend-module>/src/shared/components/QueryConditionForm.vue` 控制標題、說明、欄位列寬度與間距；只有一個文字查詢條件時再使用其封裝元件 `SingleQueryForm.vue`。單一選填條件不得顯示序號或必填 `＊`，留白代表查詢全部資料。
- 查詢 KEY 說明：共用查詢元件只統一版型，不得以模糊泛用文案取代業務條件；每頁必須明列可輸入的完整 KEY（如客戶 ID、要保書號碼、正式保單號碼或照會單號）及留白行為。
- 建立與查詢分離：資料建立表單與資料查詢清單必須使用不同 route 與側邊選單入口；同一 feature 可共用型別與元件，但不得在「建立」頁混入查詢清單。
- 多區段頁籤：登打、覆核等需要切換多個業務區段時，統一使用 `<frontend-module>/src/shared/components/SectionTabNavigator.vue` 的「頁次＋功能名稱」卡片頁籤；禁止 feature 自行建立膠囊頁籤或重複樣式，寬度不足時只在頁籤導航內水平捲動。
- 共用優先檢查：新增或修改前後端功能前，必須先搜尋既有共用元件、方法與樣式；發現兩處以上相同職責的實作時，本次變更即應完成提取並補測試，不得留下複製版本等待後續整理。
- 前端響應式設計：所有裝置使用同一份 route、Vue component、DOM、資料契約、API 與業務功能，只依可用 viewport 自動重排；禁止依 user-agent、裝置品牌或型號切換另一套頁面或邏輯。
- 響應式範圍：所有頁面至少支援 320px viewport，不得造成整頁水平捲動；空間不足時多欄表單自動成為單欄，導覽與分頁可在自身容器橫向捲動，寬表格只在表格容器內捲動，主要操作按鈕高度至少 44px。
- 響應式驗收：前端變更完成前，至少以 320×568、390×844 及桌面 viewport 驗證相同內容、欄位、狀態與操作均存在且可用，不得因 viewport 不同隱藏必要功能。
- 瀏覽器渲染入口：`<frontend-module>/index.html` 必須包含 UTF-8、`lang="zh-Hant-TW"`、`width=device-width`、`initial-scale=1` 與 `viewport-fit=cover`；共用 layout 使用 `dvh` 與 `env(safe-area-inset-*)` 適應瀏覽器可用區域。

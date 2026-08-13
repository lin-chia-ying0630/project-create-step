# Insurance System SA Skills

這個 repository 維護一套供 GitHub Copilot／Codex 使用的保險系統 SA（System Analyst）規範與 Skills。預設使用繁體中文，技術基準為 Spring Boot 3、Java 17、Maven Wrapper、MyBatis、MySQL、Flyway、Docker Compose、Vue 3、TypeScript、Pinia 與 Vite。

同一目錄已拆成三個可獨立建置的交付包：`create-api`（同步 API 與 Flyway）、`create-batch`（批次與對帳）及 `create-web`（Vue 3 前端）。責任與開發 Gate 見 `docs/analysis/framework-standards/00-development-readiness.md`。

規格參考既有 `pos-project` 的分層、`ResponseBodyDto<T>`、OpenAPI、繁中 metadata、MyBatis/Flyway、Docker health check 及前端驗證習慣；建立新專案時重新確認相容的 patch 版本與供應鏈安全性。

後端採 package-by-feature 三層架構，SQL 統一放在 `create-api/src/main/resources/mapper/<feature>/*Mapper.xml`；Java Mapper 只保留 method contract。固定代碼、繁中說明與固定錯誤訊息集中於領域 enum，不得在各層重複硬編碼。

## 規範分工

| 文件 | 責任 |
|---|---|
| `AGENTS.md` | 全專案權威規範：語言、證據、技術契約、分析／實作邊界及 Skill 設計。 |
| `.github/copilot-instructions.md` | GitHub Copilot 每次工作必須立即遵守的精簡指令。 |
| `skills/start-insurance-project/SKILL.md` | 保險開案及能力路由，不重複各專項 Skill 的流程。 |
| `skills/enforce-mybatis-three-layer/SKILL.md` | MyBatis 功能分層、依賴方向、SQL 與交易邊界的強制規則及檢查。 |
| `skills/enforce-code-writing-standards/SKILL.md` | Java、MyBatis XML、Vue、TypeScript、SCSS、設計模式、測試及文件撰寫標準與掃描。 |
| `skills/design-pattern-guide/SKILL.md` | 依真實變化點選擇、套用與 review Java、Spring、MyBatis、Batch、Vue 及保險領域設計模式。 |
| `skills/<name>/SKILL.md` | 單一可重複執行的工作能力與輸出格式。 |
| `skills/start-insurance-project/references/` | 保險領域、技術棧、既有專案慣例與分析產物規格。 |

## 目錄結構

```text
.
├── AGENTS.md
├── README.md
├── compose.yaml
├── create-api/
├── create-batch/
├── create-web/
├── .github/
│   └── copilot-instructions.md
└── skills/
    ├── start-insurance-project/
    │   ├── SKILL.md
    │   ├── agents/openai.yaml
    │   └── references/
    │       ├── analysis-artifacts.md
    │       ├── insurance-domain.md
    │       ├── java-mybatis-vue-stack.md
    │       └── project-conventions.md
    ├── enforce-mybatis-three-layer/
    │   ├── SKILL.md
    │   ├── agents/openai.yaml
    │   ├── references/package-layout.md
    │   └── scripts/check_layers.py
    ├── enforce-code-writing-standards/
    │   ├── SKILL.md
    │   ├── agents/openai.yaml
    │   ├── references/writing-standards.md
    │   └── scripts/check_writing_standards.py
    ├── design-pattern-guide/
    │   ├── SKILL.md
    │   ├── agents/openai.yaml
    │   └── references/pattern-catalog.md
    ├── insurance-requirement-modeler/
    ├── insurance-interface-mapper/
    ├── insurance-reconciliation-analyzer/
    ├── legacy-code-explainer/
    ├── business-rule-extractor/
    ├── spec-generator/
    ├── impact-analysis/
    ├── test-case-generator/
    ├── java-code-analysis/
    ├── sql-analyzer/
    ├── java-refactor/
    ├── liberty-debugger/
    ├── svn-review/
    └── plan-project-creation/
```

每個 Skill 目錄至少包含 `SKILL.md` 與 `agents/openai.yaml`。

前端程式依 feature 放置：

```text
create-web/src/
├── features/<feature>/
│   ├── api/
│   ├── types/
│   └── views/
└── shared/
    ├── api/
    └── types/
```

只有跨兩個以上 feature 且責任一致的程式才能進入 `shared/`。

表單欄位外框由 `FormField.vue` 共用；資料庫代碼下拉由 `CodeDefinitionSelect.vue` 共用，統一顯示「代碼｜中文」。目前客戶類型、國籍、居住國家、郵遞區號、職業、資金來源、投保目的與保單幣別皆使用相同元件。

「保單登打新增」採十頁條件式登打：受理與通路、契約關係人、投保內容、受益人、健康告知、聲明與簽署、首期保費授權、共同行銷同意、投資風險適合度及附件資料。保險商品下拉由 `insurance_product_definition` 商品定義檔提供；選定商品後由後端決定主／附約、傳統型壽險／投資型、幣別及投保限制，前端不得另行指定商品類型。首期保費授權頁必須直接登打授權書檔名與受控檔案參照，並同步列入附件清單。共同行銷頁只在適用案件啟用且「不同意」不得阻擋投保；投資型案件未完成適合度、風險揭露及文件交付不得送件。

完整銀行帳號或信用卡號只送至 `/api/v1/new-contract/payment-instruments/validate` 做格式檢核與不可逆代碼化；正式要保資料與覆核內容僅保存 Token、遮罩值及驗證狀態，不保存完整號碼、CVV、PIN，也不得輸出至 log。附件資料只保存受控 DMS／物件儲存參照及完整性 metadata，檔案內容不直接存入業務 JSON。

前端導覽由 `create-web/src/router/index.ts` 集中管理，使用 Vue Router history mode。路由名稱、URL、頁面標題及側邊選單項目共用同一份定義；Nginx 以 `try_files ... /index.html` 支援直接開啟及重新整理深層網址。

新契約系統提供 `/code-definitions` 的唯讀「Code Definitions 代碼定義」頁面，依代碼群組與欄位查詢資料庫目前生效的動態代碼及繁體中文說明；畫面不另行維護代碼對照。

`customer-kyc / occupation_code` 採用衛生福利部 TW Core IG 的「臺灣壽險公會傷害保險個人職業分類表」CodeSystem `2023-06-01`，保存 1,324 筆 8 碼代碼、繁體中文名稱、大分類、中分類、工作性質及來源版本。來源未提供英文名稱，因此英文欄位維持 `NULL`，不得自行翻譯；客戶職業選單以「代碼｜繁體中文」顯示。

國籍與居住國家採中華郵政國外郵政國名／地區名中英文對照，保存 169 筆二碼代碼；聯絡地址採中華郵政 368 筆前三碼郵遞區號；保單幣別目前依系統實際支援範圍提供 `TWD｜新臺幣` 與 `USD｜美元`。Code Definitions、客戶建立及保單登打畫面的動態代碼欄位皆以資料庫回傳的「代碼｜中文」下拉選單呈現，英文名稱獨立顯示或保存。

客戶類型主檔只保存一碼：`1｜自然人（PERSON）`、`2｜公司（ORGANIZATION）`，英文識別與中文說明由 `new_contract.code_definition` 管理。國家 `TW` 與幣別 `TWD` 等正式標準代碼不縮碼，但輸入畫面必須使用代碼定義下拉選單顯示中文。

全域視覺樣式集中於 `create-web/src/shared/styles/style.scss`，統一維護色彩、斷點、頁面寬度、查詢列比例、頁籤高度、選單、表單控制項與按鈕的長寬高及狀態訊息；feature-specific 樣式只保留業務專屬版面，不得覆寫共用控制項尺寸或重新定義第二份全域設計 token。登打分頁、客戶類型及覆核分類等卡片式選擇一律使用 `SectionTabNavigator.vue`。

按鈕依動作語意固定使用共用樣式：查詢、送出及排入作業使用青綠實心 `primary-button`；重新整理、查看、清除、取消及頁面導覽使用不填色的青綠外框 `secondary-button`；承保撤回固定使用藍色實心 `reversal-button`；刪除等不可逆危險動作才使用紅色 `danger-button`。feature scoped 樣式不得覆蓋這些語意顏色。

查詢按鈕文字固定為「查詢＋功能名詞」，例如「查詢保單、查詢客戶、查詢照會單」；不得使用「查詢影響、取得資料、載入案件」等非功能名詞 wording。「重新整理」只重新載入目前清單，不代替查詢動作。

前端採裝置無關的 Responsive Web Design：所有裝置使用同一份 route、Vue component、DOM、API 與業務功能，只依 viewport 可用空間自動重排。禁止 user-agent／裝置型號分支或手機專用頁。頁面至少支援 320px viewport，不得產生整頁水平捲動；空間不足時表單改為單欄，導覽、頁籤與寬表格只在自身容器內橫向捲動。交付前以 320×568、390×844 與桌面 viewport 驗證相同功能。

瀏覽器渲染入口由 `create-web/index.html` 統一設定繁中語系、UTF-8、device-width viewport、初始縮放、theme color 與 `viewport-fit=cover`。共用 layout 使用 dynamic viewport height 與 safe-area inset 適應瀏覽器可用區域，同時保留使用者縮放能力。

## 核心 SA 流程

核心流程分階段執行，不自動一路推論到底：

```text
legacy-code-explainer
        ↓
business-rule-extractor
        ↓
spec-generator
        ↓
impact-analysis
        ↓
test-case-generator
```

| Skill | 主要用途 | 預設產物 |
|---|---|---|
| `legacy-code-explainer` | 解讀舊 Java、COBOL、AS400、Batch、Copybook 與 record layout。 | `01-legacy-explanation.md` |
| `business-rule-extractor` | 萃取核保、保單、給付、費率、資格、狀態及 validation 規則。 | `02-business-rules.md` |
| `spec-generator` | 產生繁中流程、欄位、API、狀態、權限與資料異動規格。 | `03-specification.md` |
| `impact-analysis` | 分析 caller、DB、SQL、DTO、UI、JSP、Batch、Report、Interface、測試與部署影響。 | `04-impact-analysis.md` |
| `test-case-generator` | 建立 JUnit、integration、SIT、UAT 與規則追溯矩陣。 | `05-test-cases.md` |

產物依任務實際名稱保存：

```text
docs/analysis/<domain>/<feature>/
├── 01-legacy-explanation.md
├── 02-business-rules.md
├── 03-specification.md
├── 04-impact-analysis.md
└── 05-test-cases.md
```

`<domain>/<feature>` 由實際領域與功能決定，不使用固定範例名稱。不適用的階段可以省略，但必須記錄原因。

## 按需專項能力

| Skill | 使用情境 |
|---|---|
| `java-code-analysis` | 追 Java 執行路徑，定位 Null、ArrayIndex、錯誤分支、transaction 與 exception 根因。 |
| `sql-analyzer` | 分析 SQL、MyBatis mapping、欄位容量、DECIMAL、index、lock、affected rows 與 Flyway。 |
| `enforce-mybatis-three-layer` | 建立、重構或檢查 package-by-feature 的 Controller、Service、Mapper 目錄與依賴。 |
| `design-pattern-guide` | 新增抽象、擴充核保規則、整合外部系統、重構條件分支或 review 架構時，選擇最小可行模式並避免過度設計。 |
| `java-refactor` | 以 characterization test 保護行為後，小步拆解舊 Java 與複雜 if/else。 |
| `liberty-debugger` | 診斷 Liberty、WAR、JNDI、Servlet、classloading、TLS 與 datasource。 |
| `svn-review` | 唯讀比對 SVN revision、branch、mergeinfo、release scope 與跨層風險。 |

專項 Skill 只在需要時插入核心流程，不要求每次全部執行。

## 保險業務建模能力

| Skill | 使用情境 | 參考框架 |
|---|---|---|
| `insurance-requirement-modeler` | 沒有舊程式的新需求；建立角色、能力、流程、案件、決策、資料與驗收條件。 | ACORD、BPMN、CMMN、DMN、FIBO |
| `insurance-interface-mapper` | 固定長度檔、CSV、XML、JSON、MQ、API、主機、銀行及交易夥伴欄位 mapping。 | ACORD、ISO 20022、內部 canonical model |
| `insurance-reconciliation-analyzer` | 保單、收費、退款、理賠、佣金、銀行及日終批次的筆數金額對帳與重跑。 | Control totals、idempotency、replay／reversal controls |

框架使用原則：

- ACORD 用於保險能力、詞彙及交換訊息對照；未取得正式版本與授權時不宣稱相容。
- BPMN 用於固定流程；CMMN 用於順序會隨案件資料及人工判斷改變的核保、理賠與覆核；DMN 用於資格、費率、核保及給付決策。
- ISO 20022 只在收款、扣款、退款與給付金融訊息介接時使用。
- FIBO 用於詞彙及資料語意，不直接取代 MySQL schema。

## 開案與一般規劃

| Skill | 使用情境 |
|---|---|
| `start-insurance-project` | 啟動或重新設計 Java/MyBatis/Vue/Docker 保險專案，建立領域語言、跨層契約與能力路由。 |
| `plan-project-creation` | 規劃非保險或尚未決定技術棧的一般新專案。 |

不另建立 `legacy-to-spec`、`insurance-code-analyzer` 或 `change-impact-analyzer` 等重複組合 Skill；由開案 Skill 串接單一責任能力。

## 證據規則

- `Confirmed`：程式、SQL、設定、runtime evidence 或正式來源直接證明。
- `Inferred`：流程高度暗示但證據不完整。
- `Unknown`：缺少被呼叫程式、Copybook、資料表、設定或業務決策。

只有 `Confirmed` 可直接描述目前實作行為。正式需求代表預期行為；程式與 SQL 代表目前行為；真實 schema 代表資料契約；測試代表既有驗證意圖。來源矛盾時建立 gap，不自行選一邊覆蓋。

## 分析與實作

分析、規格、影響評估及測試設計預設唯讀。只有使用者明確要求實作後，才進入：

```text
impact-analysis
        ↓
實作計畫
        ↓
java-refactor／功能修改
        ↓
test-case-generator
        ↓
完整驗證
```

## 專案固定契約

- 所有業務應用 API 成功、錯誤、刪除及未分類例外都使用 `ResponseBodyDto<T>`；health、OpenAPI、串流與外部協定只有明確 allowlist 才可例外。
- Database、Entity、Create/Update Request、Query Response、OpenAPI、frontend type 與 UI metadata 維持唯一契約。
- 固定封閉狀態使用正式代碼 enum；資料庫動態代碼維持 `String` 並由後端回傳繁中說明。
- 固定 enum 同時保存 `code` 與繁中 `description`，並提供嚴格 `fromCode`；前端、SQL 與各層不得再維護第二份對照。
- 保全／契約變更的 `p` 顯示為「受理中」；其他保險領域不得未確認就沿用。
- 動態詳細畫面使用後端繁中 metadata，採一格一欄的 label + value 顯示。
- 金額使用 MySQL `DECIMAL`／Java `BigDecimal`；前端不使用浮點數重算正式金額。
- Flyway migration 只向前演進；已發布 migration 與 checksum 不可修改。
- Docker、API、資料庫與 Vue UI 必須做端到端驗證，不以單一 build 成功代替。

## 使用 GitHub Copilot

GitHub Copilot 會使用 `.github/copilot-instructions.md`。可在 Copilot Chat 回答的 References 清單確認是否載入。

建議提示：

```text
請先讀取 AGENTS.md、README.md、.github/copilot-instructions.md，依任務載入對應 SKILL.md。所有分析使用繁體中文，先產出證據文件，不要修改產品程式。
```

開案提示：

```text
使用 $start-insurance-project，以 Java、MyBatis、Docker、Vue 3、TypeScript 規劃保險專案，先建立第一條可驗證的垂直流程。
```

## 新增或更新 Skill

1. 依 `AGENTS.md` 確認能力邊界，避免與既有 Skill 重複。
2. 使用 skill-creator 的 `init_skill.py` 建立標準骨架。
3. 完成 `SKILL.md`、`agents/openai.yaml` 與必要 resources。
4. 同步更新本 README 的目錄與能力清單。
5. 驗證每個 Skill：

```bash
python3 /Users/linjiaying/.codex/skills/.system/skill-creator/scripts/quick_validate.py skills/<skill-name>
```

若執行環境缺少 `PyYAML`，先回報環境限制；不得宣稱官方驗證器已通過。

## 三包建置與啟動

```bash
cd create-api && ./mvnw test
cd create-batch && ./mvnw test
cd create-web && npm ci && npm run build && npm audit --audit-level=high
docker compose up --build mysql api web
```

批次按工作需求執行，不隨常駐服務自動啟動：

```bash
docker compose --profile batch run --rm batch --spring.batch.job.name=<已實作的Job名稱>
```

第一條業務規格已建立為「新契約批次承保作業」：`create-api` 的 Flyway 建立 `new_contract` 要保、核保、照會、規則結果、稽核與 outbox，並以 `main.policy_contract` 作正式保單寫入邊界；`create-batch` 執行基本檢核，有問題建立照會，全部通過才承保。完整規格見 `docs/analysis/new-contract/underwriting/00-requirements.md`。

目前已完成本機開發用的客戶建立、保單登打、首期保險費收款與銷帳、新契約批次承保作業排程與紀錄查詢、核保照會單查詢與 PDF，以及未生效保單承保撤回 API／畫面。客戶身分證、聯絡方式與地址採 AES-GCM 加密，身分證另存不可逆雜湊供查重；姓名只有 `customer.customer_master` 是可維護來源，送件資料只保存 `customer_id` 與不可變快照參考。

本機啟動前複製 `.env.example` 為 `.env`，將 `PII_ENCRYPTION_KEY` 換成至少 24 字元的隨機密鑰；`.env` 已排除版本控制。測試入口為 `http://localhost:5173`，API 為 `http://localhost:8082`，MySQL 為 `127.0.0.1:3308`。

### 網站資料庫 migration

網站目前以 Flyway V17 為升級起點，V18～V37 保留原始內容與 checksum；不得直接修改這些已曾發布的 migration。V38 會清除 V30 建立的 `LOCAL_TEST_DATA / TEST-NC-*` 本機測試案件，V39 則保留目前 `main` 分支後端仍在使用的 `request_status`、`execution_status`、`PERSON` 與 `ORGANIZATION` 契約。

執行 Flyway 前，migration 帳號必須已可存取 `new_contract`、`main` 與 `customer` schema。若 `main` 尚未建立，V2 會回報 `ERROR 1049 Unknown database 'main'`；若 migration 帳號不能執行既有權限設定，V8 會回報 `ERROR 1410 You are not allowed to create a user with GRANT`。託管平台應由管理者先建立 schema 與授權，不得使用一般應用程式帳號臨時執行管理員 SQL。

部署前先備份資料庫並確認 V17 歷程全部成功；部署後執行：

```sql
SELECT installed_rank, version, description, success
  FROM new_contract.flyway_schema_history
 ORDER BY installed_rank DESC
 LIMIT 5;

SELECT COUNT(*) AS local_test_case_count
  FROM new_contract.insurance_application
 WHERE source_system = 'LOCAL_TEST_DATA'
   AND application_no LIKE 'TEST-NC-%';
```

驗收結果必須為最新版本 V39 且 `success = 1`、`local_test_case_count = 0`，並確認 `/actuator/health` 回傳 `UP`。若歷程存在失敗版本，先查明最底層 `SQL State`、`Error Code` 與失敗 statement；不得直接以 Flyway repair 掩蓋部分完成的 MySQL DDL。
### Northflank MySQL 連線

`create-api` 可直接連線 Northflank MySQL Addon，且不需要把資料庫密碼寫入 image、Git 或部署命令。先在 Northflank 建立 MySQL Addon，再以 Secret Group 連結該 Addon，並把 Addon 輸出的 secret 設為下列 alias 後套用至 Spring Boot Service：

| Spring Boot 環境變數 | Northflank Addon secret | 必填 | 用途 |
|---|---|---:|---|
| `MYSQL_JDBC_URI` | JDBC URI | 是 | MySQL JDBC 連線字串；優先於本機 `DB_URL`。 |
| `MYSQL_USERNAME` | Username | 是 | 應用程式與預設 Flyway 帳號。 |
| `MYSQL_PASSWORD` | Password | 是 | 應用程式與預設 Flyway 密碼。 |
| `PII_ENCRYPTION_KEY` | 自訂 Secret | 是 | 至少 24 字元的個資加密密鑰，不得使用本機範例值。 |
| `DB_MIGRATION_USER` | 自訂或 Addon 帳號 | 否 | 只有需要分離 DDL 權限時設定。 |
| `DB_MIGRATION_PASSWORD` | 自訂或 Addon 密碼 | 否 | 與獨立 migration 帳號成對設定。 |
| `DB_POOL_MAX_SIZE` | 自訂值 | 否 | Hikari 最大連線數，預設 `8`，須低於 Addon 方案連線上限。 |

Northflank Service 的容器連接埠維持 `8080`；若平台注入 `PORT`，Spring Boot 也會自動採用。部署完成後以 `/actuator/health` 驗證應用程式及資料庫健康狀態。應用程式優先順序為 `MYSQL_JDBC_URI`、`DB_URL`、本機預設 URL，因此既有 Docker Compose 不需修改。

正式環境只應連線 Addon 的內部 endpoint。只有從本機或外部工具維護資料庫時才使用公開 endpoint，並依 Northflank 要求啟用 TLS；不得把公開連線資訊保存於 repository。

本機畫面包含：

| 功能 | API | 說明 |
|---|---|---|
| 客戶資料建立 | `POST /api/v1/customers` | 單一交易寫入客戶、證件、聯絡、地址、姓名歷程、KYC、同意及稽核。 |
| 保單登打 | `POST /api/v1/new-contract/applications` | 建立要保案件與首期應繳。 |
| 首期保險費收款與銷帳 | `GET .../initial-premium`、`POST .../remittance-slips` | 查詢應收後由「新增送金單」登錄繳費憑證及實收金額並送覆核；核准後才建立送金單、比對應收與實收並決定是否完成銷帳。舊路徑 `POST .../initial-premium-payments/reconcile` 與 `POST .../remittance-slips/match` 暫保留相容。 |
| 核保照會單 | `GET .../underwriting-inquiries/{query}`、`GET .../{query}/pdf` | 依照會單號或要保書號碼顯示核保未通過項目，並產生繁體中文 PDF。測試資料為 `DEMO-INQ-001`。 |
| 新契約批次承保作業 | `POST .../underwriting-batch/requests`、`GET .../executions` | 排入每日 21:00 批次並查詢執行紀錄。 |
| 核保審查作業 | `GET .../underwriting-reviews`、`GET .../{query}`、`POST .../decisions` | 先列出新契約受理檔中 `NS` 照會結束／待核保審查案件；點選後以彈跳視窗檢視資料並將核保結果送覆核。 |
| 承保撤回 | `GET .../preview`、`POST .../policy-reversals` | 僅允許未生效案件，使用版本及確認 token 防止誤刪。 |

上述仍是開發基線，不包含正式商品費率、外部身分驗證、AML／PEP 名單服務、IAM 權限、HSM/KMS 密鑰管理與完整批次物化，因此不可直接上線。

## SSO 授權邊界

`create-api` 的 `/api/**` 只接受 `SSO_ACCESS_TOKEN` HttpOnly Cookie 中的 RS256 JWT。後端會透過 `SSO_JWK_SET_URI` 驗證簽章，並檢查 `SSO_ISSUER`、有效期限及 `aud=NEW_CONTRACT`；未通過一律回傳 `401`。前端啟動及換頁會呼叫 `/api/auth/me`，未登入就返回同一主機的統一入口 `5174`。本機 Compose 已設定 `host.docker.internal` 讀取入口 JWKS。

## 新契約 Maker-Checker 覆核

所有會修改正式資料的操作先建立 `business_review_case`，核准前不呼叫原業務 Service。覆核工作台 `/reviews` 統一處理客戶建立、保單登打、承保撤回、新契約批次承保作業及首期保費資料；保單號碼編發也視為保單登打類別的資料異動。

新契約批次承保作業畫面以「要保書號碼或正式保單號碼＋執行日」送覆核；核准後寫入 `underwriting_batch_request.requested_business_date`，排程請求狀態先記為 `W`（等待）。排程器每日 21:00（Asia/Taipei）啟動，只原子領取要保案件狀態為 `PW`、排程請求狀態為 `W`，且 `requested_business_date = 當日臺北日期` 的案件；領件後排程狀態改為 `P`（處理中）。案件必須已有要保人與被保險人、基本金額及日期有效、已於案件建立時固定正式保單號碼，且首期保險費已銷帳；全部通過時狀態改為 `S`（完成）並寫入正式保單主檔，任一條件未通過則改為 `R`（照會／退件）。過去的執行日不得新增，以免留下不會再被排程領取的資料。

新契約契約狀態包含：受理時資料庫值為 `NULL`，承保並生效後為 `01`（有效），人工核保負向決行為 `13`（拒保）、`14`（延期）或 `15`（取消），保單送達後十天猶豫期內的變更為 `26`（猶豫期變更）。`NS` 固定表示新契約受理檔的「照會結束／待核保審查」，核保審查清單只列出受理檔與核保案件皆為 `NS` 的資料；`DC` 拒絕承保映射為階段 `RS`／契約狀態 `13`，`PO` 延期承保映射為 `DS`／`14`，`CN` 取消申請映射為 `CS`／`15`。`26` 屬新契約狀態，但不是核保結果，因此不列入核保審查結果選單。

核保審查結果並非只有不承保。畫面依後端正式對照分成兩組：`SA` 標準承保、`RA` 加費承保、`EA` 除外承保、`CA` 條件承保與 `PA` 部分承保會往下進入承保流程，核保階段為 `AS`、契約狀態為 `01`；`PO` 延期、`DC` 拒保與 `CN` 取消不會進入承保流程。畫面不自行保存代碼表，而是由 `/underwriting-reviews/outcomes` 取得完整選項及繁中說明。

`business_review_case.review_status` 使用單碼：`P` 待覆核／處理中、`A` 覆核核准、`R` 覆核退回。只有 `A`、`R` 必須具備覆核人與覆核時間；`P` 不得先填覆核資訊。

所有正式業務表 DDL、查詢 API、業務清單與明細固定包含新增人員／時間、最後修改人員／時間、覆核人員／時間；欄位為 `created_by／created_at／updated_by／updated_at／reviewer_id／reviewed_at`，尚未覆核時後兩欄為 `NULL` 且畫面顯示「尚未覆核」。

審核及業務查詢清單的操作欄固定置於第一欄；其後前三個資料欄位使用共用可排序表頭，預設第一個資料欄位升冪。每筆資料固定顯示為一列且欄位不換行，欄位超過可用寬度時只在表格容器顯示水平捲軸。共用分頁元件預設每頁 10 筆，可選 10／20／50／100 筆；切換頁碼、筆數或排序都由後端重新查詢，不得對分頁後的前端資料再次排序。此契約套用於覆核工作台、核保審查、保單資料查詢、客戶建立、承保撤回及核保照會單查詢。覆核工作台可用完整覆核編號、客戶 ID、要保書號碼、正式保單號碼或業務鍵查詢待覆核案件；客戶 ID 透過新契約關係人檔關聯，不解密或掃描覆核 payload。

具有查詢條件與結果清單的頁面統一使用 `QueryListPanels.vue`：查詢 Panel 與清單 Panel 固定為同層的兩個區塊，兩區都不得放在 Panel 外或互相包覆。

查詢 Panel 內統一使用 `QueryConditionForm.vue` 控制「查詢條件」標題、說明、欄位列寬度與間距；只有一個文字查詢條件時使用其封裝元件 `SingleQueryForm.vue`。單一選填條件不顯示序號或必填符號，留白時查詢完整清單。

所有查詢清單預設每頁 10 筆，使用共用 `PageNavigator.vue` 提供 10、20、50、100 筆選項；只有 API 本身回傳完整靜態代碼集合的代碼定義頁可在前端切頁，後端已分頁的清單不得再次切片。

資料建立與資料查詢使用不同 route 與側邊選單入口；例如 `/customers/new` 只顯示客戶建立表單，`/customers` 只顯示客戶查詢清單。

保單登打與覆核工作台的多區段導航統一使用 `SectionTabNavigator.vue`，採「頁次＋功能名稱」卡片頁籤；窄畫面由頁籤導航本身水平捲動。

承保撤回清單只列契約狀態 `01`（有效）的正式保單。送出後先建立 Maker-Checker 覆核案件；核准時保留正式保單主檔及其關聯資料，只將 `underwriting_case.contract_status_code` 由 `01` 更新為 `NULL`，並記錄覆核人、覆核時間與 append-only 撤回稽核，不再刪除正式保單或重設要保／核保階段。

V30 建立 `TEST-NC-0001`～`TEST-NC-0100` 與對應的 `TEST-UW-0001`～`TEST-UW-0100` 虛構案件，平均分布於 `AP／PW／NP／NW／NR／UW／US／NS／AS／RS／DS／CS／PS`，每個階段至少 7 筆，僅供本機及整合測試。

資料庫 migration 以英文 table／column 識別字維持跨系統契約，並以繁中 `COMMENT` 定義業務名稱。本次首期保費、送金單、銷帳、新契約批次承保作業排程與批次執行表的表名及全部欄位，已由 V21 補齊繁中 metadata；既有 migration 不回改 checksum，後續名稱調整以新的 forward-only migration 更新。

| 邊界 | 責任 |
|---|---|
| Controller | 從已驗證 JWT 取得送審人，只呼叫 `ReviewService.submit`。 |
| `ReviewService` | 加密 payload、建立待審唯一鎖、強制 Maker 與 Checker 不同人，核准時在同一交易套用異動與稽核。 |
| 原業務 Service | 只負責核准後的客戶、新契約、收費、批次或撤回正式異動。 |
| Interceptor／Filter | 適合補 requestId、登入身分與 HTTP context，不負責覆核決策。 |
| AOP | 可集中無敏感內容的 use-case 稽核樣板，不攔截 DTO 全文，也不取代顯式覆核 workflow。 |

待審防重使用 `pending_business_lock(function_code, unique_key)`；核准、退回時與案件狀態一起釋放。含個資或健康告知的 payload 使用 `PII_ENCRYPTION_KEY` 以 AES-GCM 加密，清單 API 不回傳 payload，只有明細 API 會解密供覆核人檢視。

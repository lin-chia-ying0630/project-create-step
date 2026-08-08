# Insurance System SA Skills

這個 repository 維護一套供 GitHub Copilot／Codex 使用的保險系統 SA（System Analyst）規範與 Skills。預設使用繁體中文，技術基準為 Spring Boot 3、Java 17、Maven Wrapper、MyBatis、MySQL、Flyway、Docker Compose、Vue 3、TypeScript、Pinia 與 Vite。

同一目錄已拆成三個可獨立建置的交付包：`create-api`（同步 API 與 Flyway）、`create-batch`（批次與對帳）及 `create-web`（Vue 3 前端）。責任與開發 Gate 見 `docs/analysis/framework-standards/00-development-readiness.md`。

規格參考既有 `pos-project` 的分層、`ResponseBodyDto<T>`、OpenAPI、繁中 metadata、MyBatis/Flyway、Docker health check 及前端驗證習慣；建立新專案時重新確認相容的 patch 版本與供應鏈安全性。

## 規範分工

| 文件 | 責任 |
|---|---|
| `AGENTS.md` | 全專案權威規範：語言、證據、技術契約、分析／實作邊界及 Skill 設計。 |
| `.github/copilot-instructions.md` | GitHub Copilot 每次工作必須立即遵守的精簡指令。 |
| `skills/start-insurance-project/SKILL.md` | 保險開案及能力路由，不重複各專項 Skill 的流程。 |
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

第一條業務規格已建立為「新契約批次核保」：`create-api` 的 Flyway 建立 `new_contract` 要保、核保、照會、規則結果、稽核與 outbox，並以 `main.policy_contract` 作正式保單寫入邊界；`create-batch` 執行基本檢核，有問題建立照會，全部通過才承保。完整規格見 `docs/analysis/new-contract/underwriting/00-requirements.md`。

目前已完成資料契約、臺灣基本流程控制與純 Java 基本檢核器；尚未完成批次資料讀寫、保單號碼配置、商品規則介接、API 畫面與真實 MySQL 整合測試，因此仍不可宣稱業務功能可上線。

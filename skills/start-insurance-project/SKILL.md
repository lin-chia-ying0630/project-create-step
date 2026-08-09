---
name: start-insurance-project
description: Start or redesign an insurance software project with Traditional Chinese domain language, explicit insurance concepts, privacy controls, cross-layer contracts, and end-to-end verification. Use when Codex or GitHub Copilot needs to initialize an insurance, policy administration, underwriting, premium, claims, beneficiary, coverage, or policy-change system; define its first vertical slice; or assess whether a new insurance repository is ready for implementation.
---

# 啟動保險系統專案

先建立領域語言與跨層契約，再使用 Java、MyBatis、Docker、Vue 3 與 TypeScript 產生專案骨架。所有使用者可見說明使用繁體中文；法規、商品條款或業務規則沒有正式來源時，標示為待確認，不自行補造。

## 能力路由

本 skill 只負責開案及協調。依任務載入單一能力，需要完整舊系統現代化時依下列順序串接：

`legacy-code-explainer → business-rule-extractor → spec-generator → impact-analysis → test-case-generator`

| Skill | 使用情境 |
|---|---|
| `java-code-analysis` | 追 Java 流程、Null、ArrayIndex、transaction 與 bug 根因。 |
| `legacy-code-explainer` | 解讀舊 Java、COBOL、AS400、batch 與 record layout。 |
| `business-rule-extractor` | 萃取核保、保單、給付、費率、狀態及 validation 規則。 |
| `spec-generator` | 將程式與證據整理成繁中 Markdown 規格。 |
| `sql-analyzer` | 分析 SQL、MyBatis、欄位容量、migration、lock 與效能。 |
| `java-refactor` | 在測試保護下安全拆解舊 Java。 |
| `test-case-generator` | 建立 JUnit、integration、SIT 與 UAT traceability。 |
| `svn-review` | 比對 revision、merge 與 release scope。 |
| `liberty-debugger` | 診斷 Liberty、WAR、JNDI、Servlet 與 classloading。 |
| `impact-analysis` | 修改前追蹤 caller、DB、SQL、DTO、UI、batch、report、interface 與 test。 |
| `insurance-requirement-modeler` | 將新需求建模成流程、案件、決策、資料與驗收條件。 |
| `insurance-interface-mapper` | 分析固定長度檔、批次、XML、JSON、MQ、API 與金融訊息 mapping。 |
| `insurance-reconciliation-analyzer` | 設計批次筆數、金額、重複、漏單、補送與重跑對帳。 |
| `enforce-mybatis-three-layer` | 建立、重構或檢查 Spring Boot + MyBatis 的功能分層、依賴方向、SQL 與交易邊界。 |

核心 SA 流程與按需專項能力不得合併成重複的組合 skill。每一階段先完成並檢查產物，才把已確認證據交給下一階段。

## 分析產物

完整流程依 [references/analysis-artifacts.md](references/analysis-artifacts.md) 建立文件。分析預設唯讀；只有使用者明確要求實作後，才進入 `impact-analysis → 實作計畫 → java-refactor／功能修改 → test-case-generator → 驗證`。

## 1. 確認開案邊界

整理並明確標記：

- 系統目的、使用者角色及第一個可交付流程。
- 適用地區、保險類型、通路及商品範圍。
- 新契約、保全／契約變更、核保、收費或理賠中的哪一段屬於本次範圍。
- 核心系統、身分驗證、文件、付款及通知等外部整合。
- 個資、健康資料、財務資料及稽核資料的分類與保存要求。
- Java Web framework、Java 版本、Maven 或 Gradle、資料庫及 Docker Compose 拓撲。

地區、商品條款或主管機關要求未確認時，不提出確定的法遵結論。

## 2. 建立共同領域語言

依 [references/insurance-domain.md](references/insurance-domain.md) 建立本專案實際採用的詞彙表。至少區分：

- 要保人、被保險人與受益人。
- 保單、險種、主約、附約與保障項目。
- 保額、保費、繳別、保障期間與保單狀態。
- 要保、核保、承保、生效、停效、復效、終止與滿期。
- 契約變更、批註／批單、理賠申請與理賠決定。

名稱相似但生命週期不同的概念不得共用同一資料表或 DTO，除非模型明確表達其角色。

## 3. 定義第一條垂直契約

先選一條能端到端驗證的使用者流程，例如「查詢保單摘要」或「建立地址變更申請」。依序定義：

1. 資料表、主鍵、外鍵、constraint、index 與 migration。
2. Entity 與 persistence mapping。
3. Create Request、Update Request、Query Response 與 validation。
4. API/OpenAPI 的 JSON、錯誤、paging 與相容性。
5. 前端型別、狀態管理、UI metadata 與繁中標籤。
6. 權限、稽核、transaction、concurrency 與 idempotency。
7. 單元、contract、真實資料庫 integration 與 UI 驗證。

Entity 不得直接成為 API request 或 response。OpenAPI 作為可生成的 transport contract；動態代碼及翻譯不得在前端重複維護。

技術棧的分層、目錄與驗證細節見 [references/java-mybatis-vue-stack.md](references/java-mybatis-vue-stack.md)。
開始建立前再讀取 [references/project-conventions.md](references/project-conventions.md)，以既有 POS 專案慣例作為預設值；只有使用者明確變更或新專案限制不相容時才偏離。

## 4. 套用保險資料規則

- 金額使用精確十進位型別，明確定義幣別、scale 與 rounding。
- 業務日期使用不含時區的日期型別；事件時間使用明確時區或 UTC timestamp。
- 分開記錄申請日、核准日、生效日、終止日與系統建立時間。
- 固定封閉狀態使用具正式代碼、繁中說明及統一 JSON/ORM converter 的 enum。
- 動態商品、險種、原因及其他可配置代碼使用字串加後端驗證與描述查詢。
- 未知代碼視為契約或資料錯誤，不靜默轉成 null。
- 正式狀態或多表異動保留不可否認的 audit trail，並設計重送保護與併發控制。
- 範例資料使用完全虛構內容，不複製正式個資、健康或財務資料。
- 新契約要保書依商品、通路、客戶及付款方式產生條件頁面；首期保費授權、共同行銷同意、投資型適合度及附件不得縮成無版本的單一勾選值。
- 完整銀行帳號與信用卡號只能送往一次性驗證／代碼化端點；正式資料、覆核 payload、log 與一般查詢只保存 Token、遮罩值及驗證狀態，且不得保存 CVV、PIN。
- 共同行銷同意須保存接收公司、資料範圍、文件版本及簽署證據；客戶不同意共同行銷不得阻擋本次保險要保。
- 投資型商品必須保存問卷版本、逐題或可稽核評分證據、客戶與商品風險等級、適合度、文件交付及必要錄音錄影參照；不適合或未完成不得送承保批次。
- 附件主檔至少保存類型、所屬關係人、受控儲存參照、完整性雜湊、版本／效期、檢核狀態及新增、修改、覆核人員與時間，不把檔案內容直接塞入業務 JSON。
- 共用查詢按鈕使用「查詢＋功能名詞」並維持同一主按鈕樣式；重新整理只重新載入既有清單，查看／清除／導覽使用外框次要按鈕，實際承保撤回才使用固定藍色按鈕。

## 5. 建立專案與驗證

- 先建立可啟動、可測試的最小骨架及 health check。
- 後端使用 Java + MyBatis，前端使用 Vue 3 + TypeScript，本機環境使用 Docker；除非使用者明確變更，不替換這些核心技術。
- 建立或修改後端功能時使用 `enforce-mybatis-three-layer`，採 package-by-feature 並固定 `Controller → Service interface ← ServiceImpl → Mapper` 依賴。
- Java Mapper 只保留 method contract 與 `@Mapper`；所有 SQL 必須放在 MyBatis Mapper XML，禁止使用 SQL annotation。
- 完成第一條垂直流程後再擴張其他模組。
- 依 repository 的版本與 lockfile 執行 format、lint、compile、test 與 build。
- 使用真實資料庫行為驗證 constraint、DECIMAL、日期、enum mapping、lock 與 affected rows。
- 驗證 success、validation error、domain error、unauthorized、forbidden 與 unexpected error。
- 若有 UI，驗證 loading、empty、error、success、permission denied 及繁中顯示。

## 產出內容

規劃或啟動結果必須包含：

1. 範圍、角色、適用地區及待確認事項。
2. 領域詞彙與模組邊界。
3. 第一條垂直流程的完整跨層契約。
4. 階段、檔案範圍、驗證方式與完成條件。
5. 個資、安全、法遵、資料 migration 及回復風險。

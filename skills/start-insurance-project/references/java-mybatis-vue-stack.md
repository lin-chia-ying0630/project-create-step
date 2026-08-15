# Java、MyBatis、Docker、Vue TypeScript 技術棧

## 既有專案技術基準

- Spring Boot 3、Java 17、Maven Wrapper。
- MyBatis、MySQL 8.4、Flyway forward-only migration。
- Vue 3、TypeScript、Pinia、Vue Router、Vite。
- npm lockfile、ESLint、Prettier、Vitest、Playwright。
- OpenAPI／springdoc 與 `openapi-typescript`。
- Testcontainers MySQL、JUnit、Spring Security test。
- Docker Compose 編排 MySQL、API 與 Web，並以 health check 控制啟動順序。

開新專案時確認相容的 patch 版本與供應鏈弱點，不直接複製可能已過期的精確版本，也不未經評估跨 major 升級。

## 後端邊界

強制採用 package-by-feature；詳細檢查流程見 `skills/enforce-mybatis-three-layer/SKILL.md`：

```text
<feature>/
├── controller/
├── dto/
├── domain/          # 需要時建立
├── service/
│   └── impl/
└── persistence/

Controller -> Service interface <- ServiceImpl -> Mapper -> database
```

- Controller 只處理 transport、基本格式驗證與 response mapping。
- Service 管理保險規則、transaction、權限、idempotency 與跨表流程。
- Java Mapper interface 只保留 method contract 與 `@Mapper`；SQL 只能放在 `src/main/resources/mapper/<feature>/*Mapper.xml`，禁止 `@Select`、`@Insert`、`@Update`、`@Delete`。
- 功能 class 不得散落在 `<feature>/` 根目錄，`common/` 不得收納無法分類的業務 class。
- persistence model 對應資料庫；Request DTO 表達可寫輸入；Response DTO 表達查詢輸出。只有需要獨立生命週期或持久化 mapping 時才建立對應 model。
- SQL 使用參數綁定；排序欄位等無法綁定的片段使用 allowlist，不接受任意輸入。
- Mapper integration test 使用真實資料庫驗證欄位、DECIMAL、日期、enum、constraint、lock 與 affected rows。
- API success 與 failure 都回傳 `ResponseBodyDto<T>`；未分類例外由全域 handler 包裝，不回傳裸字串或框架預設錯誤頁。

## 前端邊界

建議責任分層：

```text
view/page -> component -> optional state module -> typed API client
```

- Vue SFC 使用 `<script setup lang="ts">`，除非 repository 已有不同慣例。
- API 型別優先由 OpenAPI 生成；無生成流程時逐欄定義，不使用 `any` 或不安全 assertion 掩蓋差異。
- `ResponseBodyDto<T>` 只在共用 HTTP client 解開；Store 與 Component 不重複判斷外層格式。
- Component 管理呈現與互動；需要跨元件或跨 route 時才建立狀態模組；後端管理保險規則與資料正確性。
- 所有畫面實作 loading、empty、error、success 與 permission denied 狀態。
- 顯示文字使用繁體中文；日期、金額、幣別與代碼說明由一致 metadata 格式化。
- 詳細資料以一格一個 label + value 呈現；動態欄位缺少繁中 metadata 時顯示原始 key，不讓整個查詢失敗。

## Docker 邊界

- 使用 multi-stage build，runtime image 不包含不必要的建置工具。
- 固定 dependency/runtime 版本，避免只使用漂移的 `latest` tag。
- Compose 明確設定 service name、network、volume、container port、host port、health check 與 dependency condition。
- 秘密資料由環境或 secret mechanism 注入，不寫入 image、Compose 或 repository。
- Migration 執行責任只能有一個明確擁有者，避免多 replica 競爭套用。
- README 的啟動、停止、重建、查看 log 與資料初始化指令必須可直接重現。

## 框架技術規範摘要

完整規範見 `docs/analysis/framework-standards/`（共七份）。開發前依任務類型確認對應規範。

### 例外處理（`01-exception-handling.md`）

- 例外依專案既有 `BusinessException`、validation 與全域 handler 分層為 400、404、409、422 及 500；不為符合文件名稱另建例外類別。
- 錯誤碼格式：`{模組}-{四位數字}`；模組代碼與實際錯誤碼必須由現有 feature 定義，不從文件範例建立。
- 專案既有全域 handler 攔截未處理例外；系統錯誤不得回傳 stack trace 給前端。
- 錯誤訊息不得含身分證號、銀行帳號等個資。

### 交易與併發（`02-transaction-concurrency.md`）

- 多表寫入必須 `@Transactional(REQUIRED)`；正式異動與成功稽核放在同一交易。只有失敗嘗試或資安事件必須在主交易 rollback 後保留時，才以經核准的獨立元件使用 `REQUIRES_NEW`。
- 樂觀鎖欄位依實際 schema 命名；更新時檢查 `affectedRows == 0` 並轉為 409。
- 防重先辨識實際業務唯一鍵，再以資料庫唯一鍵及必要的 row lock 保證。
- Deadlock：`@Retryable` 最多 3 次，間隔 100ms/200ms/400ms。

### API 版本（`03-api-versioning.md`）

- 路徑格式 `/api/v{N}/`；改欄位名稱或移除欄位才需要新版本。
- 棄用期最短 3 個月；棄用回應加 `Deprecation: true` 與 `Sunset` Header。
- OpenAPI 規格是 API 唯一契約，每次變更先改規格再改程式。

### 分頁與排序（`04-pagination-sorting.md`）

- 參數：`page`（從 1 起）、`pageSize`（一般上限 100）；回傳專案唯一的 `PageResult<T>` 契約。
- 排序：`sort=fieldName,asc|desc`；欄位必須白名單驗證，禁止直接拼入 ORDER BY（SQL Injection）。
- 沿用 repository 已存在的分頁工具；不得因文件範例引入新套件。Controller 不直接處理分頁 SQL。

### 稽核 Log（`05-audit-log.md`）

- 所有 POST／PUT／DELETE／覆核狀態變更／敏感資料查詢，依所屬模組已確認的 append-only 稽核契約記錄；共用文件不指定單一功能表名。
- 每個請求注入 `requestId`（MDC），回應 Header 加 `X-Request-ID`。
- Log 禁止輸出：身分證號、銀行帳號、健康告知內容、密碼、token。
- 稽核資料與系統 Log 保存期限由適用地區、資料分類及法遵核准來源決定；未取得正式來源前不得自行填入固定年限。

### 前端狀態管理（`06-frontend-state.md`）

- API 呼叫統一透過 `src/features/<feature>/api/` typed 函式；跨功能 HTTP client 放 `src/shared/api/`，Component 不直接使用 fetch／axios。
- 每個非同步操作統一管理 loading／error／data；只有跨 route 或多消費者確有需要時才建立 Store。具使用者範圍的快取在登出時清除。
- 授權資料由後端提供與裁決；前端不寫死授權判斷，也不預設專案中不存在的 Store。
- 共用 UI 片段放 repository 實際的 `src/shared/components/`；業務頁面只透過 props／slots 使用。

### 測試策略（`07-testing-strategy.md`）

- 覆蓋率：保費計算 ≥ 90%；業務驗證 ≥ 85%；一般 Service ≥ 70%。
- Mapper 測試用 Testcontainers MySQL，**禁止 H2**。
- 測試資料全部虛構；禁止複製正式個資或財務資料。

## 最小驗證鏈

1. 資料庫 health check 通過且 migration 成功。
2. 後端 compile、unit test、mapper integration test 與 API contract test 通過。
3. 前端 format、lint、type-check、unit test 與 production build 通過。
4. Compose 全部必要服務 healthy。
5. 從 Vue UI 操作第一條流程，確認 API JSON 與資料庫異動符合契約。

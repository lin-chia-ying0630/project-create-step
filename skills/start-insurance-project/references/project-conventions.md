# 既有 POS 專案慣例

本規格源自既有 `pos-project` 的後端、前端、Docker、測試及文件模式。新專案預設沿用；若要偏離，先記錄原因、契約影響與 migration／相容策略。

## 專案結構

```text
project-root/
├── pos-api/                 # Spring Boot、MyBatis、MySQL、Flyway
│   ├── src/main/java/
│   │   └── .../
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── dao/
│   │       ├── service/
│   │       ├── service/impl/
│   │       ├── domain/      # 固定代碼 enum、值物件與領域規則
│   │       ├── typehandler/
│   │       ├── config/
│   │       ├── filter/
│   │       └── util/
│   ├── src/main/resources/
│   │   ├── mapper/
│   │   └── db/migration/
│   ├── src/test/java/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw
└── pos-web/                 # Vue 3、TypeScript、Pinia、Vite
    ├── src/
    │   ├── api/             # typed client 函式；httpClient.ts 統一 unwrap ResponseBodyDto
    │   ├── components/
    │   │   ├── shared/      # 分頁、Dialog 殼、動態欄位、狀態標籤等跨業務共用元件
    │   │   ├── policy/      # 保單相關元件（查詢、摘要、維護）
    │   │   └── change/      # 保全變更相關元件（案件、地址、保額、聯絡方式）
    │   ├── views/
    │   ├── stores/
    │   ├── router/
    │   ├── composables/
    │   ├── utils/           # format、fieldLayout、reviewDetail 等純函式工具
    │   └── domain/
    ├── Dockerfile
    ├── compose.yaml
    ├── package.json
    └── package-lock.json
```

實際專案名稱可以不同，但後端與前端責任維持分離。共用根目錄 README 說明整體開案方式；各子專案 README 說明自身業務、API、啟動、驗證與部署。

## API 回覆契約

所有 Controller 與 exception handler 回傳 `ResponseEntity<ResponseBodyDto<T>>`，涵蓋成功、建立、刪除、validation、not found、conflict、forbidden、service unavailable 與未分類例外。

```text
ResponseBodyDto<T>
├── success: boolean
├── message: string
├── errorCode: string
├── errorMessage: string
└── data: T | null
```

- 成功時 `success=true`，失敗時 `success=false`。
- HTTP status 表達 transport 結果；`errorCode` 供程式判斷；`message`／`errorMessage` 供人閱讀。
- 成功時 `errorCode`、`errorMessage` 為 `null`；失敗時 `message`、`data` 為 `null`，不得用 `data` 偷渡錯誤內容。
- 失敗時不得傳回部分正式資料；前端不得比較中文錯誤訊息控制流程。
- 單筆不存在回傳 `null`，集合無資料回傳 `[]`，不得互換語意。
- 前端只在 HTTP client 統一 unwrap，Store 與 Component 接收已型別化的 `data`。

## 欄位與代碼分類

| 類型 | 規格 |
|---|---|
| 固定封閉狀態 | Java enum 使用完整語意名稱、正式代碼、繁中說明；JSON 與 MyBatis TypeHandler 統一轉換。 |
| 資料庫動態代碼 | Java 保持 `String`；Service 驗證啟用及覆核狀態；API 回傳代碼及繁中說明。 |
| 欄位識別 | 使用集中欄位名稱來源；繁中 label 由 metadata 提供，不混入業務代碼 enum。 |
| 技術識別碼 | 只供關聯、lock、version 與 audit，不自動顯示在一般畫面。 |
| 金額 | MySQL `DECIMAL`、Java `BigDecimal`；前端不以浮點數重算正式金額。 |
| 日期時間 | 分開業務日期與事件 timestamp，外部契約使用明確 ISO 8601 與時區策略。 |

同一組動態代碼不得同時存在資料庫、Java enum、TypeScript enum 與前端翻譯表。

## 保全流程慣例

- 受理中正式代碼維持小寫 `p`，畫面顯示「受理中」。
- 案件、變更項目、欄位差異、前後快照、附件、覆核及稽核分開建模。
- 多表正式異動使用同一 public Service transaction，順序為 lock、validate、transition、apply、audit、complete。
- 使用 expected status、affected rows、row lock 或 optimistic version 防止 read-then-write 競爭。
- 建立、送審、覆核及外部事件定義 idempotency key；衝突回傳可讀的 HTTP 409，不洩漏 SQL。
- 查詢 use case 不得產生覆核資料；正式維護 use case 才能建立並套用變更。

## 繁中 UI 與 metadata

- 所有可見 UI chrome、欄名、狀態、錯誤與文件使用繁體中文。
- 後端 metadata 提供 label、type、required、length、precision、readOnly、order、option source 及 visible。
- 動態表單、表格與覆核快照共用相同 metadata 及技術欄位隱藏規則。
- 詳細資料採「一格一個欄位」的 label + value 顯示，不把多個欄位串接在同一格。
- API 回傳代碼與說明時，畫面直接顯示後端說明；metadata 缺失才 fallback 原始 key／code。
- Component 處理呈現與互動；Pinia Store 處理客戶端狀態；後端 Service 決定資格、授權、狀態機及正式資料結果。
- loading、empty、error、success、permission denied 必須有可結束的畫面狀態；HTTP client 設定有限逾時。

## Migration、Security 與 Docker

- Flyway versioned migration 只向前演進；已發布檔案及 checksum 不可修改。
- demo data 與正式 migration 分離；不可提交正式個資、帳密或 token。
- 後端是授權邊界；前端隱藏功能只改善 UX。
- Log 可包含 requestId、error code、business key 的遮罩值與耗時，不包含 Authorization、完整個資、SQL 參數、snapshot 或 response body。
- Docker image 固定版本或 digest，使用 non-root／least privilege、read-only filesystem、`no-new-privileges`、capability drop 與 health check。
- Compose port、service dependency、環境變數與 README 保持一致；資料庫只綁定 loopback 的本機 port。
- 託管平台的資料庫連線由 Secret Group／Database Addon 注入，應用程式使用穩定 alias 讀取 JDBC URL、帳號及密碼；正式 secret 不得出現在 Git、Dockerfile、image layer、啟動參數或 log。
- 本機與雲端共用 datasource 契約：平台 JDBC URL 優先、本機 `DB_URL` 次之；Flyway 預設沿用 datasource 帳號，只有明確權限隔離需求才使用獨立 migration 帳號。
- Hikari 最大連線數必須低於資料庫方案上限，並保留管理與 migration 所需連線；服務 health check 必須實際涵蓋 datasource，不只確認 HTTP process 存活。

## 驗證順序

後端：

```text
Maven Wrapper -> compile -> unit test -> mapper/Testcontainers integration -> verify -> OpenAPI drift -> migration checksum
```

前端：

```text
npm format -> lint -> type-check/build -> unit test -> API type generation drift -> Playwright E2E
```

整合：

```text
Docker Compose health -> Flyway history -> 真實 DB rows -> API wrapper/JSON -> Vue 畫面與繁中 metadata
```

完成報告必須列出實際執行結果。沒有執行的檢查標示原因，不以單一 build 成功取代端到端驗證。

# 專案慣例

本文件只保存可跨保險功能重複使用的專案契約。實際 feature、類別、API、資料表、Store、畫面與狀態碼必須由 repository 現況或已核准需求決定，不從範例複製。

## 結構

```text
project-root/
├── <backend-module>/    # Spring Boot、MyBatis、MySQL、Flyway
├── <batch-module>/      # 實際需要時才建立
└── <frontend-module>/   # Vue 3、TypeScript、Vite
```

上述名稱都是佔位符。先盤點目標 repository 的既有 module；只有全新專案且需求確認需要時才命名並建立，不得照字面建立佔位符目錄。

後端採 package-by-feature；功能內依 `controller`、`dto`、`domain`、`service`、`service/impl`、`persistence` 分層。前端採 `features/<feature>/api|types|views`；只有兩個以上功能以相同責任使用的 API client、component、style 或 type 才移入 `shared/`。選用目錄沒有內容時不建立。

## 跨層契約

- Database、persistence model、Create／Update Request、Query Response、OpenAPI、frontend type 與 UI metadata 使用一致欄位語意。
- Entity／persistence model 不跨 Controller 邊界；API 使用專用 DTO。
- 所有業務 API 使用既有 `ResponseBodyDto<T>`；前端只在共用 HTTP client 解開外層。
- 固定封閉狀態使用領域 enum；營運維護的動態代碼使用資料庫定義，前端不建立第二份對照。
- 金額使用 MySQL `DECIMAL`／Java `BigDecimal`；日期與 timestamp 明確區分並定義時區。
- 檔案內容若由 DMS／物件儲存管理，業務資料庫只保存經核准的識別、完整性與稽核 metadata；是否建立獨立 persistence model 由生命週期、關聯與查詢需求決定。

## 共用判斷

符合以下條件才提取共用元件或工具：

1. 至少兩個實際消費者。
2. 責任與變更原因相同。
3. 不包含某一 feature 的狀態轉換、資格、授權或文案決策。
4. 有明確輸入、輸出與測試。

無法共用的邏輯留在所屬 feature；不得建立萬用 Service、util、Store 或 metadata registry。

## UI 與狀態

- 顯示文字使用繁體中文，動態代碼說明由後端提供。
- Component 負責呈現與互動；後端 Service 決定正式業務結果。
- 狀態先保持在最小生命週期；只有跨 route 或多消費者確有需要時才建立 Pinia Store。
- loading、empty、error、success 與 permission denied 必須可辨識且可結束。
- 所有 viewport 使用同一 route、DOM、API 與功能；至少驗證 320×568、390×844 及桌面。

## Migration、Security 與 Docker

- Flyway versioned migration 只向前演進，已發布檔案與 checksum 不修改。
- Demo data 與正式 migration 分離；Git、image、log 不含正式個資、帳密或 token。
- 後端是授權邊界；前端隱藏操作不能取代 API 授權。
- Migration 只有一個明確執行者；health check 應涵蓋必要依賴。
- 設定、port、service dependency 與 README 保持一致。

## 驗證

依變更風險執行 migration、架構掃描、compile、unit／integration test、OpenAPI drift、frontend format／lint／type-check／build、Docker health、API JSON 與 UI 驗證。分開回報通過、失敗、跳過及未執行項目。

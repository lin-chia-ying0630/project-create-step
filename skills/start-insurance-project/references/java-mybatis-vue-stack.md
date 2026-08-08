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

建議責任分層：

```text
controller -> application/service -> domain rules
                              -> mapper -> database
```

- Controller 只處理 transport、基本格式驗證與 response mapping。
- Service 管理保險規則、transaction、權限、idempotency 與跨表流程。
- Mapper interface 與 XML／annotation SQL 只處理 persistence。
- Entity 對應資料庫；Request DTO 表達可寫輸入；Response DTO 表達查詢輸出。
- SQL 使用參數綁定；排序欄位等無法綁定的片段使用 allowlist，不接受任意輸入。
- Mapper integration test 使用真實資料庫驗證欄位、DECIMAL、日期、enum、constraint、lock 與 affected rows。
- API success 與 failure 都回傳 `ResponseBodyDto<T>`；未分類例外由全域 handler 包裝，不回傳裸字串或框架預設錯誤頁。

## 前端邊界

建議責任分層：

```text
view/page -> component -> store/composable -> API client
```

- Vue SFC 使用 `<script setup lang="ts">`，除非 repository 已有不同慣例。
- API 型別優先由 OpenAPI 生成；無生成流程時逐欄定義，不使用 `any` 或不安全 assertion 掩蓋差異。
- `ResponseBodyDto<T>` 只在共用 HTTP client 解開；Store 與 Component 不重複判斷外層格式。
- Component 管理呈現與互動；store 管理跨元件狀態；後端管理保險規則與資料正確性。
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

## 最小驗證鏈

1. 資料庫 health check 通過且 migration 成功。
2. 後端 compile、unit test、mapper integration test 與 API contract test 通過。
3. 前端 format、lint、type-check、unit test 與 production build 通過。
4. Compose 全部必要服務 healthy。
5. 從 Vue UI 操作第一條流程，確認 API JSON 與資料庫異動符合契約。

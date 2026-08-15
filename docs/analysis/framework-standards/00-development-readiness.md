# 開發就緒基準

本 repository 同時保存跨專案規範與三個可執行骨架。規範入口依 `skills/start-insurance-project/SKILL.md` 路由；任何歷史參考快照只能提供候選名稱與待確認事項，不能覆蓋已核准需求、OpenAPI 或實際 schema。

文件中的 module 名稱皆為佔位符。使用前先以 repository 實際目錄取代；未採用的模組標示「不適用」，不得為符合文件而建立空模組。

## 模組責任

| 專案 | 責任 | 禁止事項 |
|---|---|---|
| `<backend-module>` | REST API、Flyway migration、同步業務交易、OpenAPI runtime drift 驗證 | 不執行排程批次；Entity 不作 API contract |
| `<batch-module>` | 排程／批次、chunk transaction、重跑、對帳、checkpoint | 不擁有 Flyway；不提供業務 REST API |
| `<frontend-module>` | Vue 3 UI、typed API client、Pinia、繁中 metadata 呈現 | 不複製後端業務規則或動態代碼 |

## 開始功能開發前的 Gate

- 已確認適用地區、第一條垂直流程、角色、資料分類與正式業務來源。
- 已盤點實際 module、Java package root、前端目錄、schema、migration owner、OpenAPI、建置工具及測試框架。
- 靜態 OpenAPI YAML、schema migration、DTO、TypeScript type 與 UI metadata 已逐欄對齊。
- API 統一使用 `ResponseBodyDto<T>`：`success,message,errorCode,errorMessage,data`。
- 分頁統一使用 `page,pageSize,totalItems,totalPages`。
- 正式異動與成功稽核同一交易；失敗／資安事件與正式業務稽核分離。
- 待審防重有 DB 唯一鍵與併發整合測試；保全受理中正式代碼為小寫 `p`。
- 保存期限只有在記錄地區、來源版本及法遵核准後才可填入。
- 適用模組各自 compile／test／build，Compose health 與第一條端到端流程皆通過。

目前骨架的 health check 通過只代表技術啟動就緒，不代表任何保險商品、費率、核保或法遵規則已核准。

## 開案前必備條件

| 項目 | 必備證據 |
|---|---|
| Schema 與 migration | schema owner、versioned migration 位置、空庫升級方式及既有庫相容策略已確認 |
| Docker／執行環境 | service、port、network、volume、health check、必要環境變數及 secret 注入方式一致 |
| API 契約 | Controller、Request／Response DTO、OpenAPI、錯誤包裝及前端型別具有唯一來源 |
| 測試與覆蓋率 | 實際測試框架可執行；適用覆蓋率門檻依 `07-testing-strategy.md` 設定並由 CI 強制檢查 |
| CI／交付 | formatter、lint、compile、unit、integration、build、migration 與安全檢查的執行者已確認；尚無 CI 時明確列為缺口 |
| 安全與個資 | 驗證、授權、敏感資料分類、log 遮蔽、secret 管理及稽核責任已確認 |

## 各模組完成 Gate

| 模組 | Done 定義 |
|---|---|
| `<backend-module>` | 架構掃描、compile、unit test、真實 MySQL Mapper integration、migration、OpenAPI drift、API success／error contract 通過 |
| `<batch-module>` | job 可被明確啟動，chunk／transaction、重跑、冪等、對帳、失敗復原及 execution summary 通過 |
| `<frontend-module>` | format／lint／type-check／build 通過；typed API、loading／empty／error／success、320px 至桌面響應式及必要操作可用 |
| 整合 | Compose 必要服務 healthy，migration history 正確，真實 DB row、API JSON 與瀏覽器流程完成同一案例驗證 |

## 架構與文件掃描

先以實際路徑替換佔位符，再執行：

```bash
python3 skills/enforce-mybatis-three-layer/scripts/check_layers.py <backend-module>/src/main/java/<java-package-path>
python3 skills/enforce-code-writing-standards/scripts/check_writing_standards.py .
```

`<java-package-path>` 必須由目前 Java `package` 宣告及目錄確認，不得直接沿用其他 repository。掃描成功只代表靜態規則通過，不能取代 compile、測試或 runtime 驗證。

## 本機啟動與交付驗證

1. 依 lockfile 安裝相依套件並完成各適用模組的 compile、test 與 build。
2. 以 versioned migration 建立或升級隔離的本機資料庫，確認 migration history 全部成功。
3. 啟動 Compose，等待資料庫、後端及前端等必要服務通過 health check。
4. 驗證 health、至少一個成功 API、一個分類正確的錯誤 API，以及 `ResponseBodyDto<T>`／OpenAPI 契約。
5. 使用瀏覽器完成第一條垂直流程，核對資料庫正式資料、稽核、畫面繁中 metadata 與響應式操作。
6. 分開回報通過、失敗、跳過及未執行項目；不得以單一 build 或 health check 宣稱完成。

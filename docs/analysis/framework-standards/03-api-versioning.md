# API 版本控管規範

## 路徑與版本

對外 REST API 使用 `/api/v{N}/<resource>`。`<resource>` 是實際資源路徑佔位符；不得將文件範例視為已存在的端點。

需要新 major path version 的變更包括移除欄位、修改既有欄位名稱或語意、新增既有用戶無法提供的必填欄位，以及改變 HTTP status 語意。新增向後相容的選填欄位或新端點通常不需升版。

## 棄用

- 舊版最短保留 3 個月，實際日期須由發布計畫確認。
- 棄用回應加入 `Deprecation`、`Sunset` 與 successor `Link` header。
- 到期移除前確認所有內外部消費者完成遷移。
- 舊版若需保留，由 adapter 轉換同一份 application result，不複製 Service 業務邏輯。

## OpenAPI

- 使用 repository 實際 OpenAPI 位置，不建立文件中假想的模組目錄。
- schema、path、required、status 與 `ResponseBodyDto<T>` 必須和 Java／TypeScript／測試一致。
- 若專案採靜態 OpenAPI，靜態檔為契約來源，runtime spec 用於 drift 檢查。
- PR 清楚標出 breaking、compatible 與 deprecated 變更。

## 前端

版本路徑只存在 feature typed API function；View 與 component 不直接拼接 URL。升版時同步 request／response 型別與回歸測試，不以 assertion 掩蓋契約差異。

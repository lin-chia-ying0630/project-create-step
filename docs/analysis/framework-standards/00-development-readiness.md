# 開發就緒基準

本 repository 同時保存跨專案規範與三個可執行骨架。規範是設計依據；標示「參考快照」的 POS 文件只能提供候選名稱與歷史經驗，不能覆蓋已核准需求、OpenAPI 或實際 schema。

## 三包責任

| 專案 | 責任 | 禁止事項 |
|---|---|---|
| `create-api` | REST API、Flyway migration、同步業務交易、OpenAPI runtime drift 驗證 | 不執行排程批次；Entity 不作 API contract |
| `create-batch` | 排程／批次、chunk transaction、重跑、對帳、checkpoint | 不擁有 Flyway；不提供業務 REST API |
| `create-web` | Vue 3 UI、typed API client、Pinia、繁中 metadata 呈現 | 不複製後端業務規則或動態代碼 |

## 開始功能開發前的 Gate

- 已確認適用地區、第一條垂直流程、角色、資料分類與正式業務來源。
- 靜態 OpenAPI YAML、schema migration、DTO、TypeScript type 與 UI metadata 已逐欄對齊。
- API 統一使用 `ResponseBodyDto<T>`：`success,message,errorCode,errorMessage,data`。
- 分頁統一使用 `page,pageSize,totalItems,totalPages`。
- 正式異動與成功稽核同一交易；失敗／資安事件與正式業務稽核分離。
- 待審防重有 DB 唯一鍵與併發整合測試；保全受理中正式代碼為小寫 `p`。
- 保存期限只有在記錄地區、來源版本及法遵核准後才可填入。
- 三包各自 compile/test/build，Compose health 與第一條端到端流程皆通過。

目前骨架的 health check 通過只代表技術啟動就緒，不代表任何保險商品、費率、核保或法遵規則已核准。

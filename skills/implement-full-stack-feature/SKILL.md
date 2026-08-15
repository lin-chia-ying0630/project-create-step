---
name: implement-full-stack-feature
description: Implement a complete full-stack feature across the layers that actually exist in the repository, with consistent contracts, tests, and documentation. Use when the user explicitly asks to build, add, or complete a cross-layer capability rather than only analyze or specify it.
---

# 實作全端功能

以一條可驗證的垂直切片交付功能，不分次留下互相不一致的資料庫、後端或前端契約。

## 執行流程

1. 讀取 `AGENTS.md`、根目錄 `README.md`、`.github/copilot-instructions.md`、`skills/enforce-code-writing-standards/SKILL.md`、`skills/enforce-mybatis-three-layer/SKILL.md` 與 `docs/analysis/framework-standards/00-development-readiness.md`。
2. 確認使用者已明確要求實作；只有分析或規格要求時停止於文件，不修改產品程式。
3. 搜尋既有 feature、共用元件、API wrapper、代碼定義、錯誤 enum、migration 與測試，保留工作區既有變更。
4. 使用 `impact-analysis` 盤點任務實際涉及的 database、persistence model、DTO、API JSON、frontend type 與 UI metadata；缺少正式需求時標示 `Unknown` 並請求確認。
5. 先固定欄位型別、nullability、資料來源、錯誤狀態、權限、交易、稽核與驗收條件，再依現有架構實作需要的層；不得為湊齊清單建立不存在或不需要的 Entity、Store、API 或畫面。
6. 涉及 API、交易、分頁、稽核、前端狀態或測試時，讀取 `docs/analysis/framework-standards/` 下對應規範；新增抽象或複雜狀態轉換時使用 `design-pattern-guide`。
7. 使用 `test-case-generator` 補足本次功能適用的成功、驗證、授權、衝突、rollback、responsive UI 與 regression coverage。
8. 執行受影響 formatter、lint、type-check、架構掃描、測試與 build；需要資料庫語意時使用 MySQL，不以 H2 或測試跳過宣稱完成。
9. 只修正本次變更造成的失敗；既有問題與未能執行的驗證分開回報。

## 跨層完成條件

- migration、Entity／persistence model、DTO、OpenAPI／JSON、前端型別與 UI metadata 語意一致。
- 業務 API 使用 `ResponseBodyDto<T>`；Entity 不跨越 Controller 邊界。
- 共用資料與元件只在兩個以上功能具有相同責任時提取；功能專屬邏輯留在 feature。
- 多表寫入與成功稽核位於同一交易，錯誤碼與 HTTP status 符合專案規範。
- Vue 共用既有元件與樣式，320×568、390×844 及桌面保留相同內容與功能。
- 驗證證據足以重現；跳過、失敗或依賴外部環境的項目不得列為已通過。

## 交付格式

先說明已完成的使用者結果，再列出跨層契約、主要檔案、驗證結果及剩餘風險。只有使用者另外要求時才 commit、push、建立 PR 或部署。

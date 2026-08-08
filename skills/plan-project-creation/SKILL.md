---
name: plan-project-creation
description: Design an executable non-insurance project-creation plan from an idea, business requirement, or incomplete setup. Use when Codex needs to create or redesign a general frontend, backend, full-stack, library, or automation project; choose a suitable structure; define cross-layer contracts; sequence implementation; or produce acceptance and verification steps. For insurance projects, use start-insurance-project instead.
---

# 規劃專案建立流程

把需求轉換成可實作、可驗證、可交接的專案建立方案。先查明現況，保留尚未確定的決策，不把偏好誤寫成已確認需求。

## 執行流程

### 1. 盤點現況

- 讀取 repository 的 `AGENTS.md`、README、建置檔、目錄及現有設定。
- 確認是全新專案、既有骨架，或重構初始化流程。
- 記錄語言、框架、執行環境、部署目標、資料來源與外部整合。
- 區分「已確認需求」、「合理假設」、「待決策事項」。

若 repository 為空，先建立中立方案，不自行鎖定框架。只有選項會實質改變資料模型、架構或交付範圍時才詢問使用者。

### 2. 建立最小契約

在撰寫步驟前定義適用的契約：

- 使用者流程與成功條件。
- 模組責任及禁止跨越的邊界。
- 資料模型、欄位型別、必填性與生命週期。
- API 或事件的 request、response、錯誤與相容性規則。
- 設定、環境變數、秘密資料與預設值的責任歸屬。
- UI 狀態、權限、錯誤、空資料與載入行為。

全端專案須沿著資料庫、後端模型、DTO/API、前端型別、UI、測試的順序檢查契約是否一致。不要用同一個模型同時承擔 persistence、transport 與 presentation 責任。

### 3. 做出架構決策

每項重要決策至少記錄：

- 決定與原因。
- 被否決的主要替代方案。
- 影響範圍與可逆性。
- 尚待驗證的風險。

優先採用符合現有 repository 慣例的方案。若是新專案，以需求約束選擇最低複雜度且能滿足驗證需求的結構。

### 4. 切分垂直階段

把工作切成能獨立驗證的階段，而不是只按技術層拆分。建議順序：

1. 可執行的最小骨架與健康檢查。
2. 第一條端到端成功流程。
3. 驗證、錯誤與邊界條件。
4. 安全性、可觀測性及部署設定。
5. 文件、範例與交接檢查。

每個階段都要列出輸入、變更範圍、驗證命令或觀察方式，以及明確完成條件。

### 5. 執行與驗證

- 實作任務中，按階段完成一條綠色切片後再擴張。
- 使用 repository 已有的 format、lint、compile、unit、integration 與 build 指令。
- 新增基礎設施或外部服務時，同時驗證設定、連線、實際資料流與失敗行為。
- 不把「編譯成功」等同於功能完成；依契約驗證真實輸入與輸出。
- 保留使用者既有變更，不處理無關問題。

完整檢查項目見 [references/project-checklist.md](references/project-checklist.md)。只讀取與當前專案類型有關的段落。

## 輸出格式

規劃型任務依序提供：

1. 目標與範圍。
2. 已確認事項、假設與待決策事項。
3. 架構及跨層契約。
4. 分階段實作步驟。
5. 每階段驗證與整體完成條件。
6. 主要風險及回復策略。

實作型任務直接完成可安全執行的階段；回報實際變更、驗證結果及仍需外部條件才能完成的項目。

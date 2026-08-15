---
name: review-generated-code
description: Review AI-generated or newly added Java, MyBatis XML, Flyway, Vue, TypeScript, tests, and documentation for correctness and project compliance. Use when inspecting generated code, a diff, branch, commit, or PR without being asked to implement fixes.
---

# 審查 AI 產生程式

審查預設唯讀。以實際 diff 與可重現證據找出會造成錯誤行為、契約漂移、安全風險或缺少驗證的問題。

## 執行流程

1. 讀取 `AGENTS.md`、根目錄 `README.md`、`.github/copilot-instructions.md`、`skills/enforce-code-writing-standards/SKILL.md`，後端變更另讀 `skills/enforce-mybatis-three-layer/SKILL.md`。
2. 確認 review 範圍與比較基準；檢查工作區、branch、commit 或 PR 的實際 diff，不把整個 repository 的既有問題歸入本次變更。
3. 使用 `impact-analysis` 對照 database、migration、persistence model、DTO、API JSON、frontend type、UI metadata、測試與文件。
4. 檢查交易、併發、防重、權限、稽核、個資、錯誤 status、動態代碼、MyBatis SQL 綁定、responsive UI 及相容性。
5. 執行能驗證疑慮的最小唯讀檢查或測試；無法證明的疑慮標示為待確認，不寫成確定 finding。
6. 依嚴重度輸出可執行 findings；若沒有問題，明確說明仍存在的測試或環境缺口。

## Finding 格式

每項 finding 必須包含：

- 嚴重度與短標題。
- 精確檔案及最小行號範圍。
- 觸發條件與可觀察後果。
- 違反的契約或缺少的驗證。
- 最小修正方向，但不直接修改檔案。

不要把風格偏好、未證實推測或與 diff 無關的舊債列為缺陷。使用者明確要求修正後，改用 `fix-full-stack-bug` 或 `implement-full-stack-feature`。

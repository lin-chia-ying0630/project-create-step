---
name: release-with-pr
description: Publish an explicitly approved local change through an intentional branch, commit, push, and pull request to main. Use when the user asks to commit, push, create a PR, or release current repository changes through GitHub.
---

# 以 PR 發布變更

將已完成且已驗證的變更安全發布成可審查 PR。此流程不替代功能實作、測試或部署驗證。

## 執行流程

1. 讀取專案規範，確認 repository、remote、目前 branch、目標 `main`、工作區狀態與使用者要求的發布範圍。
2. 逐檔檢查 diff、未追蹤檔與敏感資料；混合工作區只 stage 本次檔案，禁止 `git add -A`。
3. 執行與風險相稱的測試、架構掃描、format、lint、build 與 `git diff --check`；失敗或跳過項目清楚回報。
4. 確認目前 branch 不是受保護的 `main`；建立符合專案命名的 feature branch，保留使用者既有 branch 決策。
5. 只 stage 已確認檔案，檢查 staged diff 後以繁體中文建立聚焦 commit。
6. push 目前 branch，再建立以 `main` 為 base 的 PR；PR 摘要包含目的、跨層變更、驗證證據、migration／部署風險及未驗證項目。
7. 查詢 PR URL、head/base、最新 commit、CI 與 merge 狀態；不得把「已建立 PR」描述成「已合併到 main」。

## 安全邊界

- 沒有使用者明確要求時，不 commit、push、建立 PR、merge 或部署。
- 不提交 `.env`、token、密碼、正式個資、產物、暫存檔或機器專屬設定。
- 不使用 force push，不覆寫他人變更，不在 CI 未通過時宣稱發布完成。
- 建立 PR 與合併 PR 是不同動作；只有使用者明確要求且權限與 checks 均允許時才合併。

## 交付格式

回報 branch、commit、push、PR URL、base、CI／merge 狀態與尚待處理事項。部署或執行環境驗證需另依 `diagnose-deployment` 或正式發布要求執行。

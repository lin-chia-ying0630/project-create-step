---
name: fix-full-stack-bug
description: Diagnose and fix a reproducible defect across database, MyBatis, Spring API, Vue state, UI, configuration, or tests. Use when the user explicitly requests a bug fix and the symptom may cross layers or require regression protection.
---

# 修復全端缺陷

先證明根因，再做能恢復正確契約的最小修復；不得以畫面 workaround 掩蓋後端或資料問題。

## 執行流程

1. 讀取專案規範、`skills/enforce-code-writing-standards/SKILL.md`，後端問題另讀 `skills/enforce-mybatis-three-layer/SKILL.md`。
2. 記錄可觀察症狀、重現步驟、預期行為、實際行為、環境與第一個相關錯誤；尚未證明的原因標示為假設。
3. 沿 UI action → typed API → HTTP response → Controller → Service → Mapper XML → schema／資料追蹤，必要時使用 `java-code-analysis`、`sql-analyzer` 或 `impact-analysis`。
4. 先建立會失敗的最小回歸測試，或說明無法自動化重現的具體原因與替代證據。
5. 修正擁有錯誤責任的層；同步調整受影響契約與測試，不擴張成無關重構。
6. 驗證原始重現案例、鄰近邊界、錯誤路徑、權限、交易、稽核及 responsive UI。
7. 重跑受影響測試、架構掃描與 build，確認修復沒有改變未授權的業務規則。

## 判定原則

- 程式、SQL、schema、runtime log 或測試直接證明的內容才標示 `Confirmed`。
- README、註解與猜測不能單獨裁決目前行為。
- 資料修復使用 forward-only migration；不得手動改正式資料或隱藏 migration 錯誤。
- 只有使用者要求修復時才修改程式；單純要求診斷時只回報根因與修復建議。

## 交付格式

依序回報根因、修正內容、回歸測試、已驗證項目與尚未驗證風險。不要只列修改檔案而省略問題為何發生。

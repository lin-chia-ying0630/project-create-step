---
name: insurance-reconciliation-analyzer
description: Design and diagnose insurance batch and transaction reconciliation for record counts, premium and claim amounts, duplicates, missing records, retries, replays, reversals, and operational recovery. Use for policy, billing, collection, payment, claims, commission, bank, partner, migration, or end-of-day interfaces.
---

# 保險批次對帳分析

## 工作流程

1. 確認對帳雙方、business date、cutoff、timezone、currency、source of truth 與 settlement boundary。
2. 定義 batch／file／message／business record 的穩定識別碼及 idempotency key。
3. 建立 control total：輸入、接受、拒絕、重複、成功、失敗、待處理筆數及各幣別金額。
4. 分類差異為 timing、missing、duplicate、amount、status、version、mapping 或 manual adjustment。
5. 定義 retry、replay、reversal、補送、重跑、checkpoint、lock 及 partial failure 行為。
6. 定義操作人員可見狀態、告警、SLA、audit、evidence retention 與人工處置。
7. 以原始輸入、staging、正式資料、outbound acknowledgement 與外部回覆完成閉環驗證。

完整控制清單見 [references/reconciliation-controls.md](references/reconciliation-controls.md)。

## 產出

寫入 `docs/analysis/<domain>/<feature>/reconciliation.md`，包含 boundary、business key、control totals、差異分類、狀態機、重跑矩陣、SQL／API 證據、操作 runbook、alert 與 test cases。不得以直接修改正式資料作為一般補救方式。

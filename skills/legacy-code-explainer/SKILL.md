---
name: legacy-code-explainer
description: Explain legacy Java, COBOL, AS400, batch, fixed-width, and procedural code as maintainable specifications. Use when translating old programs, copybooks, job flows, flags, record layouts, or undocumented behavior into clear Traditional Chinese documentation.
---

# 舊系統程式解讀

## 工作流程

1. 辨識程式入口、呼叫端、runtime、input、output、file、table、copybook 與外部程式。
2. 依執行順序追蹤流程，包含 label、GO TO、PERFORM、return code、flag 與異常出口。
3. 建立縮寫詞彙表；不確定的名稱不得靜默擴寫。
4. 將 record layout 整理成位置、長度、型別、scale、encoding 與意義的欄位表。
5. 將每個分支描述成條件、動作、狀態變化與輸出。
6. 明列無法解析的呼叫、缺少的 copybook 與依賴環境的行為。

## 限制

- 保留 numeric scale、signed field、padding、encoding 與 date format。
- 分開「程式實際行為」、「推測意圖」與「已確認業務規則」。
- 未經要求不得現代化或修正；解讀必須忠實呈現目前行為，包含缺陷。

## 產出

使用繁體中文提供總覽、call flow、欄位表、逐步邏輯、錯誤／return-code 表、side effects 與待確認問題。完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/01-legacy-explanation.md`，其中路徑名稱依實際任務決定。

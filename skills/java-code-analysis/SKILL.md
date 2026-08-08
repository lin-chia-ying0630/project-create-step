---
name: java-code-analysis
description: Analyze Java applications by tracing entry points, calls, data flow, exceptions, state, and framework boundaries. Use for bug diagnosis, NullPointerException, ArrayIndexOutOfBoundsException, incorrect branches, transaction failures, or requests to explain why Java behavior occurs.
---

# Java 程式分析

## 工作流程

1. 重現或精確描述症狀、輸入、預期結果與實際結果。
2. 找出真正入口，依情境追蹤 Controller、Service、DAO、Mapper、Listener、Job 或 Batch。
3. 追蹤 nullable 值、集合長度、index 來源、資料異動、例外轉換與 transaction 邊界。
4. 分開已確認證據與假設；每項結論附檔案與行號。
5. 找出最早出現的錯誤狀態，不只停在最後拋出的例外。
6. 使用者要求修正時，進行最小且契約安全的修改，並加入 regression test。

## 必查項目

- Null 問題：確認初始化責任與 null 是否為合法契約值。
- Index 問題：證明 collection／array 長度及 index 來源，涵蓋空值與邊界。
- Spring／MyBatis：檢查 proxy call、transaction visibility、mapper binding、affected rows 與 exception handler。
- 保險規則：不得只依 method 名稱推測；需要規則時交給 `business-rule-extractor`。

## 產出

依序提供症狀、執行路徑、根因、證據、受影響案例、修正建議與驗證。無法重現的內容標示「未驗證」。

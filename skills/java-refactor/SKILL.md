---
name: java-refactor
description: Refactor legacy Java for clarity, cohesion, testability, and lower conditional complexity while preserving observable behavior. Use for large methods, deep if/else trees, duplicated logic, mixed responsibilities, unsafe null handling, or unclear naming.
---

# Java 安全重構

只有使用者明確授權修改程式時才執行本 skill；分析、解讀、規格或影響評估要求維持唯讀。

## 工作流程

1. 結構變更前以 characterization test 固定目前行為。
2. 辨識責任、invariant、side effect、transaction boundary 與外部可見契約。
3. 小步執行 rename、extract method、value object、replace conditional，再分離責任。
4. 業務規則改動與純 refactor 分開。
5. 每次重要轉換後跑 focused test，完成後跑完整相關測試。

## 限制

- 保留 API JSON、DB write、exception mapping、ordering、money rounding、time handling 與 audit event。
- 避免推測性 abstraction 與 catch-all utility class。
- 不把 transaction 工作搬到 self-invoked 或不經 Spring proxy 的 method。
- 只有存在單一權威來源時，才以 domain type 取代 Boolean parameter 或 magic code。

## 產出

說明 code smell、保留行為、轉換步驟、風險點與測試證據。若刻意修改行為，必須明確指出。

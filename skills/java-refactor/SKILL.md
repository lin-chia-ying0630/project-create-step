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

## 三層架構強制規則

重構時必須確認或修正以下分層違反：

- **Controller**：只接收請求與回傳 `ResponseBodyDto<T>`，不含業務邏輯、不直接呼叫 DAO。
- **Service**：業務規則、`@Transactional` 邊界、例外拋出；不得含任何 SQL 字串或 JDBC 呼叫。
- **DAO（Mapper）**：SQL 只能定義在 `src/main/resources/mapper/<feature>/*Mapper.xml`；Java Mapper 只保留 interface method 與 `@Mapper`，禁止 `@Select`、`@Insert`、`@Update`、`@Delete`。動態條件使用 `<if>`、`<where>`、`<foreach>` 與 `#{}`，不接受字串拼接或使用者輸入 `${}`。

## 工具類抽取規則

下列純技術邏輯發現重複時才抽取至 `util/`：單一功能使用者放 `<feature>/util/`，確實跨功能者才放 `common/util/`。不得為了縮短 Service 而搬移業務規則：

- 日期計算、金額換算、字串遮罩（個資）
- UUID 產生、`requestId`/`traceId` 注入
- 排序白名單驗證
- 其他出現在兩個以上 Service 的相同邏輯片段

工具類不得注入 Spring Bean、不得呼叫資料庫，保持純函式。

## 限制

- 保留 API JSON、DB write、exception mapping、ordering、money rounding、time handling 與 audit event。
- 避免推測性 abstraction；工具類只收確定重複且無副作用的邏輯。
- 不把 transaction 工作搬到 self-invoked 或不經 Spring proxy 的 method。
- 只有存在單一權威來源時，才以 domain type 取代 Boolean parameter 或 magic code。
- 重構後覆蓋率不得低於重構前；若發現覆蓋率空洞，同步補測試。

## 產出

說明 code smell、保留行為、轉換步驟（含三層分層與工具類移動）、風險點與測試證據。若刻意修改行為，必須明確指出。

# 專案設計模式 Catalog

## 選擇原則

依序選擇「直接 method／enum」、「物件組合」、「設計模式」，最後才考慮 framework 或自製 DSL。每個抽象必須對應已確認的變化軸；避免同時對商品、通路、客戶類型與流程階段建立多重繼承。

| 模式 | 適用訊號 | 建議 package | 最低驗證 |
|---|---|---|---|
| Strategy | 同一決策已有兩種以上演算法或規則 | `domain/.../rule`、`validation` | 每個策略 contract test；選擇器測試 |
| Composite／Pipeline | 多條規則需排序、彙總、短路或完整收集 | `validation`、batch service | 順序、短路、全錯誤彙總測試 |
| Factory Method | 建立方式依固定類型分歧，呼叫端不應知道具體類別 | `domain/.../factory` | 每個類型與未知類型測試 |
| Facade | 一個 use case 協調多個 repository、Mapper 或 integration | `service/impl` | transaction integration test；失敗回滾 |
| Adapter | 外部格式、錯誤模型或協定不可滲入 domain | `integration/<provider>` | provider contract、timeout、mapping 測試 |
| State／Transition Policy | 合法動作取決於目前狀態，且轉換規則固定 | `domain/.../policy` | 狀態轉移矩陣與非法轉移測試 |
| Template Method | 骨架穩定且子步驟確實有多個實作 | batch framework | 骨架順序與每個 hook 測試 |
| Observer／Domain Event | 交易完成後有多個獨立後續反應 | application/event | 重送、順序、失敗隔離與 outbox 測試 |
| Specification | 多個可組合條件需要 and／or／not，且具領域名稱 | domain specification | 單條與組合 truth table |
| Repository／Persistence Gateway | domain/application 需要資料存取契約 | `persistence` | MyBatis XML integration test |

## 專案套用邊界

### Java／Spring

- 以 constructor injection 注入 `List<Rule>` 或具名 Adapter，由組合根決定實作集合。
- Spring service 預設無狀態；request 資料只存在 method local、DTO 或明確 scope。
- application service 管 use case 與 transaction；domain policy 管純規則；Mapper 管持久化 contract。

### MyBatis

- Mapper interface 視為 Persistence Gateway，只定義 method contract 與必要 `@Param`。
- SQL、`resultMap`、動態條件與 statement 全部放 XML；禁止 `@Select`、`@Insert`、`@Update`、`@Delete` 及 provider annotation。
- 跨多表正式狀態變更由 service transaction 協調，不建立可繞過 service 的 Active Record。

### Vue／TypeScript

- composable 用於可重用的狀態與互動流程；純視覺重用使用 component；API 轉換使用 typed adapter function。
- Pinia store 不是所有畫面的預設 Singleton；只有跨 route 共享且有明確生命週期的狀態才放 store。
- 後端 metadata 與動態代碼是唯一來源；前端 enum 只表示真正封閉的 UI 狀態。

## 保險案例

### 核保規則

當基本資料、保費銷帳、健康告知與職業風險規則開始獨立演進時，將每條規則實作相同 contract，由 pipeline 收集 `ValidationIssue`。規則不得自行承保、建立照會或更新資料庫；use case 根據彙總結果執行狀態交易。

### 要保作業狀態

狀態 code 與繁中 description 放固定 enum；合法轉換集中於 transition policy。狀態寫入仍由 application service 在 transaction 內完成，避免 enum 執行 Mapper I/O。

### 首期保費與銀行介接

內部使用統一收款命令與結果；不同銀行、繳費管道或檔案格式各自實作 Adapter。銷帳比對與承保資格是 domain/application 規則，不放在銀行 Adapter。

## Review 判斷

以下任一成立時通常不應新增模式：只有一個呼叫端與一個實作、差異只是欄位名稱、預期變化沒有需求或證據、抽象後仍需以 `if` 判斷具體類別、測試必須啟動完整 Spring context 才能驗證純規則。

以下訊號適合重構：同一 switch 在三處以上同步修改、加入一條規則會修改不相關類別、外部 DTO 已滲入 domain、service 同時負責選擇算法與實作算法、狀態可由多處任意跳轉。

## 參考依據

- Spring Framework IoC／Dependency Injection：以 constructor 或 factory method 提供依賴，讓物件不自行尋找實作。
- MyBatis 3 Mapper XML：mapped statements、result maps 與可重用 SQL fragment 的正式配置位置。
- Oracle Java enum：固定且有限的常數集合使用 enum。

專案規則比通用範例更嚴格：MyBatis SQL annotation 全面禁止，且不得以模式合理化跨層依賴或個資共享狀態。

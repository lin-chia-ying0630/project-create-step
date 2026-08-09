# 新契約資料異動覆核需求

## 範圍與證據

| 等級 | 內容 |
|---|---|
| Confirmed | 使用者要求凡可修改資料的操作都必須進入覆核。 |
| Confirmed | 覆核範圍包含客戶建立、保單登打、承保撤回、新契約批次核保及首期保費資料。 |
| Inferred | 保單號碼編發會修改要保案件，因此歸入保單登打覆核類別。 |
| Unknown | 正式環境的 functionCode 授權矩陣與稽核保存期限尚未提供。 |

## 能力與控制

| ID | 能力／控制 | 驗收條件 |
|---|---|---|
| BR-001 | 所有正式資料異動先送審 | 送審成功後正式表尚未改變，只建立待覆核案件。 |
| BR-002 | Maker-Checker 職務分離 | 建立人核准或退回自己的案件時回傳 `REV-4221`。 |
| BR-003 | 待審防重 | 同 `functionCode + businessKey` 同時只能有一件待覆核案件。 |
| BR-004 | 原子核准 | 正式異動、覆核狀態、成功稽核與解鎖同成同敗。 |
| BR-005 | 安全保存 | 覆核 payload 加密保存；待審清單不回傳 payload。 |
| BR-006 | 可退回 | 退回不修改正式資料，並釋放待審鎖以允許重新送審。 |

## 模式檢視

| 項目 | 結論 |
|---|---|
| 問題與變化點 | 已有客戶、保單、撤回、批次與收費等多種異動 use case，需共用送審及決行控制。 |
| 現有第二種行為 | 六種 operation type（五類畫面，另含保單號碼編發）有不同 payload 與正式 Service。 |
| 建議模式 | Application Service Facade + 固定 operation enum + transition policy。 |
| 放置層級 | `review/service/impl` 協調交易；`review/domain` 驗證狀態與職務分離；Mapper XML 保存案件。 |
| 交易與錯誤邊界 | 核准使用單一 Spring transaction；重複待審 409、同人覆核 422、查無 404。 |
| 測試方式 | transition policy 單元測試；後續以 Testcontainers 驗證唯一鎖與核准 rollback。 |
| 不採用的替代方案 | 不以 Interceptor 或 AOP 暗中攔截寫入，避免非 HTTP 流程繞過及正式資料先被修改。 |

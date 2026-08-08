---
name: impact-analysis
description: Analyze the cross-layer impact of changing a field, method, type, code, API, table, configuration, or workflow. Use before implementation or review to find callers, consumers, persistence mappings, migrations, tests, documentation, deployment, and compatibility risks.
---

# 跨層影響分析

## 工作流程

1. 完整 SA 流程時先讀取可用的 `00-requirements.md` 與 `03-specification.md`，再以目前 repository、schema 與外部契約驗證內容。
2. 定義變更 symbol 與語意契約，包含前後型別、意義、nullability、format 或 behavior。
3. 搜尋 declaration、reference、string/XML mapping、reflection、serialization、SQL、configuration、template、job 與 external contract。
4. 沿 database、Entity、DTO、OpenAPI、client、UI metadata、test 與 docs 追蹤上游 producer 及下游 consumer。
5. 將每項影響分類為必要修改、相容性、資料 migration、測試、部署或有證據的無影響。
6. 辨識 rollout order、backward compatibility window、rollback 限制與 observability 需求。

## 固定檢查面

呼叫端、DB 欄位、SQL／MyBatis、Entity、DTO／VO、API、JSP／Vue、Batch、Report、Interface、設定、測試與文件。

- 搜尋 persisted name 與語意別名，不只搜尋 Java symbol。
- 將 dynamic code table、batch file、report 與 integration 視為可能的隱藏 consumer。
- 缺少 repository、generated client 或外部系統時，不得宣稱已完整涵蓋。

## 產出

提供 impact matrix：layer、artifact、relationship、required action、risk、evidence 與 verification，最後附有順序的實作及上線清單。完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/04-impact-analysis.md`。

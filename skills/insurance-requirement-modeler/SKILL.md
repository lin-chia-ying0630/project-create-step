---
name: insurance-requirement-modeler
description: Model new insurance business requirements as traceable capabilities, actors, processes, cases, decisions, data, controls, and acceptance criteria. Use for policy administration, new business, underwriting, premium collection, policy change, claims, beneficiary, coverage, product, or review requirements that are not being extracted primarily from legacy source code.
---

# 保險需求與流程建模

將新需求轉成可由業務、SA、開發與測試共同檢查的繁中規格，不以流程圖取代資料、決策或例外契約。

## 工作流程

1. 確認領域、角色、商品版本、通路、保單狀態、有效時間與適用地區。
2. 建立 capability、business event、trigger、precondition、result 與 out-of-scope。
3. 依 [references/industry-frameworks.md](references/industry-frameworks.md) 選擇適合的建模框架。
4. 將流程、案件、決策及資料分開建模，再以穩定 ID 建立關聯。
5. 定義主流程、替代流程、錯誤、逾時、取消、補件、人工介入與恢復。
6. 定義權限、個資、稽核、transaction、concurrency、idempotency 與通知。
7. 產生 acceptance criteria，交給 `spec-generator`、`impact-analysis` 與 `test-case-generator`。

## 框架選擇

- 固定且可預先排序的流程：BPMN。
- 依資料、事件或人工判斷動態展開的案件：CMMN。
- 多條件資格、費率、核保或給付判斷：DMN decision requirement／decision table。
- 保險能力、詞彙或交換欄位對照：ACORD，只引用可合法取得的版本。
- 跨系統金融詞彙對照：FIBO，不直接產生資料庫 schema。

## 產出

寫入 `docs/analysis/<domain>/<feature>/00-requirements.md`，包含 scope、glossary、actor、capability、process／case、decision、data、control、acceptance criteria、evidence 及 open questions。未取得正式來源的內容標示 `Inferred` 或 `Unknown`。

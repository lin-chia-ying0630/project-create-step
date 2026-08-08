---
name: test-case-generator
description: Generate traceable JUnit, integration, SIT, and UAT test cases from requirements, business rules, source code, APIs, and database contracts. Use for test design, regression coverage, boundary analysis, workflow validation, or acceptance criteria.
---

# 測試案例產生

## 工作流程

1. 完整 SA 流程時先讀取同一 `<domain>/<feature>` 可用的 `00-requirements.md`、`02-business-rules.md`、`03-specification.md` 與 `04-impact-analysis.md`。
2. 萃取需求並指派 rule 或 acceptance-criterion ID。
3. 分成 happy path、boundary、invalid、authorization、state、concurrency、retry 與 failure recovery。
4. 選擇能證明行為的最低測試層；ORM、SQL、lock、migration 與 precision 使用真實資料庫。
5. 定義可重現的前置資料、輸入、步驟、預期 response、DB／audit 影響與 cleanup。
6. 每個案例回鏈來源規則，並列出尚未覆蓋的規則。

## 保險系統必測

涵蓋生效日、商品版本、保單狀態、角色、重複送件、覆核狀態轉換、金額 rounding、null／blank 差異、dynamic code 狀態與 rollback。不得使用正式個資。

## 產出

提供 traceability matrix；使用者要求時提供可執行的 JUnit／integration test。測試名稱描述可觀察行為，不依賴 private implementation，並區分 automation、SIT 與 UAT 責任。完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/05-test-cases.md`，只有 `Confirmed` 規則可直接成為既有行為的確定預期。

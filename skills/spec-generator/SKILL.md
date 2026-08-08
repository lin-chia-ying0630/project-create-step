---
name: spec-generator
description: Generate implementation-grounded Markdown specifications from source code, APIs, SQL, tests, and requirements. Use when documenting system flows, API contracts, field definitions, validation, state transitions, errors, permissions, or batch behavior.
---

# 系統規格產生

## 工作流程

1. 完整 SA 流程時讀取同一 `<domain>/<feature>` 可用的 `00-requirements.md`、`01-legacy-explanation.md` 與 `02-business-rules.md`；不存在的階段須有省略原因。
2. 定義範圍、讀者、系統版本與權威來源。
3. 撰寫規格前先追蹤實際端到端流程。
4. 分開目前行為、要求行為與差異。
5. 只產生有證據支持的章節；標示未知與矛盾。
6. 使用穩定 ID 串起 rule、field、API、table、error、role 與 test。

## 必要章節

- 目的與範圍。
- 角色、權限、前置條件與假設。
- 主流程、替代流程與錯誤流程。
- 狀態轉換表。
- API input、output 與 error contract。
- 欄位表：名稱、繁中標籤、型別、長度／精度、必填、來源、驗證。
- 資料庫讀寫、transaction、locking 與 audit 影響。
- Business rule catalog 與 test traceability。
- 待確認問題與實作缺口。

除非使用者指定其他語言，使用繁體中文。只有多步流程或狀態模型因此更清楚時才使用 Mermaid。不得把實作缺陷描述成已核准需求。

完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/03-specification.md`，並分開記錄「目前行為」、「預期行為」與「Gap」。

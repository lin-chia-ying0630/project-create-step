---
name: insurance-interface-mapper
description: Map insurance batch files, fixed-width records, CSV, XML, JSON, MQ messages, APIs, ACORD messages, and payment messages into traceable source-to-target contracts. Use for policy, premium, claims, commission, beneficiary, coverage, product, bank, partner, mainframe, or migration interfaces.
---

# 保險介接欄位映射

## 工作流程

1. 確認 producer、consumer、方向、頻率、transport、encoding、timezone、版本與資料敏感等級。
2. 保存原始 schema、copybook、XSD、OpenAPI、sample 與錯誤回覆；樣本必須去識別化。
3. 建立 source-to-target mapping，逐欄記錄型別、長度、precision、required、default、format、code、conversion 與 evidence。
4. 定義 header、detail、trailer、筆數、金額、checksum、排序、分頁／分檔及檔名規則。
5. 定義 validation、reject、partial success、retry、duplicate、timeout、version negotiation 與 backward compatibility。
6. 需要對帳時交給 `insurance-reconciliation-analyzer`；需要 SQL／MyBatis 驗證時交給 `sql-analyzer`。

## 標準使用

依 [references/interface-standards.md](references/interface-standards.md) 判定 ACORD、ISO 20022 或內部 canonical model。未取得正式 schema、版本與授權時，只做概念對照，不宣稱 compliant。

## 產出

寫入 `docs/analysis/<domain>/<feature>/interface-mapping.md`，包含 context、schema version、mapping table、code mapping、validation、error、security、replay、compatibility、test vectors 與 open questions。

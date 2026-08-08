---
name: business-rule-extractor
description: Extract traceable insurance business rules from Java, COBOL, SQL, configuration, tests, and documentation. Use for underwriting, policy administration, benefits, premiums, rates, eligibility, review workflows, status transitions, and validation-rule discovery.
---

# 保險業務規則萃取

## 工作流程

1. 完整 SA 流程時讀取可用的 `00-requirements.md`、`01-legacy-explanation.md` 與原始證據；新需求可沒有 `01`，並保留來源及證據等級。
2. 定義業務事件、角色、商品、保單狀態與生效時間情境。
3. 檢查所有裁決點：validation、service branch、SQL predicate、code table、formula、configuration 與 test。
4. 將每條規則正規化為 `條件 → 判斷／計算 → 結果 → 例外`。
5. 指派穩定 rule ID，並引用精確來源位置。
6. 辨識規則優先序、重疊、預設行為及互相衝突的實作。
7. 確認代碼是固定 enum 或資料庫維護的 dynamic code。

## 證據等級

- `Confirmed`：有直接實作與證據。
- `Inferred`：實作高度暗示，但沒有完整裁決或正式來源。
- `Unknown`：缺少必要程式、文件或領域決策。

不得把推測規則直接寫成正式需求。完整保留金額精度、生效日、時區、狀態、角色及商品版本條件。

## 產出

提供規則目錄：ID、名稱、觸發、前置條件、規則、結果、例外、來源、證據等級與建議測試。矛盾規則另列。完整 SA 流程時寫入 `docs/analysis/<domain>/<feature>/02-business-rules.md`；不得把上一階段的 `Inferred` 或 `Unknown` 自動升級為 `Confirmed`。

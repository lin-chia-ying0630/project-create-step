# 分階段分析產物

## 目錄

依實際任務動態決定 `<domain>/<feature>`：

```text
docs/analysis/<domain>/<feature>/
├── 00-requirements.md        # 新需求建模時使用
├── 01-legacy-explanation.md
├── 02-business-rules.md
├── 03-specification.md
├── 04-impact-analysis.md
└── 05-test-cases.md
```

不得把示例 class、method 或功能名稱寫成固定路徑。若任務是新需求，先以 `insurance-requirement-modeler` 建立 `00-requirements.md`；若任務不是 legacy code，省略 `01`，並在保留的第一份文件說明原因。

## 階段門檻

| 階段 | Skill | 可交給下一階段的內容 |
|---|---|---|
| 00 | `insurance-requirement-modeler` | Scope、角色、流程／案件、決策、資料、控制與 acceptance criteria。 |
| 01 | `legacy-code-explainer` | 實際 call flow、欄位、輸入輸出、side effect 與未解析依賴。 |
| 02 | `business-rule-extractor` | 有穩定 ID、來源位置及證據等級的規則。 |
| 03 | `spec-generator` | 分開目前行為、預期行為及 gap 的完整規格。 |
| 04 | `impact-analysis` | 跨層 impact matrix、相容性、migration、rollout 與驗證範圍。 |
| 05 | `test-case-generator` | 規則對測試的 traceability、JUnit／integration／SIT／UAT 案例。 |

前一階段的 `Inferred` 或 `Unknown` 不得在下一階段升級為 `Confirmed`，除非取得新的正式來源或可重現證據。

## 權威來源

- 正式核准需求與商品規則：預期行為。
- 實際 Java、COBOL、SQL、設定與 runtime evidence：目前行為。
- 真實 database schema、constraint 與 migration history：目前資料契約。
- 測試：既有驗證意圖，可能過期。
- README、註解、SVN log：補充證據。

來源不一致時建立 gap，不自行選擇其中一份覆蓋其他來源。

## 編號與語言

- Business rule：`BR-001`。
- Field：`FLD-001`。
- API：`API-001`。
- Flow：`FLOW-001`。
- Test case：`TC-001`。
- 文件說明使用繁體中文；class、method、DB column、API field 與正式代碼保留原名。

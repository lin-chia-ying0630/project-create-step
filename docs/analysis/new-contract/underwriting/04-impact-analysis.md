# 承保撤回影響與控制

## 功能定義

本功能處理「錯誤出單但尚未形成有效權利義務」的案件。它不是一般保單終止、解約、撤銷或保全功能；保單已生效、已有收費、理賠、保全、佣金、再保或外部報送時禁止直接刪除，必須走相對應的正式沖正／終止流程。

## 操作前 Gate

- 使用者具 `NEW_CONTRACT_ISSUANCE_REVERSAL` 權限，Maker 與核准者分離規則依公司權限政策確認。
- `policy_no`、`application_no`、`underwriting_case_no` 關聯一致。
- 保單狀態與生效日符合公司核准的可撤回條件。
- 收費、保全、理賠、佣金、文件簽發、再保與外部通報皆沒有不可逆後續資料。
- `expectedPolicyVersion`、`expectedApplicationVersion`、`expectedUnderwritingVersion` 全部一致。
- 操作原因代碼有效，且使用者完成二次確認。

## 同一交易順序

1. 依固定順序鎖定 `insurance_application`、`underwriting_case`、`policy_contract`。
2. 查詢所有正式子表及下游阻擋資料。
3. 產生去識別化的 `before_content`，至少保存狀態、版本、金額、日期、各子表筆數及資料 hash；不得保存健康答案或完整個資。
4. 刪除 `policy_contract_evidence`。
5. 刪除 `policy_underwriting_condition`。
6. 刪除 `policy_beneficiary`。
7. 刪除 `policy_coverage`。
8. 刪除 `policy_party`。
9. 刪除 `policy_contract`。
10. 將 `underwriting_case` 改為 `PENDING`、清除決定與 policyNo、增加 version。
11. 將 `insurance_application` 改為候選代碼 `NOT_ISSUED`、清除批次檢核結果、增加 revision 與 version。
12. 刪除該保單的 `policy_materialization_map`，但其內容已包含在前快照及稽核 hash 中。
13. 建立去識別化 `after_content`。
14. INSERT `policy_issuance_reversal_audit` 與 outbox event。
15. commit；任何一步失敗全部 rollback。

`NOT_ISSUED`／「未承保」目前是候選代碼，正式實作前須取得業務狀態碼核准。

## API

| Endpoint | 說明 |
|---|---|
| `GET /api/v1/new-contract/policy-reversals/{policyNo}/preview` | 回傳案件狀態、版本、預計刪除筆數與 blockers，不回傳健康或完整個資 |
| `POST /api/v1/new-contract/policy-reversals` | 帶版本、原因、confirmToken 與 `Idempotency-Key` 執行承保撤回 |

## 錯誤

- `409 UW-2010`：版本衝突。
- `409 UW-2011`：存在收費、理賠、保全或其他下游資料。
- `422 UW-2012`：保單狀態／生效日不允許撤回。
- `403 UW-2013`：沒有承保撤回權限。
- `400 VAL-0001`：原因、版本或確認資訊缺漏。

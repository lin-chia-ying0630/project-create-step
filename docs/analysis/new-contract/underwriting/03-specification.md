# 要保作業資料模型規格

## 核心業務表

| Table | 中文名稱 | 責任 | 承保後處理 |
|---|---|---|---|
| `insurance_application` | 要保主檔 | 要保書號碼、商品版本、彙總金額、通路、日期及流程狀態 | 保留原始申請，不覆寫成保單 |
| `application_party` | 要保關係人 | 要保人、被保險人及其他角色與客戶快照參照 | 轉為 `main.policy_party` 快照 |
| `application_coverage` | 要保保障項目 | 主約／附約、被保險人、保額、保費及期間 | 轉為 `main.policy_coverage` |
| `application_beneficiary` | 要保受益人 | 受益類型、順位、比例及指定方式 | 轉為 `main.policy_beneficiary` |
| `health_disclosure` | 健康告知 | 題組版本、題號、加密答案及加密補充內容 | 留在核保證據區，不複製至一般保單主檔 |
| `application_declaration` | 聲明與同意 | 聲明版本、確認角色、方式及證據參照 | 保留不可否認證據 |
| `application_signature` | 簽署證據 | 簽署角色、方式、時間及外部證據參照 | 保留不可否認證據 |
| `underwriting_case` | 核保案件 | 核保流程狀態、決定、核保人及正式保單號碼 | 完成後唯讀 |
| `underwriting_condition` | 核保條件 | 加費、除外及有效期間 | 轉為正式保單核保條件 |
| `underwriting_inquiry` | 照會單 | 申請 revision 的照會狀態及結案時間 | 保留歷史，不覆寫 |
| `underwriting_inquiry_item` | 照會項目 | 規則別問題、回覆及時間 | 保留歷史，不覆寫 |

## 主檔界線

`insurance_application` 是要保主檔，不是正式保單主檔。它只保存案件層級欄位；多角色、多保障、多受益人、逐題健康告知與多次照會必須留在子表，不得塞入主檔 JSON 或重複欄位。

## 關聯

```text
insurance_application (1)
├── application_party (N)
├── application_coverage (N)
├── application_beneficiary (N)
├── health_disclosure (N)
├── application_declaration (N)
├── application_signature (N)
└── underwriting_case (1)
    ├── underwriting_condition (N)
    └── underwriting_inquiry (N revisions)
        └── underwriting_inquiry_item (N)
```

## 健康資料控制

- 題組代碼、題組版本與題號使用明文索引欄位，答案及補充醫療內容使用 application-level envelope encryption。
- `encryption_key_version` 只記錄金鑰版本，不保存金鑰或 secret。
- 解密只允許具核保健康資料權限的後端服務；前端、一般 Log、稽核與 outbox 不得收到原始答案。
- 重新回答時增加 application revision／新資料，不直接覆寫已參與核保決定的歷史答案。

## 承保 mapping Gate

正式建立保單前必須確認：一筆 `BASE` 保障、必要角色、受益比例、全部聲明及簽署、健康告知完整、無未結照會、所有必要規則通過。之後在同一交易建立 `main` 的契約、關係人、保障、受益人及核保條件快照。

## 逐表承保處理

| `new_contract` 來源 | 承保時處理 | `main` 目標／結果 |
|---|---|---|
| `insurance_application` | 建立契約層級快照，回寫 `PASS`、`COMPLETED`、policyNo | `policy_contract` |
| `application_party` | 每一角色逐筆複製，不只處理要保人與主被保險人 | `policy_party` |
| `application_coverage` | 每一主約／附約逐筆複製並設定初始保障狀態 | `policy_coverage` |
| `application_beneficiary` | 每一受益類型、順位及比例逐筆複製 | `policy_beneficiary` |
| `health_disclosure` | 原始加密答案留在核保區；逐筆建立不可含答案的證據索引 | `policy_contract_evidence` |
| `application_declaration` | 保留原始確認記錄並建立聲明版本證據索引 | `policy_contract_evidence` |
| `application_signature` | 保留原始簽署證據並建立簽署證據索引 | `policy_contract_evidence` |
| `underwriting_case` | 回寫 `COMPLETED`、`APPROVE`、policyNo、完成時間與 version | 留在 `new_contract`，並由 `policy_contract.underwriting_case_no` 反查 |
| `underwriting_condition` | 每筆加費／除外條件逐筆複製 | `policy_underwriting_condition` |
| `underwriting_inquiry` | 確認不存在未結案照會；歷史照會留存 | 不複製內容，以核保案件關聯追溯 |
| `underwriting_inquiry_item` | 確認母照會已結案；歷史問答留存 | 不複製內容，以核保案件關聯追溯 |

每一筆真正複製或建立索引的來源資料，都必須寫入 `new_contract.policy_materialization_map`。其來源及目標唯一鍵是批次重跑控制，不得只依 application 狀態判斷。

## 單件承保交易順序

1. 依 `application_no` 鎖定 `insurance_application` 與 `underwriting_case`。
2. 確認 application revision、record version、檢核結果及照會狀態。
3. 配置並鎖定唯一 `policy_no`。
4. 建立 `policy_contract`。
5. 逐筆建立 `policy_party`、`policy_coverage`、`policy_beneficiary`。
6. 逐筆建立 `policy_underwriting_condition` 與 `policy_contract_evidence`。
7. 每一筆寫入 `policy_materialization_map`。
8. 更新要保及核保案件狀態與版本。
9. 寫入成功稽核及 outbox event。
10. 一次 commit；任何一表失敗則全部 rollback。

## 批次啟動

- `<frontend-module>` 控制台輸入 `applicationNo` 與營業日，透過 `<backend-module>` 寫入 `underwriting_batch_request`；前端不得直接連線 `<batch-module>`。
- 正式排程固定為 `0 0 21 * * *`，時區固定 `Asia/Taipei`，也就是每日臺灣時間 21:00。
- UI 不提供 cron 修改；變更排程必須走設定變更、審核、測試與部署流程。
- 每次執行建立 `underwriting_batch_execution`，並彙總總件數、承保、照會與失敗筆數。
- 承保前必須存在有效 `initial_premium_due`，且唯一送金單經程式配對為 `MATCHED`；其他結果建立照會，不得建立有效保單。
- 現階段已完成排程入口、queue schema 與畫面；MyBatis queue coordinator 與逐件 materialization 尚屬下一實作切片，不得宣稱已能自動承保。

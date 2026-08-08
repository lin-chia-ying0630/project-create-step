# 壽險資料 ID 設計

> 參考快照：作為資料建模候選方案，不代表新專案已核准的主鍵或外部識別契約。

## 原則

- 技術識別碼統一使用 UUID v7，由後端在寫入資料庫前產生。
- API 使用標準 36 字元 UUID；資料庫欄位命名為 `{entity}_id`。
- UUID 必須搭配 `PRIMARY KEY` 或 `UNIQUE`，不可把低碰撞機率當成唯一性約束。
- 業務鍵仍保留唯一限制；UUID 不取代保單號碼、案號及代碼表複合鍵。
- 前端新增時不得傳入 ID；修改、刪除及覆核時必須帶回後端核發的 ID。
- 稽核 `recordKey` 保存資料 ID，`key1` 保存可供使用者查詢的保單號碼或使用者 ID。

## ID 一覽

| 資料 | ID 欄位 | 業務唯一鍵 |
|---|---|---|
| 保單契約 | `policyContractId` | `policyNo + policySeq` |
| 保單地址 | `addressId` | `policyContractId + addressTypeCode`（同用途僅一筆有效資料） |
| 電子郵件 | `emailId` | 不限制同用途筆數；以 `primaryFlag` 指定主要資料 |
| 電話／手機 | `phoneId` | 不限制同用途筆數；以 `primaryFlag` 指定主要資料 |
| 主約／附約 | `coverageId` | `policyContractId + coverageItemSeq` |
| 保全案件 | `changeCaseId` | `changeCaseNo` |
| 保全異動項目 | `changeItemId` | `changeCaseId + changeItemCode + recordKey` |
| 欄位異動 | `changeFieldId` | 無額外業務唯一鍵 |
| 檔案快照 | `changeSnapshotId` | `changeItemId + changedRecordType + recordKey` |
| 覆核案件 | `reviewId` | `reviewKey` |
| 覆核稽核事件 | `auditEventId` | 每次狀態事件一筆、不可更新或刪除 |
| 使用者 | `userId` | 登入帳號另設唯一限制 |
| 畫面授權 | `userScreenAuthorizationId` | `userId + functionCode` |
| 代碼定義 | `codeDefinitionId` | `codeGroup + codeField + codeBefore` |

## 聯絡資料代碼

資料表本身區分地址、Email、電話；用途代碼不可再混放資料種類。

| 資料表 | 用途欄位 | 範例 |
|---|---|---|
| `policy_contact_address` | `address_type_code` | `01` 通訊、`02` 戶籍、`03` 收費 |
| `policy_contact_email` | `email_type_code` | `01` 個人、`02` 公司、`03` 通知 |
| `policy_contact_phone` | `phone_type_code` | `11` 通訊電話、`12` 戶籍電話、`21` 手機、`22` 公司、`31` 傳真 |

異動申請使用獨立代碼：

- `001` 地址變更
- `004` 電子郵件變更
- `005` 市內電話變更
- `006` 行動電話變更

同一 `changeCaseId` 可以一次包含多個異動項目，但每個項目必須保存自己的
`changeItemId`、`recordKey`、異動前快照與異動後快照。

## 防重與覆核

待覆核防重鍵：

```text
functionCode + changeItemCode + recordKey
```

其中 `recordKey` 使用 `addressId`、`emailId`、`phoneId` 或 `coverageId`。新增資料尚無正式 ID
時，由後端先產生 UUID v7，再建立覆核資料，因此新增與修改可使用同一套流程。

## 安全

- UUID 不構成授權機制；每次查詢或異動仍須核對目前使用者的畫面授權及資料權限。
- 後端不得接受前端在新增操作指定的 UUID。
- Log 不記錄完整地址、Email、電話及快照內容，只記錄 ID、功能代碼、結果與 trace ID。
- 稽核事件採 append-only，UUID 只負責事件識別，不允許覆蓋既有事件。

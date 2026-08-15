# 全系統資料字典與欄位元件規格

## 1. 目標

建立全系統唯一的 Data Dictionary（DD，資料字典）來源，消除 Vue 畫面中的英文代碼與繁中名稱硬編碼。

所有可選代碼欄位必須依下列資料流呈現：

```text
new_contract.code_definition／後端 domain enum
                    ↓
         統一 Data Dictionary API
                    ↓
          前端 typed API／cache
                    ↓
 DataDictionarySelect／ReadonlyCodeField
                    ↓
                 業務畫面
```

## 2. 唯一來源規則

| 類型 | 權威來源 | 前端責任 |
|---|---|---|
| 營運可維護代碼 | `new_contract.code_definition` | 只呈現 API 回傳的 `code` 與 `description` |
| 固定封閉狀態 | 後端所屬 feature 的 domain enum | 透過 metadata API 取得，不另建 TypeScript 對照表 |
| 商品、題庫等獨立主檔 | 所屬主檔與專屬 API | 不複製到 DD |
| 布林選項 | 共用 `BooleanChoiceField` | 只處理 `true／false`，不建立業務代碼 |
| 自由文字、日期、金額 | 共用 `FormField` | 不放入 DD |
| 頁次與畫面區段 | screen metadata／導航元件 | 不放入業務代碼表 |

## 3. 共用欄位元件

### 3.1 `DataDictionarySelect.vue`

取代現有畫面中直接撰寫的 `<select><option value="...">中文</option></select>`。

必要輸入：

| prop | 說明 |
|---|---|
| `modelValue` | 正式保存的英文／標準代碼 |
| `codeGroup` | DD 群組 |
| `codeField` | DD 欄位 |
| `label` | 欄位繁中名稱 |
| `required` | 是否必填 |
| `disabled` | 是否停用 |
| `placeholder` | 未選取提示 |

顯示格式固定為 `代碼｜繁體中文說明`，元件只回傳代碼。

### 3.2 `BooleanChoiceField.vue`

統一呈現下列布林欄位：

- 是／否
- 同意／不同意
- 適合／不適合
- 適用／不適用

元件必須保留真正的 boolean，不得轉成未定義的字串代碼。

### 3.3 `ReadonlyCodeField.vue`

用於新契約階段、契約狀態、核保決定及覆核狀態等唯讀欄位，固定顯示 `代碼｜繁中說明`。畫面不得自行以 switch 翻譯。

## 4. 應納入 DD 欄位

| code group | code field | 繁中名稱 | 目前主要使用畫面 |
|---|---|---|---|
| `customer-master` | `customer_type_code` | 客戶類型 | 客戶建立 |
| `customer-master` | `identity_type_code` | 證件類型 | 客戶建立 |
| `customer-master` | `gender_code` | 性別 | 客戶建立 |
| `customer-master` | `organization_type_code` | 組織類型 | 客戶建立 |
| `common` | `country_code` | 國家／地區 | 客戶建立 |
| `customer-contact` | `postal_code3` | 郵遞區號 | 客戶建立 |
| `customer-kyc` | `occupation_code` | 職業 | 客戶建立 |
| `customer-kyc` | `source_of_funds_code` | 資金來源 | 客戶建立、保單登打 |
| `customer-kyc` | `insurance_purpose_code` | 投保目的 | 客戶建立、保單登打 |
| `new-contract` | `channel_code` | 受理通路 | 保單登打 |
| `new-contract` | `relationship_to_insured_code` | 與被保險人關係 | 保單登打 |
| `new-contract` | `currency_code` | 幣別 | 保單登打 |
| `new-contract` | `coverage_item_type_code` | 主約／附約 | 保單登打 |
| `new-contract` | `payment_mode_code` | 繳別 | 保單登打 |
| `new-contract` | `beneficiary_designation_code` | 受益人指定方式 | 保單登打 |
| `new-contract` | `health_answer_code` | 健康告知答案 | 保單登打 |
| `new-contract` | `signature_method_code` | 簽署方式 | 保單登打 |
| `new-contract` | `authorization_type_code` | 首期保費授權方式 | 保單登打 |
| `new-contract` | `payer_role_code` | 繳款人身分 | 保單登打、首期保費 |
| `new-contract` | `attachment_type_code` | 附件類型 | 保單登打 |
| `new-contract` | `attachment_owner_role_code` | 附件所屬對象 | 保單登打 |
| `new-contract` | `customer_risk_level_code` | 客戶風險等級 | 保單登打 |
| `new-contract` | `product_risk_level_code` | 商品風險等級 | 保單登打 |
| `new-contract` | `payment_channel_code` | 繳費管道 | 首期保費 |
| `new-contract` | `policy_reversal_reason_code` | 承保撤回原因 | 承保撤回 |

## 5. 固定狀態 metadata

下列欄位由後端 domain enum 管理，不應再建立資料庫與前端第二份對照：

| metadata field | 唯一來源 |
|---|---|
| `review_operation_type` | `ReviewOperationType` |
| `review_status_code` | `ReviewStatus` |
| `application_status_code` | `NewContractApplicationStatus` |
| `underwriting_decision_code` | 核保結果 domain enum |

若既有資料庫 `code_definition` 已經擁有同一正式欄位，必須先判定它是營運可維護代碼或封閉狀態，不能同時保留 enum 與資料庫兩個來源。

## 6. 不納入 DD 的項目

- 商品代碼與商品名稱：使用 `insurance_product_definition`。
- 健康告知題目：應使用正式題庫主檔，不使用一般 code table。
- 姓名、地址、日期、金額、原因說明及附件檔名。
- 查詢、清除、核准、退回等操作按鈕文字。
- 頁碼、上一頁、下一頁及每頁筆數。
- 要保書十頁區段名稱與查詢明細頁籤；此類資料屬 screen metadata。

## 7. 前端禁止事項

正式調整完成後，業務畫面不得再出現：

```vue
<option value="AGENT">業務員</option>
<option value="BANK">銀行保險</option>
```

也不得建立：

```ts
const labels = { AGENT: '業務員', BANK: '銀行保險' }
```

應改由共用欄位元件取得：

```vue
<DataDictionarySelect
  v-model="form.channelCode"
  code-group="new-contract"
  code-field="channel_code"
  label="通路"
  required
/>
```

## 8. 分頁與效能

- 查詢頁使用後端 `PageResult<T>`、`LIMIT` 與 `OFFSET`。
- 一般小型代碼選單採 lazy cache，同一登入期間同一 `codeGroup／codeField` 只查一次。
- `occupation_code` 等大量資料不得由一般下拉一次載入全部；應使用後端搜尋、分頁及可輸入的 autocomplete 欄位。
- 登出時必須清除 DD cache。

## 9. 驗收條件

1. 上表代碼欄位不再於 Vue template 硬編碼中英對照。
2. 同一英文代碼只有一個正式繁中來源。
3. DD 元件具有 loading、empty、error、disabled 與 required 狀態。
4. 後端仍驗證代碼有效性，不能只依賴前端選單。
5. 前端 type-check、unit test、build 與 320px／390px／桌面 viewport 驗證通過。
6. Mapper SQL、migration、API DTO、TypeScript 型別與畫面欄位一致。

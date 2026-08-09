# 分頁與排序規範

> 適用範圍：所有後端列表查詢 API 的分頁參數、排序參數、回應 metadata 格式與前端整合方式。

## 1. 分頁參數

所有列表查詢 API 使用以下統一參數名稱（Query String）：

| 參數 | 型態 | 預設值 | 說明 |
|---|---|---|---|
| `page` | int | `1` | 頁碼，從 **1** 起算（非 0） |
| `pageSize` | int | `20` | 每頁筆數 |

```
GET /api/v1/claims?page=2&pageSize=20
```

### 上限規則

| 查詢類型 | 最大 pageSize | 說明 |
|---|---|---|
| 一般業務查詢 | `100` | 保單清單、理賠清單、核保清單等 |
| 代碼定義查詢 | `500` | `code_definition` 整批載入 |
| 匯出用查詢（需授權） | `1000` | 需特定 functionCode，啟用游標分頁 |

超過上限時回傳 `400`，錯誤碼 `VAL-0003`，訊息：「每頁筆數上限為 {上限值}。」

## 2. 排序參數

| 參數 | 格式 | 範例 |
|---|---|---|
| `sort` | `{欄位},{asc\|desc}` | `sort=created_at,desc` |

- 欄位名稱使用 **camelCase API canonical name**（與回應欄位一致）。
- 不支援多欄位排序時，單一 `sort` 參數即可；若需多欄位排序，允許重複參數：
  ```
  GET /api/v1/policies?sort=effectiveDate,desc&sort=policyNo,asc
  ```
- 欄位不在允許清單時回傳 `400`，錯誤碼 `VAL-0004`，訊息：「不支援以 {fieldName} 排序。」
- 每個 API 必須在 OpenAPI 規格中列出允許的排序欄位（`enum` 限制）。

### 各模組預設排序

| 模組 | 預設排序 |
|---|---|
| 保單查詢 | `effectiveDate desc` |
| 保全案件 | `policyChangeApplicationDate desc` |
| 理賠清單 | `claimApplicationDate desc` |
| 核保清單 | `applicationDate desc` |
| 代碼定義 | `codeGroup asc, codeField asc` |

## 3. 回應 Metadata 格式

分頁查詢回傳的 `data` 統一使用 `PageResult<T>` 結構：

```java
public record PageResult<T>(
    List<T> items,       // 本頁資料清單
    long totalItems,     // 符合條件的總筆數
    int page,            // 目前頁碼
    int pageSize,        // 請求的每頁筆數
    int totalPages       // 總頁數 = ceil(totalItems / pageSize)
) {}
```

回應範例：

```json
{
  "success": true,
  "errorCode": null,
  "message": null,
  "data": {
    "items": [ ... ],
    "totalItems": 253,
    "page": 2,
    "pageSize": 20,
    "totalPages": 13
  }
}
```

## 4. MyBatis 分頁實作

使用 **PageHelper** 外掛（`pagehelper-spring-boot-starter`）：

```java
// Service 層
public PageResult<PolicySummaryResponse> queryPolicies(PolicyQueryRequest req) {
    PageHelper.startPage(req.page(), req.pageSize())
              .orderBy(SortHelper.toOrderByClause(req.sort(), ALLOWED_SORT_FIELDS));

    List<PolicyContract> rows = policyContractDao.findByCondition(req);
    PageInfo<PolicyContract> pageInfo = new PageInfo<>(rows);

    return new PageResult<>(
        rows.stream().map(policyMapper::toSummaryResponse).toList(),
        pageInfo.getTotal(),
        req.page(),
        req.pageSize(),
        pageInfo.getPages()
    );
}
```

### 排序安全規則

- 排序欄位必須通過白名單驗證，**不得直接將前端字串拼入 ORDER BY**（SQL Injection 防護）。
- 使用 `SortHelper` 統一轉換，白名單在各 DAO 常數類別定義：

```java
public class PolicySortFields {
    public static final Set<String> ALLOWED = Set.of(
        "effectiveDate", "policyNo", "contractDate", "maturityDate"
    );
}
```

## 5. 前端整合規則

- 清單固定一筆資料一列，欄位內容預設不換行；欄位總寬超過 viewport 時，由表格容器提供水平捲軸，禁止造成整頁水平捲動或把同筆資料折成多列。

- 分頁組件統一使用專案共用的 `<PaginationBar>` 元件，不各自實作。
- URL query string 是分頁與排序的唯一來源（`?page=2&pageSize=20&sort=effectiveDate,desc`）；store 可衍生讀取但不得維護第二份可變狀態。
- 分頁切換時滾動至頁面頂部。

## 6. 效能注意事項

- `totalItems` 計數查詢與資料查詢分開執行（PageHelper 會自動處理）。
- 若 `totalItems > 10,000`，可考慮只回傳「是否有下一頁」而非精確總數（游標分頁）；需業務確認後另行規格化。
- 列表查詢必須有對應索引涵蓋查詢條件欄位，禁止全表掃描上 production。

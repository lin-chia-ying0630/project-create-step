# 例外處理規範

> 適用範圍：所有後端 Spring Boot 3 服務的例外分類、HTTP 狀態碼映射、錯誤碼格式與回應結構。

## 1. 統一回應結構

所有 API 回應（成功、業務錯誤、系統錯誤）一律使用 `ResponseBodyDto<T>` 包裝：

```java
public record ResponseBodyDto<T>(
    boolean success,
    String message,      // 成功訊息；錯誤時為 null
    String errorCode,    // 錯誤碼；成功時為 null
    String errorMessage, // 繁中錯誤說明；成功時為 null
    T data               // 錯誤時為 null
) {}
```

成功時 `errorCode`、`errorMessage` 必須為 `null`；失敗時 `message`、`data` 必須為 `null`。HTTP status 負責分類，`errorCode` 供程式判斷，顯示文字使用 `message` 或 `errorMessage`。

## 2. 例外分層

| 層級 | 類別名稱 | 說明 |
|---|---|---|
| 業務規則違反 | `BusinessRuleException` | 違反保險業務不變量，例如重複覆核、日期衝突 |
| 資料驗證失敗 | `ValidationException` | 欄位格式、必填、長度、enum 值錯誤 |
| 資源衝突 | `ResourceConflictException` | 同一 unique_key 已有處理中案件 |
| 資源不存在 | `ResourceNotFoundException` | 保單號碼、案件號碼查無資料 |
| 權限不足 | `ForbiddenException` | 使用者無該 functionCode 授權 |
| 系統內部錯誤 | `SystemException` | 非預期例外，不暴露堆疊給外部 |

## 3. HTTP 狀態碼映射

| 例外類別 | HTTP Status | 使用時機 |
|---|---|---|
| `ValidationException` | 400 Bad Request | 輸入格式或必填錯誤 |
| `ResourceNotFoundException` | 404 Not Found | 查無保單、案件等業務資料 |
| `ResourceConflictException` | 409 Conflict | 同 key 已有處理中覆核 |
| `BusinessRuleException` | 422 Unprocessable Entity | 輸入格式正確但違反業務規則 |
| `ForbiddenException` | 403 Forbidden | 缺少 functionCode 授權 |
| 身份驗證失敗 | 401 Unauthorized | Token 無效或過期 |
| `SystemException` | 500 Internal Server Error | 非預期系統錯誤 |

## 4. 錯誤碼格式

格式：`{模組代碼}-{四位數字}`

| 模組 | 模組代碼 | 範例 |
|---|---|---|
| 要保申請 | `APP` | `APP-1001` |
| 核保 | `UW` | `UW-2001` |
| 保全變更 | `CHG` | `CHG-3001` |
| 理賠 | `CLM` | `CLM-4001` |
| 保費計算 | `PRM` | `PRM-5001` |
| 保單查詢 | `POL` | `POL-6001` |
| 通用驗證 | `VAL` | `VAL-0001` |
| 系統錯誤 | `SYS` | `SYS-9001` |

### 常用通用錯誤碼

| 錯誤碼 | 說明 |
|---|---|
| `VAL-0001` | 必填欄位缺漏 |
| `VAL-0002` | 欄位格式錯誤 |
| `VAL-0003` | 欄位值超出允許範圍 |
| `VAL-0004` | 枚舉代碼不在允許清單 |
| `SYS-9001` | 系統內部錯誤，請稍後再試 |
| `SYS-9002` | 資料庫連線異常 |

## 5. GlobalExceptionHandler 實作規則

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ValidationException → 400，回傳各欄位錯誤清單
    // BusinessRuleException → 422，回傳 errorCode + errorMessage
    // ResourceConflictException → 409
    // ResourceNotFoundException → 404
    // ForbiddenException → 403
    // 其他未攔截例外 → 500，message 固定為「系統內部錯誤，請聯絡系統管理員」
    //   - 不得將 exception.getMessage() 或 stack trace 回傳給前端
    //   - 必須將完整 stack trace 寫入後端 Log（含 requestId、traceId）
}
```

## 6. 個資相關例外規則

- 錯誤訊息不得含有身分證號、銀行帳號、健康資料原始值。
- 查無使用者時，統一回傳 `404`，不區分「帳號不存在」與「密碼錯誤」（防止帳號列舉）。
- 個資存取被拒時，Log 只記錄 userId、functionCode、requestId，不記錄被查詢的個資內容。

## 7. 多欄位驗證錯誤格式

本基準不以 `data` 偷渡錯誤清單。先回傳第一筆依固定欄位順序排序的錯誤；若產品需要一次回傳多筆，須先在 OpenAPI 增加明確的 `validationErrors` 欄位並同步更新 Java、TypeScript 與測試，不得臨時改變 `data` 語意：

```json
{
  "success": false,
  "message": null,
  "errorCode": "VAL-0001",
  "errorMessage": "要保人客戶代碼為必填",
  "data": null
}
```

# 例外處理規範

## 共用契約

所有業務 API 的成功與失敗回應使用專案既有 `ResponseBodyDto<T>`；health、OpenAPI、串流、外部 webhook 或框架協定端點只有明確 allowlist 才可例外。不得以文件範例新增另一套 wrapper 或不存在的例外類別。

## HTTP 分類

| 狀況 | HTTP Status |
|---|---|
| 輸入驗證失敗 | 400 Bad Request |
| 身分驗證失敗 | 401 Unauthorized |
| 權限不足 | 403 Forbidden |
| 資源不存在 | 404 Not Found |
| 資源或版本衝突 | 409 Conflict |
| 格式正確但違反業務規則 | 422 Unprocessable Entity |
| 未分類系統錯誤 | 500 Internal Server Error |

錯誤碼格式為 `{MODULE}-{NNNN}`。`MODULE` 必須取自實際 feature 已定義的 `ErrorCode`，文件中的 `<MODULE>-0001` 只是格式佔位符，不是預先核准的錯誤碼。

## 實作邊界

- 固定錯誤碼與繁中訊息集中於所屬領域的 `ErrorCode` enum。
- 業務例外接受既有 `ErrorCode` contract，不在 throw 處重複硬編碼。
- 全域 handler 依例外類型映射 HTTP status，並包裝未分類錯誤。
- 不存在的錯誤類別不因本規範而建立；先沿用目前專案的 `BusinessException`、`ErrorCode` 與 handler 設計。
- 500 回應不得包含 exception message、SQL、stack trace 或內部路徑；後端 log 以 requestId／traceId 追蹤。

## 驗證錯誤

成功與失敗欄位的 nullability 以 `ResponseBodyDto` 現有契約為準。多欄位錯誤若需要陣列，必須先修改 OpenAPI、Java DTO、TypeScript type 與測試，不得臨時以 `data` 偷渡不同語意。

## 敏感資料

錯誤與 log 不得包含身分證號、銀行帳號、健康資料、密碼、token 或憑證。對外訊息使用可理解的繁體中文，程式判斷只依錯誤碼及 HTTP status，不比較中文文字。

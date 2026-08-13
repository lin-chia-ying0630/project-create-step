# 稽核 Log 規範

> 適用範圍：後端業務操作稽核（audit trail）、系統 Log 分級、requestId/traceId 規則與保存年限。

## 1. 稽核範圍

### 必須寫稽核的操作

| 操作 | 說明 |
|---|---|
| 所有 POST（新增） | 建立要保申請、保全案件、理賠申請等 |
| 所有 PUT / PATCH（修改） | 更新任何業務資料 |
| 所有 DELETE（刪除/終止） | 邏輯刪除、狀態終止 |
| 覆核狀態變更 | P → S（核准）、P → C（取消） |
| 權限異動 | 新增/移除使用者角色、畫面授權 |
| 登入 / 登出 / 登入失敗 | 身份驗證事件 |
| 個資查詢 | 查詢含個人健康、財務、身分證件的 API |

### 不需要稽核的操作

- 代碼定義查詢（`code_definition`）
- 純查詢列表（不含個資等級資料）
- 系統 health check

## 2. 稽核事件資料結構

### 畫面稽核欄位契約

所有業務清單及明細畫面固定顯示 `createdBy`、`createdAt`、`updatedBy`、`updatedAt`、`reviewerId`、`reviewedAt`。六個欄位由查詢 API 回傳；尚未覆核時保留欄位並顯示「尚未覆核」，不得隱藏，也不得由前端以登入人或目前時間推測。敏感資料查詢仍須另外寫入 `QUERY_PII` 稽核事件。

所有正式業務表 DDL 必須直接包含下列欄位：

```sql
created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
updated_by VARCHAR(100) NOT NULL COMMENT '最後修改人員',
updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最後修改時間',
reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為 NULL',
reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為 NULL'
```

覆核核准套用正式異動時，修改與覆核欄位、正式資料及成功稽核必須在同一交易更新。資料表保存目前責任欄位，append-only audit event 保存完整歷程，兩者不可互相取代。

稽核事件寫入各模組定義的 append-only `business_audit_event`（邏輯名稱）。保全模組可依已確認 schema 映射為 `change_review_audit`，其他模組不得直接沿用該表名：

```java
public record AuditEvent(
    String auditEventId,      // UUID v7
    String reviewId,          // 覆核案件 ID（非覆核操作可為 null）
    String functionCode,      // 功能代碼，對應 user_screen_authorization
    String operationType,     // CREATE / UPDATE / DELETE / APPROVE / REJECT / LOGIN / QUERY_PII
    String operatorId,        // 操作人員 userId
    String requestId,         // 本次 HTTP 請求唯一 ID
    String traceId,           // 分散式追蹤 ID（目前與 requestId 相同，未來串 Zipkin）
    String key1,              // 可查詢業務鍵（policy_no 或 userId），不含個資
    String recordKey,         // 被操作資料的 UUID
    String contentBefore,     // 操作前快照 JSON（個資欄位遮罩後）
    String contentAfter,      // 操作後快照 JSON（個資欄位遮罩後）
    String resultCode,        // SUCCESS / FAILURE / FORBIDDEN
    String errorCode,         // 失敗時的業務錯誤碼
    Instant occurredAt        // 事件發生時間（UTC）
) {}
```

### Append-only 強制規則

- 業務稽核表禁止應用程式執行 `UPDATE` 與 `DELETE`；DB 權限或 trigger 必須由 migration 建立並以整合測試證明，不得只寫在文件。
- 正式異動與 `SUCCESS` 稽核同一交易，同成同敗。
- 業務失敗、登入失敗與拒絕存取可在主交易 rollback 後，寫入分離的 `security_audit_event`；不得留下看似成功的正式業務軌跡。

## 3. requestId 與 traceId 規則

### requestId

- 每個 HTTP 請求由後端產生一個 UUID v4 作為 `requestId`。
- 優先讀取請求 Header `X-Request-ID`（前端或 API Gateway 帶入）；若無則自動產生。
- 回應 Header 附上 `X-Request-ID`，方便前端與 Log 對應。
- 所有 Log 輸出必須含 `requestId`（使用 MDC 自動注入）。

### traceId

- 目前與 `requestId` 相同；日後串接 OpenTelemetry / Zipkin 時改用標準格式。
- 跨服務呼叫時透過 `X-Trace-ID` Header 傳遞。

```java
// Filter 實作
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, ...) {
        String requestId = Optional.ofNullable(req.getHeader("X-Request-ID"))
                                   .orElse(UUID.randomUUID().toString());
        MDC.put("requestId", requestId);
        MDC.put("traceId", requestId);  // 暫時相同
        response.addHeader("X-Request-ID", requestId);
        try {
            filterChain.doFilter(req, response);
        } finally {
            MDC.clear();
        }
    }
}
```

## 4. Log 分級規則

| 等級 | 使用時機 | 範例 |
|---|---|---|
| `ERROR` | 系統例外、無法恢復的錯誤 | DB 連線失敗、未攔截例外 |
| `WARN` | 業務規則違反、Deadlock 重試、棄用 API 被呼叫 | `CHG-3001 重複保全案件` |
| `INFO` | 業務操作里程碑 | 覆核核准、保單生效、理賠付款 |
| `DEBUG` | 開發除錯，**production 禁止輸出** | SQL 參數、中間計算值 |

### 禁止寫入 Log 的資料

- 身分證明文件號碼（`identity_document_no`）
- 銀行帳戶號碼（`bank_account_no`）
- 完整信用卡號
- 健康告知內容（`health_disclosure_answer`）
- 任何包含「密碼」、「token」、「secret」的欄位值

違反時須在 Code Review 攔截；CI 可加入 secret scanning 工具輔助偵測。

## 5. 個資查詢稽核

查詢含個資的 API（健康告知、身分文件、銀行帳號）必須額外寫一筆 `QUERY_PII` 稽核事件：

```
operationType = QUERY_PII
key1          = policyNo 或 userId
recordKey     = 被查詢資料的 UUID
contentBefore = null（查詢不需快照）
contentAfter  = null
resultCode    = SUCCESS 或 FORBIDDEN
```

## 6. 保存期限

| 資料類型 | 保存期限 | 必要依據 |
|---|---|---|
| 業務稽核軌跡 | 待法遵核定 | 適用地區、業務類型、正式法規／內規、版本、核准日 |
| 系統 Log | 待資安與法遵核定 | 資料分類、營運需求、正式政策、版本、核准日 |
| 登入 / 登出 Log | 待資安與法遵核定 | 身分驗證政策與適用法規 |
| 個資查詢稽核 | 待法遵核定 | 個資類型、處理目的與適用法規 |

- 未記錄正式來源前，不得自行填入年數或宣稱法規要求。保存、封存與銷毀必須同時定義 legal hold 例外。
- 封存策略（冷儲存媒體、加密）由資訊安全部門另行規範。

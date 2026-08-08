# 交易與併發規範

> 適用範圍：後端 Spring Boot 3 + MyBatis + MySQL 的交易邊界、樂觀鎖、防重與 Deadlock 處理策略。

## 1. 交易邊界原則

### 必須使用 @Transactional 的情境

| 情境 | 說明 |
|---|---|
| 保全覆核套用（POS 模組範例） | 同時寫案件、正式表與成功業務稽核，三者須原子完成；實際表名以該模組 schema 為準 |
| 要保申請建立 | 同時建立 `insurance_application`、`application_party`、`health_disclosure` |
| 理賠申請建立 | 同時建立 `claim_case`、`claim_event`、`claim_document` |
| 核保決定 | 同時更新 `underwriting_case`、建立 `underwriting_condition`、寫稽核 |
| 任何涉及多表寫入的操作 | 只要一次請求寫超過一張表，必須在同一交易 |

### 不需要 @Transactional 的情境

- 純查詢（SELECT）操作
- 只寫一張表且無相依的單筆 INSERT
- 讀取代碼定義（`code_definition`）

### 傳播行為規則

```
Service 層：        @Transactional(propagation = REQUIRED) ← 一般業務操作與成功稽核
失敗／資安事件寫入：@Transactional(propagation = REQUIRES_NEW) ← 主交易 rollback 完成後另行記錄
查詢方法：          @Transactional(readOnly = true)
```

正式表、案件狀態與代表成功結果的業務稽核必須同成同敗。只有失敗嘗試、拒絕存取等不宣稱業務成功的安全事件，才可在主交易 rollback 後由外層攔截器或事件處理器以獨立交易寫入。

## 2. 樂觀鎖規範

### 適用資料表

所有可被保全覆核修改的正式表必須有 `record_version` 欄位：

```sql
record_version BIGINT NOT NULL DEFAULT 0
```

### 更新時的版本檢查

```xml
<!-- MyBatis Mapper -->
<update id="updateWithVersion">
  UPDATE policy_contract
  SET policy_status = #{policyStatus},
      record_version = record_version + 1,
      updated_by = #{updatedBy},
      updated_at = NOW()
  WHERE policy_contract_id = #{policyContractId}
    AND record_version = #{recordVersion}
</update>
```

- `affectedRows == 0` 時拋出 `ResourceConflictException`（`CHG-3010`），訊息：「資料已被他人修改，請重新整理後再試。」
- 覆核套用前必須再次讀取正式表的 `record_version`，與覆核建立時的快照版本比對。

## 3. 防重機制

### DB 層（強制）

MySQL 不使用 partial unique index 假設。以獨立待審鎖表保證唯一性：

```sql
CREATE TABLE pending_business_lock (
  function_code VARCHAR(50) NOT NULL,
  unique_key VARCHAR(200) NOT NULL,
  case_id VARCHAR(36) NOT NULL,
  PRIMARY KEY (function_code, unique_key),
  UNIQUE KEY uk_pending_case (case_id)
);
```

### 應用層（輔助）

- 建立案件時在同一交易新增 lock row 與小寫 `acceptance_status = 'p'` 的案件；重複鍵轉為 `409`。
- 核准、退回或取消時，在同一交易完成正式異動、成功稽核與 lock row 刪除。
- 若流程需要先讀後寫，鎖定的是必然存在的穩定父資料或 lock row；不可對不存在的案件列做無效的 `FOR UPDATE` 後就認為安全。

```java
// 偽代碼
@Transactional
public void submitChangeCase(...) {
    policyDao.lockPolicyForUpdate(policyNo);
    pendingBusinessLockDao.insert(functionCode, uniqueKey, newCase.id());
    changeCaseDao.insert(newCase.withAcceptanceStatus("p"));
}
```

## 4. Deadlock 處理策略

### 預防（優先）

- 多表寫入時固定操作順序（例如永遠先鎖 `policy_contract`，再鎖 `policy_change_case`）。
- 避免在同一交易內對同一資料表多次讀後寫（改用一次 UPDATE）。
- 成功稽核依固定鎖順序在主交易內最後寫入；避免稽核交易反向鎖回業務表。

### 偵測與重試

```java
@Retryable(
    retryFor = { DeadlockLoserDataAccessException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
@Transactional
public void applyChangeReview(String reviewId) { ... }
```

- 最多重試 3 次，間隔 100ms、200ms、400ms。
- 超過重試次數後拋出 `SystemException`（`SYS-9003`），訊息：「操作衝突，請稍後再試。」
- 每次 Deadlock 重試必須寫 WARN 等級 Log，含 `reviewId`、`requestId`、重試次數。

## 5. 分散式環境補充

現行架構為單一 MySQL 節點，以 DB 層 unique constraint 作為防重主力。若日後水平擴展為多 Pod：

- 引入 Redis 分散式鎖（`SETNX + TTL`）作為 DB 防重的前置快速失敗層。
- 鎖的 key 格式：`change:lock:{functionCode}:{uniqueKey}`，TTL 設 30 秒。
- 取鎖失敗立即回傳 `409`，不排隊等待。
- **此項為未來規劃，目前不實作。**

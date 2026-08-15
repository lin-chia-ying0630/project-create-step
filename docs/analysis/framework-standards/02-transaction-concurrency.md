# 交易與併發規範

## 交易邊界

- 一次業務操作涉及多表寫入、狀態轉換或成功稽核時，由 Service implementation 的 public method 使用 `@Transactional` 包覆。
- 正式資料與代表成功結果的稽核同成同敗。
- 失敗嘗試或資安事件只有在主交易 rollback 後仍須保留時，才由獨立元件以新交易記錄。
- 純查詢可使用 read-only transaction；單表寫入是否需要交易，依其 constraint、事件與後續副作用判斷。
- 本文件不指定資料表、DAO、Service method 或狀態碼；實作必須從現有 schema 與 feature contract 取得。

## 樂觀鎖

需要偵測同時修改的資料才增加版本欄位。更新 SQL 必須同時比對主鍵與版本，成功時遞增版本；`affectedRows == 0` 轉為 409 衝突。欄位名稱與錯誤碼依實際 schema／領域定義，不套用文件範例名稱。

## 防重

- 先識別穩定的業務唯一鍵，再以資料庫 unique constraint 作最後防線。
- Maker／Checker 類流程若需「同功能＋同業務鍵僅一筆待審」，使用獨立 lock row 或等效已證明設計；不得假設 MySQL 支援 partial unique index。
- 先讀後寫時鎖定必然存在的資料列或先建立唯一 lock row；對不存在的 row 執行 `FOR UPDATE` 不構成防重。
- lock、業務寫入、狀態完成與 lock 釋放必須位於明確交易。

## Deadlock

- 多表操作使用固定鎖定與寫入順序，交易內不執行不必要的外部 I/O。
- 只對可安全重試且具冪等保障的操作重試，最多 3 次。
- 每次重試記錄 requestId、業務鍵遮罩值與次數，不記錄敏感內容。
- 超過上限轉為專案既有系統錯誤，不在共用文件預先指定類別或錯誤碼。

## 分散式環境

資料庫 unique constraint 仍是最終一致性防線。只有觀測到跨節點競爭且已定義 TTL、owner、釋放與失敗策略時才加入分散式鎖；不得因未來可能擴充而預先寫入特定 Redis key 或業務名稱。

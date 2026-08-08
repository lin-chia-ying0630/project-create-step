# 對帳控制清單

## Control totals

- Record count：received、accepted、rejected、duplicate、processed、pending、failed。
- Amount：依 currency 分組的 premium、refund、claim payment、commission、fee 與 tax。
- Balance：opening、movement、closing，並說明 rounding 及 reversal。
- File／batch：sequence、business date、producer、consumer、schema version 與 checksum。

## 狀態與重跑

- 區分 received、validated、staged、applied、acknowledged、rejected、reversed 與 manually resolved。
- 每一狀態轉換使用條件更新、affected rows 或 lock，並保存 audit。
- 重跑前確認上一次 checkpoint、已套用正式資料、已送外部訊息及不可逆 side effect。
- 相同 idempotency key 不重複產生保單異動、收款、退款、理賠給付或通知。
- Reversal 建立相反正式交易及關聯，不刪除原始 audit evidence。

## 差異處理

| 差異 | 必查 |
|---|---|
| Missing | cutoff、延遲、filter、mapping reject、transaction rollback。 |
| Duplicate | retry、檔案重送、key 粒度、並行 worker、ack 遺失。 |
| Amount | scale、rounding、currency、tax／fee、正負號、部分給付。 |
| Status | 非同步順序、舊回覆覆蓋新狀態、人工處置、版本不一致。 |
| Count | header／trailer 定義、空白／無效列、分檔、跨日。 |

## 驗證

- 使用同一資料庫引擎驗證 unique constraint、lock、affected rows 與 rollback。
- 驗證中斷於每個 checkpoint 後的安全重跑。
- 驗證重複檔、重複訊息、亂序回覆、partial file、跨日及夏令時間情境。
- Dashboard／報表數字必須能回溯到 immutable evidence，不以 log 作唯一帳務依據。

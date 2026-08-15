# 全站程式效能評估

## 文件目的

集中保存新契約作業系統的前端、API、MyBatis、MySQL、Nginx 與部署效能證據，避免改善方案只存在於對話或分散於不同功能文件。

本文件只記錄效能評估，不包含密碼、token、資料庫連線字串、加密金鑰、正式個資或健康資料。

## 證據等級

- `Confirmed`：由程式、SQL、設定、測試或實際 HTTP 量測直接證明。
- `Inferred`：現象與程式結構支持，但尚未取得指標或執行計畫證明。
- `Unknown`：缺少正式環境 metrics、MySQL `EXPLAIN ANALYZE` 或壓測基準。

## 官方技術資料

| 主題 | 官方來源 | 採用重點 |
|---|---|---|
| Spring Cache | [Spring Framework Cache Abstraction](https://docs.spring.io/spring/reference/integration/cache/strategies.html) | 只快取相同輸入會得到相同結果的查詢；必須定義 cache provider、key 與失效策略。 |
| Spring Boot metrics | [Spring Boot Metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html) | 使用 `http.server.requests`、JVM、system、JDBC 與 Hikari 指標定位 API、CPU、記憶體及連線池瓶頸。 |
| Vue 效能 | [Vue Performance](https://vuejs.org/guide/best-practices/performance) | 優先減少網路與渲染工作；大型不可變資料才評估 `shallowRef`。 |
| Vue Router 懶載入 | [Lazy Loading Routes](https://router.vuejs.org/guide/advanced/lazy-loading) | route component 使用動態 `import()` 進行 code splitting。 |
| MyBatis 設定 | [MyBatis Configuration](https://mybatis.org/mybatis-3/configuration.html) | 預設 local cache 只在同一 SqlSession 內有效，不能取代跨 HTTP 請求快取。 |
| MySQL 查詢計畫 | [Optimizing Queries with EXPLAIN](https://dev.mysql.com/doc/refman/8.4/en/using-explain.html) | 新增索引前先取得實際執行計畫，不以欄位名稱猜測索引。 |
| MySQL 索引 | [Optimization and Indexes](https://dev.mysql.com/doc/refman/8.4/en/optimization-indexes.html) | 以少量可服務多個查詢的複合索引改善 WHERE、JOIN 與 ORDER BY；避免過多索引增加寫入成本。 |

## 公開測試站量測

量測目標：`https://p01--create-web--kkj9gmg9xdcp.code.run/`。

### 低併發基準

| API | HTTP | 觀察時間 |
|---|---:|---:|
| `/api/auth/me` | 200 | 約 1～2.4 秒；高負載恢復期間曾為 4.6～15.1 秒 |
| 代碼表清單 | 200 | 約 2.8～3.9 秒 |
| `occupation_code` 第一頁 | 200 | 約 2.8～3.7 秒 |
| 覆核清單 0 筆 | 200 | 約 3.2～3.9 秒 |
| 靜態 HTML | 200 | 約 0.85 秒 |

### 併發觀察

`Confirmed`：同時送出 12 個初始頁面 API 時，多數請求超過 30 秒；代碼表約 26.2 秒、客戶清單約 13.6 秒、核保清單約 19.8 秒，批次紀錄端點回傳 500。測試發現服務併發餘裕不足後已立即停止，未再重複高併發壓測。

`Inferred`：主要瓶頸位於後端可用運算資源、請求排隊及資料庫往返，不是 Vue 表格渲染或單次回傳 1,324 筆職業代碼。

`Unknown`：缺少 `http.server.requests`、CPU、JVM、Hikari 與 SQL 執行時間指標，尚不能判定各因素的實際占比。

## 全部網頁評估

| Route | 初始請求 | 狀態 | 主要缺口 |
|---|---|---|---|
| `/reviews` | 認證＋待覆核分頁 | 中 | 每頁 count＋page；本機修改已讓 0 筆時跳過 page SQL。 |
| `/code-definitions` | 認證＋代碼表＋預設分頁 | 中 | 本機已改並行與共用快取；後端仍執行 count＋page。 |
| `/customers` | 首次認證＋客戶分頁 | 已改善 | 已依 query mode 停止載入 5 組建檔代碼。 |
| `/customers/new` | 首次認證＋5 組代碼 | 已改善 | 已依 create mode 停止執行客戶分頁查詢。 |
| `/new-contract/applications/new` | 認證＋6 組代碼＋商品定義 | 差 | 首次進入同時發出 7 個 API；商品定義尚未快取。 |
| `/new-contract/applications` | 認證＋保單分頁 | 中 | count＋page，且沒有取消過期請求。 |
| `/new-contract/premiums` | 認證＋2 組代碼 | 良好 | 請求已並行；本機代碼快取可避免重複載入。 |
| `/underwriting/batches` | 認證＋最近 20 筆 | 中／異常 | 資料量有限，但併發觀察時端點回 500，需另行重現根因。 |
| `/underwriting/reviews` | 首次認證＋核保結果＋案件分頁 | 已改善 | 核保結果與案件分頁已並行，核保結果使用可清除記憶體快取。 |
| `/underwriting/inquiries` | 認證＋照會單分頁 | 中 | count＋page，沒有取消過期請求。 |
| `/policies/reversals` | 認證＋可撤回保單分頁 | 中 | count＋page，沒有取消過期請求。 |

## 改善方案與專案現況

| 改善方案 | 目前狀態 | 後續處理 |
|---|---|---|
| Route code splitting | 已實作 | 保留動態 import。 |
| 前端代碼快取 | 本機已實作，未發布 | 驗證登出與代碼維護後清除行為。 |
| 相同進行中請求合併 | 已用於代碼、認證、商品定義與核保結果 | 維持錯誤不快取及明確清除入口。 |
| 初始請求並行 | 代碼頁、首期保費與核保審查已實作 | 後續新增頁面須維持必要請求並行。 |
| 依 route 載入必要資料 | 客戶查詢與建立已依 `mode` 分流 | 其他 route 持續以初始 API 清單驗收。 |
| 過期請求取消 | 覆核與職業搜尋已實作 | 擴充到所有查詢、排序與分頁清單。 |
| 後端真分頁 | 已實作 | 維持 `PageResult` 與 pageSize 上限。 |
| 0 筆跳過 page SQL | 已擴充至覆核、客戶、保單、核保、照會、撤回與代碼 | 新增分頁 Service 時納入測試。 |
| 減少 count＋page 往返 | 未實作 | 評估 window count、延後 count 或第一頁快取。 |
| Spring Cache／Caffeine | 未實作 | 僅用於低變動權威資料，定義 TTL、容量、eviction 與多 instance 策略。 |
| MyBatis 二級快取 | 未實作 | 正式異動與失效風險未釐清前不得直接啟用。 |
| Hikari 連線池 | 已設定 | 取得 active、idle、pending、timeout 指標後再調整。 |
| HTTP／JVM／Hikari metrics | dependency 已存在，未開放 | 只在受保護的內部管理入口提供，不公開敏感資訊。 |
| MySQL `EXPLAIN ANALYZE` | 未執行 | 對實際慢 SQL 保存執行計畫與 rows examined。 |
| 複合索引 | 部分不足 | 以執行計畫確認後用 forward-only migration 新增。 |
| Nginx gzip／靜態快取 | 未明確設定 | 對 hashed assets 設長效 cache；`index.html` 保持可更新。 |
| Nginx upstream keepalive | 未明確設定 | 驗證平台 proxy 架構後再設定。 |

## 建議實作順序

### P0：先降低無效請求與阻塞

1. 客戶查詢與建立依 route `mode` 只載入必要資料。
2. 合併進行中的 `/api/auth/me`；認證成功可使用短期記憶體狀態改善換頁，但後端每個 API 仍必須正式授權。
3. 核保結果與核保案件清單並行載入，核保結果使用可失效快取。
4. 所有查詢清單加入 `AbortController` 與 sequence guard。

### P1：減少資料庫往返

1. 所有分頁在 `totalItems = 0` 時跳過 page SQL。
2. 評估單次 SQL 回傳總筆數與當頁資料，保留空頁與超出頁碼的正確語意。
3. 對代碼定義、商品定義與核保結果建立後端短期快取。

### P2：用指標與執行計畫調整

1. 開放受保護的 HTTP、JVM、CPU、JDBC、Hikari metrics。
2. 收集各 endpoint 的 p50、p95、p99、錯誤率與 active request。
3. 對慢 SQL 執行 `EXPLAIN ANALYZE`，確認實際索引使用與掃描筆數。
4. 以 forward-only Flyway migration 新增經證明有效的複合索引。
5. 設定 Nginx 靜態資產壓縮與 hashed assets 快取。

## 驗收標準

- 每個 route 列出初始 API 數量，不得載入該 route 不使用的資料。
- 相同唯讀主檔在有效快取期間只向後端取得一次。
- 快速切換查詢、排序與分頁時，只有最後一次請求可更新畫面。
- 分頁 API 每次回傳不超過 `pageSize`，不得以 UI 分頁掩蓋後端全量查詢。
- 公開測試站至少記錄單請求與小量併發的 p50、p95、錯誤率。
- 索引變更必須附變更前後 `EXPLAIN ANALYZE`，不得只以推測宣稱改善。
- 快取必須定義 key、容量、TTL／失效、登出清除與錯誤不快取策略。
- 所有效能驗證分開記錄通過、失敗、跳過與未驗證項目。

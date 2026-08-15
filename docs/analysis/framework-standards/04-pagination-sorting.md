# 分頁與排序規範

## 分頁契約

列表 API 統一使用：

| 參數 | 型態 | 預設 | 限制 |
|---|---|---|---|
| `page` | integer | 1 | 從 1 起 |
| `pageSize` | integer | 20 | 一般查詢上限 100 |

回傳 `PageResult<T>`，至少包含 `items`、`totalItems`、`page`、`pageSize`、`totalPages`。若既有共用型別尚未提供某欄位，先完成唯一契約遷移，不在不同功能各建一份。

## 排序

格式為 `sort=<field>,asc|desc`。每個 API 依實際 response canonical field 建立 allowlist 並在 OpenAPI 列出；不得將使用者輸入直接以 `${}` 或字串拼入 `ORDER BY`。預設排序由該 feature 規格決定，共用文件不預設業務欄位。

## 後端實作

- 先搜尋現有分頁工具與 `PageResult`，可共用時直接使用。
- 不在規範強制 repository 尚未使用的分頁套件或假想 DAO／Mapper class。
- 資料查詢與 count 查詢使用相同條件，並對常用條件及排序建立適當索引。
- page、pageSize 與 sort 在進入 Mapper 前完成範圍及白名單驗證。

## 前端整合

- 使用專案實際存在的共用分頁、排序表頭與查詢 Panel 元件。
- URL query 是否作為狀態來源由 route 規格決定；不得另建第二份互相漂移的可變狀態。
- 一筆資料維持一列；寬表格只在表格容器內水平捲動，不造成整頁溢出。

## 大量資料

需要 cursor pagination、匯出或放寬上限時，另行定義權限、穩定排序鍵、next cursor、逾時與資源限制；不得只提高 `pageSize`。

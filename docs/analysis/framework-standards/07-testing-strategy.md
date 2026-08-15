# 測試策略

## 原則

測試應證明目前需求與程式的可觀察行為，不得因文件範例建立專案中不存在的 Service、Store、API、資料表或流程。

## 測試分層

| 測試層 | 建議工具 | 驗證責任 |
|---|---|---|
| Domain／Service 單元測試 | JUnit 5、Mockito、AssertJ | 規則、狀態轉換、錯誤與邊界 |
| Mapper 整合測試 | Testcontainers MySQL | SQL、constraint、lock、DECIMAL、mapping |
| API 整合測試 | Spring Boot Test、MockMvc | 路徑、驗證、HTTP status、統一回應 |
| Vue／TypeScript 單元測試 | Vitest | 轉換、表單、component 與已存在的狀態模組 |
| 關鍵使用者流程 | Playwright | 實際存在且高風險的跨畫面流程 |

類別與檔名依實際 feature 命名，例如 `<Feature>ServiceTest`、`<Feature>MapperTest`、`<Feature>View.spec.ts`；尖括號代表待替換的佔位符，不是要建立的類別。

## 覆蓋率底線

| 範圍 | 最低指令覆蓋率 |
|---|---|
| 保費計算 Service（若專案實際存在） | 90% |
| 業務驗證 Service | 85% |
| 一般 Service | 70% |
| Controller | 以 API 整合測試涵蓋 |

不得為達成數字而測試 private implementation 或排除高風險分支。

## 必測行為

- 成功與必要的 200／201 回應。
- 輸入驗證 400、未授權 401／403、不存在 404、衝突 409、業務規則 422、未分類錯誤 500。
- null、blank、邊界值、日期／金額精度、未知代碼。
- 多表寫入 rollback、樂觀鎖、唯一鍵與需要時的並發鎖定。
- 動態代碼啟停及後端繁中說明。
- 前端 loading、error、empty、success 與 responsive 行為。

只測實際 schema 已定義的 constraint、trigger 與稽核機制，不從規範虛構資料庫物件。

## 測試資料

- 使用完全虛構且可辨識的資料，例如 `TEST-ENTITY-0001`。
- 禁止複製正式個資、健康、財務、身分證件或憑證。
- 每個測試自行建立前置資料並清理；不得依賴執行順序。
- MySQL 特有 SQL、migration、lock 與精度不得以 H2 取代。

## 追溯與交付

每個案例連結需求、規則或缺陷 ID，記錄前置條件、操作、預期 API／DB／稽核結果及清理方式。分開回報通過、失敗、跳過與未執行項目；缺少 Docker 或外部服務時，不得宣稱相應整合測試已通過。

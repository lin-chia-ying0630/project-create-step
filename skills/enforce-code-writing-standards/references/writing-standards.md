# 程式與文件撰寫標準

## Java／Spring

- UTF-8；一個檔案一個 top-level type；package 小寫；class `UpperCamelCase`；method／variable `lowerCamelCase`；constant `UPPER_SNAKE_CASE`。
- 一行只表達一個主要 statement；method 保持單一目的，巢狀過深時以具業務語意的方法拆分。
- dependency 使用 constructor injection 並宣告 `final`；禁止 field injection。
- public／protected method 使用繁中 Javadoc 說明目的、參數、回傳、例外與 transaction／I/O 副作用。
- 註解解釋 why、契約與特殊案例；程式本身負責表達 what。
- 例外不得被吞掉；固定錯誤 code/message 使用領域 `ErrorCode` enum；系統例外不回傳內部細節。

## 設計模式使用準則

先描述問題與變化點，再選模式；模式是降低耦合的手段，不是交付件數。只有單一實作、沒有替換需求且測試不受阻時，維持直接實作。

| 情境 | 優先模式 | 專案落點 | 檢查重點 |
|---|---|---|---|
| 多組同類規則需獨立增減、排序及測試 | Strategy + Composite | 所屬 feature | 每個規則回傳固定結果，不直接寫資料庫；流程負責彙總 |
| 多種已確認類型需要建立不同處理物件 | Factory Method | domain/application service | Factory 只負責選擇，不混入查詢、交易或畫面文案 |
| API 需協調多個 Mapper、加密、PDF 或外部服務 | Facade/Application Service | `service/impl` | Controller 維持薄層；交易邊界放 application service |
| 外部支付、身分驗證或舊系統介面格式不同 | Adapter | `integration/<provider>` | domain 不依賴供應商 DTO；Adapter 完成格式與錯誤轉換 |
| 批次流程骨架固定、個別步驟可替換 | Template Method 或明確 Pipeline | batch service | 不以繼承共享資料狀態；可組合時優先組合 |
| 固定有限狀態或代碼與繁中名稱 | Enum / State transition policy | `domain` | 狀態轉換由後端驗證；動態代碼仍由資料庫管理 |
| PDF、日期、遮蔽等無狀態純轉換 | 專責 utility | `util` | 不存 request state；I/O 失敗轉為集中錯誤碼 |

採用前必答：第二種實作是什麼、選擇條件在哪一層、交易邊界在哪裡、如何單元測試、移除模式後會增加哪種重複或耦合。答不出時先不套模式。

Spring bean 透過 constructor injection 組合策略或 Adapter；不得在業務程式以 `new`、reflection 或 service locator 尋找實作。MyBatis Mapper 是 Persistence Gateway，Java interface 僅定義存取契約，SQL 全部留在 XML。

## MyBatis XML

- Java Mapper 只放 interface method、`@Mapper` 與必要 `@Param`，不含任何 SQL。
- XML `namespace` 等於 Mapper 完整類名，statement `id` 等於 method 名；查詢結果使用明確 result map／type。
- 值使用 `#{}`；`${}` 只允許經後端 allowlist 決定的非資料識別片段，且不得直接接收使用者輸入。
- 複雜查詢分段排版，明確列欄位，不以 `SELECT *` 建立正式契約。

## Vue／TypeScript

- SFC 固定依序為 `<script setup lang="ts">`、`<template>`、`<style scoped lang="scss">`（需要樣式時）。
- component 檔名與 template tag 使用 PascalCase；props 使用 type-based `defineProps`，emits 使用 typed `defineEmits`。
- props 唯讀；子元件透過 emit 通知變更；不得直接修改父層狀態。
- 禁止 `any`、非必要 `as`、大量一行 expression 與在 template 內執行複雜業務計算。
- API 呼叫放 feature typed API；共用 HTTP client 放 `shared/api`；component 管理呈現與互動，不裁決業務規則。
- loading、empty、error、success、disabled 與 accessibility label 必須明確。

## SCSS

- 全域 token、breakpoint、mixin 與共用元件樣式放 `shared/styles/style.scss`。
- 表格、按鈕、表單、分頁與狀態訊息等跨頁視覺契約使用具語意的共用 class；feature 只定義資料欄位、內容與自身版面。
- 搜尋列、分頁導覽、窄寬度堆疊與觸控按鈕等重複響應式行為集中於 `shared/styles` 或 `shared/components`；feature 不得複製相同 breakpoint 規則。
- feature style 只處理自身版面；不得重新宣告相同色彩 token或全域 element rule。
- 優先使用 class，避免過深 selector；狀態名稱表達語意，不以顏色命名。

## 裝置無關的響應式設計

- 所有裝置使用同一份 route、Vue component、DOM、API、驗證與業務功能；只以 CSS／viewport 可用空間調整排列。
- 禁止依 user-agent、作業系統、品牌或裝置型號建立條件分支；不建立手機專用頁、手機專用 API 或內容較少的替代 DOM。
- viewport 改變只能重排、換行或產生局部捲動，不得隱藏必要欄位、狀態、錯誤訊息或操作。
- `index.html` 使用完整 HTML5 結構、`lang="zh-Hant-TW"`、UTF-8 與 `meta viewport`；viewport 至少包含 `width=device-width`、`initial-scale=1`、`viewport-fit=cover`。
- 使用 `100dvh` 處理行動瀏覽器動態工具列，並以 `env(safe-area-inset-*)` 保留瀏海與底部手勢安全區域；`100vh` 只作相容 fallback。
- 保持瀏覽器縮放能力，不設定 `maximum-scale=1` 或 `user-scalable=no`，避免破壞無障礙操作。
- 支援範圍至少從 320px viewport 到桌面；共用 breakpoint 以 `shared/styles/style.scss` 的 token 為準。
- 頁面本身不得水平捲動；`html`、`body`、workspace 與 grid child 必須允許收縮，必要時使用 `min-width: 0`。
- 雙欄、三欄表單在空間不足時改為單欄；輸入、下拉、textarea 與主要操作按鈕使用可用寬度，不得超出 panel。
- 主要操作按鈕與分頁按鈕的可觸控高度至少 44px；錯誤、loading、disabled 與 focus 狀態在窄 viewport 仍須可辨識。
- 側邊導覽、頁籤與檔案分頁可在自身容器橫向捲動，並保留目前項目的視覺狀態。
- 寬表格保留語意化 table，只允許表格自身容器橫向捲動；欄位內容可換行，不得撐出整個 viewport。
- 長代碼、業務鍵、電子郵件與地址使用安全換行，不裁切必要資訊。
- 完成前至少驗證 320×568、390×844 與桌面 viewport；確認相同內容與功能均存在，沒有 body 水平 overflow，且導覽、主要欄位、上一頁／下一頁及提交按鈕可操作。

## 測試與文件

- 測試名稱描述情境與預期結果，採 Arrange／Act／Assert；每個測試只證明一個主要行為。
- README 與規格先寫結果、契約與可重現指令；事實、推論及待確認事項分開。
- 不提交真實個資、token、密碼、機器專屬絕對路徑或無法重現的操作紀錄。

## 參考來源

- Google Java Style Guide。
- Spring Framework dependency injection 與 transaction reference。
- MyBatis 3 Mapper XML reference。
- Vue 3 Style Guide、Props 與 TypeScript guide。
- Oracle Java enum 與 Java language reference。

專案規則可比外部建議更嚴格，例如本專案禁止 MyBatis SQL annotation。

# 前端狀態與 API 邊界

## 目的

本規範只定義可跨功能共用的 Vue 3／TypeScript 邊界，不預設任何尚未存在的 Store、畫面、API 或業務流程。

## 目錄

```text
<frontend-module>/src/
├── features/<feature>/
│   ├── api/       # 該功能 typed API
│   ├── types/     # 該功能 request／response 型別
│   └── views/     # route view
└── shared/
    ├── api/       # 跨功能 HTTP client 與共用 API
    ├── components/
    ├── styles/
    └── types/
```

- 功能專屬程式留在 `features/<feature>/`。
- 只有兩個以上功能以相同責任重複使用時，才移入 `shared/`。
- 不因文件範例建立專案中不存在的 `store`、`composable` 或 module。

## 狀態所有權

依狀態生命週期選擇最小範圍：

| 狀態範圍 | 放置位置 |
|---|---|
| 單一 component 的輸入與顯示狀態 | component 內的 `ref`／`computed` |
| 同一 feature 多個 component 共用 | feature composable 或父層狀態 |
| 跨 route、需保留或由多處修改 | 經證明需要後才建立 Pinia Store |
| Server state | typed API 呼叫結果；明確定義 loading、error、data |

Pinia Store 不是每個 feature 的固定目錄。建立前須指出跨 route 或多消費者需求，命名使用 `<feature>Store.ts`，不得先寫入不存在的業務 Store 名稱。

## API 呼叫

- 所有 HTTP 呼叫由 typed function 封裝，View 不直接拼 URL 或解析任意 JSON。
- Request、Response 與後端 DTO／OpenAPI 欄位逐一一致。
- 共用 HTTP client 處理 base URL、認證、統一回應與共通錯誤；業務錯誤顯示留在 feature。
- loading、error、data 必須由同一個操作邊界更新，失敗後不得保留容易誤認為最新結果的資料。

## 共用資料快取

只有符合下列條件才建立共用快取：

1. 來源為後端權威資料。
2. 兩個以上 feature 使用。
3. 已定義失效時機、使用者隔離與登出清除方式。

代碼定義不得在前端另建翻譯表。是否採 Store、module cache 或查詢函式，依現況決定，不在共用規範指定不存在的實作名稱。

## 權限與錯誤

- 權限由後端裁決；前端隱藏按鈕只改善操作體驗，不能取代 API 授權。
- 未授權、驗證失敗、衝突及系統錯誤依統一 API contract 顯示。
- 不在 View、Store 或元件硬編碼另一份業務錯誤碼、狀態名稱或代碼說明。

## 驗證

- type-check、unit test 與 production build 通過。
- API 型別與實際 response 一致。
- 若新增 Store，驗證 loading／error／data、重試、清除與使用者切換。
- 以 320×568、390×844 與桌面 viewport 驗證同一 route、DOM、資料與功能。

# 專案建立檢查表

按專案類型選用，不需要逐項機械套用。

## 通用

- 目的、使用者與不在範圍內的項目已明確。
- runtime、套件管理器及版本策略已固定。
- 本機啟動、測試、建置與部署指令可重現。
- 設定範例不含秘密資料，必要環境變數有驗證。
- 日誌不輸出憑證或敏感資料。
- README 與實際目錄、指令一致。
- format、lint、test、build 有可執行入口。

## 後端與 API

- persistence model、create request、update request、query response 分離。
- 欄位型別、nullability、預設值、時區與列舉來源一致。
- API success、validation、domain error、unexpected error 契約一致。
- 身分驗證、授權與資源所有權在服務端執行。
- 資料庫變更使用可追蹤且向前演進的 migration。
- 多表或狀態變更定義 transaction、concurrency 與 idempotency。
- contract test 使用真實資料庫行為涵蓋關鍵欄位。

## 前端與 UI

- API type 與 transport contract 一致，避免重複手寫動態代碼與翻譯。
- loading、empty、error、success、permission denied 狀態完整。
- 表單驗證提升操作體驗，但安全與業務規則仍由後端裁決。
- 元件處理呈現，store 管理客戶端狀態，服務端管理業務規則。
- 可見文字、無障礙標籤與日期數字格式符合指定語系。

## 部署與維運

- dev、test、staging、production 差異由設定管理。
- health、readiness 與 migration 執行時機明確。
- container port、host port、service name 與文件一致。
- 具備可觀測的錯誤、request correlation 與必要 metrics。
- 部署失敗與資料 migration 有回復或向前修復策略。

## 完成證據

- 記錄實際執行的驗證命令及結果。
- 對關鍵流程提供 API、UI 或整合測試證據。
- 清楚區分已通過、未執行與因外部條件阻塞的驗證。

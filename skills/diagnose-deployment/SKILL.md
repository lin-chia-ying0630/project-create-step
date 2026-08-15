---
name: diagnose-deployment
description: Diagnose local Docker Compose or hosted deployment failures involving images, environment variables, secrets, ports, Spring Boot, Flyway, MySQL, Nginx, API health, or browser behavior. Use when a build, startup, migration, health check, authentication, API, or deployed UI is failing or stale.
---

# 診斷部署問題

以同一版本的 Git、image、設定、資料庫、服務與瀏覽器證據找出第一個失敗邊界。診斷預設唯讀，不因排錯直接修改產品程式或正式環境。

## 執行流程

1. 確認目標環境、預期 commit／image、部署方式、服務 URL、發生時間與使用者可觀察症狀。
2. 核對本機 HEAD、遠端 branch／PR、image tag／digest 與實際執行版本，避免用舊 image 判斷新程式。
3. 依序檢查 build → image → container／service → environment／secret alias → datasource → Flyway history → Spring health → API response → Nginx/proxy → browser。
4. 收集每一層第一個相關錯誤、HTTP status、健康狀態與時間；遮蔽密碼、token、JDBC credential、個資及健康資料。
5. 比對 application 設定優先序、port、schema、migration chain、內外部 URL、auth mode 與 frontend build-time variables。
6. 用最小實驗排除假設；將根因與後續連鎖錯誤分開，不以重新啟動暫時成功取代原因說明。
7. 使用者只要求診斷時停止於修復方案；明確要求修復後，依問題類型使用 `fix-full-stack-bug`，再重新驗證完整路徑。

## 必查證據

- Git commit、branch、PR／merge 狀態與乾淨工作區。
- 實際 image 建置時間、tag 或 digest，以及服務目前執行版本。
- 必要環境變數是否存在及來源，但不輸出 secret value。
- Flyway 最新成功版本、失敗 migration、目標 schema 與資料庫連線權限。
- `/actuator/health`、關鍵 API 的 status／統一回應，以及前端 proxy 與 browser console／network。
- Docker、Testcontainers 或外部平台不可用時，將相關驗證列為未驗證而非通過。

## 交付格式

依序列出 `Confirmed` 根因、證據、受影響範圍、最小修復方案、修復後驗證清單與仍為 `Unknown` 的外部條件。

---
name: liberty-debugger
description: Diagnose Open Liberty and WebSphere Liberty startup, deployment, WAR, classloading, feature, JNDI, datasource, Servlet, context-root, TLS, and configuration failures. Use when server logs, deployment status, HTTP behavior, or environment bindings are incorrect.
---

# Liberty 問題診斷

## 工作流程

1. 取得 Liberty、Java 版本、server name、deployment artifact、configuration source 與第一個相關 error。
2. 從 `messages.log`、`console.log`、FFDC 與 server status 找最早失敗及完整 cause chain。
3. 檢查 `server.xml`、include、feature、variable、shared library、classloader order 與 application binding。
4. JNDI／datasource 檢查 declared name、lookup name、driver、credential source、connectivity 與 transaction behavior。
5. WAR／Servlet 檢查 archive structure、context root、servlet mapping、API namespace compatibility 與 dependency packaging。
6. 一次只改一項，確認 server startup 與 application health request。

## 限制

保存診斷證據前不得刪除 cache 或 work area。遮蔽 credential、token、certificate 與個資。分開 application、container、network 與 database 原因。

## 產出

提供最早失敗、因果鏈、設定證據、最小修正、restart scope 與 verification steps。

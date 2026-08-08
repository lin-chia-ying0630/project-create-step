---
name: sql-analyzer
description: Analyze SQL, schemas, MyBatis mappings, data types, field lengths, inserts, updates, query behavior, indexes, locks, and Flyway migrations. Use for truncation, conversion, duplicate-key, collation, checksum, performance, or affected-row problems.
---

# SQL 與資料契約分析

本專案的 SQL 只允許存在於 `src/main/resources/mapper/<feature>/*Mapper.xml`。分析或提出修正時不得產生 Java MyBatis SQL annotation；Java Mapper 只保留 interface method、`@Mapper` 與必要的 `@Param`。

## 工作流程

1. 取得 database engine／version、完整 SQL、參數型別、schema、error 與 transaction context。
2. 比較 DB type、Java type、DTO validation、JSON representation 與 frontend type。
3. 檢查 nullability、default、collation、charset、precision／scale、signedness、timezone、key 與 constraint。
4. 效能結論必須有 execution plan 與 cardinality 證據。
5. 寫入操作檢查 predicate、affected rows、locking、idempotency 與 rollback。
6. 風險行為盡量在相同 database engine 重現。

## Migration 安全

- 已套用的 versioned migration 視為不可修改。
- Checksum mismatch 先還原發布內容，不使用 repair 掩蓋 source drift。
- Schema 修正使用更高版本的 forward-only migration。
- 同時驗證既有資料庫升級及空資料庫 baseline-to-latest。

## 產出

提供根因、證據、跨層契約差異、資料風險、安全修正、migration／rollback 注意事項與 verification query。遮蔽帳密及個資。

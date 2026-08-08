---
name: enforce-code-writing-standards
description: Enforce readable and consistent Java, Spring, MyBatis XML, Vue, TypeScript, SCSS, tests, and technical documentation. Use when creating, editing, reviewing, or refactoring source files, shared components, mapper XML, comments, names, error handling, or project instructions.
---

# 強制程式撰寫標準

每次程式變更都同時改善可讀性、可測試性與一致性，不以編譯成功取代品質檢查。先讀 [references/writing-standards.md](references/writing-standards.md)，再依語言套用適用規則。

## 工作流程

1. 盤點本次變更檔案、既有 formatter、lint、compiler 與測試。
2. 先修正名稱、責任與依賴，再格式化；不得用 formatter 掩蓋錯誤分層。
3. 依「問題、變化點、候選模式、最簡實作」檢視設計模式；沒有第二種行為或明確變化點時不得預建抽象層。
4. Java 使用 constructor injection、`final` dependency、清楚 method 名稱及繁中 Javadoc。
5. MyBatis Java Mapper 只留 contract；SQL 僅放 XML，statement `id` 對應 method。
6. Vue SFC 使用 `<script setup lang="ts"> → <template> → <style>`，元件 PascalCase、props/emits 明確型別。
7. TypeScript 禁止 `any` 與無依據 assertion；API、domain、view state 使用不同型別責任。
8. SCSS 共用 token、mixin 與基礎元件樣式；feature 不重複定義全域 token。
9. 所有裝置共用同一份頁面與功能，支援至少 320px viewport；驗證導覽、表單、表格、分頁及主要操作會依空間重排但不消失。
10. 註解說明原因、契約、副作用與非直覺規則，不逐字翻譯程式。
11. 執行本 skill 檢查、formatter、type-check、test 及 build；只修本次範圍或明確列出既有債務。

## 不可違反

- 不把多個 statement、method 或 template section 壓成一行。
- 不使用 wildcard import、field injection、空 catch、裸 `printStackTrace`、未分類 magic string。
- 不在 Java 寫 SQL，不在 Vue component 直接呼叫 fetch／axios，不複製後端業務規則。
- 不以 `Manager`、`Helper`、`Factory`、`Strategy` 等名稱包裝原本單一且穩定的流程；採用模式必須能指出已存在的變化點與測試邊界。
- 每個 function 有能解釋目的的註解；override、accessor 仍至少由 interface／class 契約涵蓋。
- 新增共用元件前至少確認兩個相同責任的使用者；只相似但契約不同者不合併。
- 不以固定桌面寬度完成頁面；不得讓 body 產生水平捲動，也不得以縮小字體取代正確的響應式重排。

## 驗證

```bash
python3 skills/enforce-code-writing-standards/scripts/check_writing_standards.py .
```

檢查結果分為 ERROR 與 WARNING。ERROR 必須在交付前清除；既有 WARNING 若超出本次範圍，須列出檔案與後續處理方式。

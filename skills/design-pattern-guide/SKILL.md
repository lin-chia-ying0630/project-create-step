---
name: design-pattern-guide
description: Select, apply, and review design patterns for Java, Spring, MyBatis, batch, Vue, and insurance-domain changes without over-engineering. Use when introducing abstractions, splitting complex services, adding underwriting rules, integrating providers, designing state transitions, refactoring conditionals, or reviewing architecture consistency.
---

# 設計模式指南

以實際變化點選擇最小可行模式，避免為名稱、層數或未發生的需求增加抽象。開始前讀取 [references/pattern-catalog.md](references/pattern-catalog.md)。

## 執行流程

1. 描述目前問題、責任邊界、重複或耦合，不先指定模式名稱。
2. 列出已存在或已確認即將出現的第二種行為，以及選擇行為的條件。
3. 確認 Controller、application service、domain、Mapper XML、batch 與 Vue feature 的正確責任層。
4. 從 catalog 選擇一個主要模式；能用簡單 method、enum 或組合解決時不新增模式。
5. 先定義 contract、交易邊界、錯誤碼、輸入輸出與測試，再搬移實作。
6. 以小步驟修改並保持可編譯；不得同時改變業務規則與架構而沒有 characterization test。
7. 執行單元、整合、MyBatis XML、前端與 build 驗證，並回報採用模式帶來的具體效果。

## 保險專案優先模式

- 核保、費率、資格及文件檢核：多個可替換規則使用 Strategy；規則集合使用 Composite 或明確 Pipeline。
- 新契約、收費、照會及承保交易：application service 作 Facade，統一協調 Mapper、交易與領域物件。
- 銀行、身分驗證、通知及舊主機介接：使用 Adapter 隔離外部 DTO、錯誤及協定。
- 自然人／法人、商品或支付方式的物件建立差異：有多個建構分支時使用 Factory Method。
- 保單與案件狀態：固定有限狀態使用 enum 與 transition policy；狀態行為顯著分歧時才使用 State。
- 批次固定骨架與可替換步驟：優先組合式 Pipeline；確有共同不可變骨架時才使用 Template Method。
- PDF、遮蔽、日期等無狀態純轉換：使用專責 utility，不建立假 service 或 singleton state。

## 禁止事項

- 不為單一實作建立空殼 Strategy、Factory、Adapter 或 interface／impl 組合。
- 不以設計模式跨越三層依賴方向，Controller 不直連 Mapper，domain 不依賴 Spring 或外部 DTO。
- 不用 Singleton 保存 request、批次案件、個資或可變業務狀態；Spring bean 預設 singleton 不代表可放共享可變資料。
- 不使用 Service Locator、reflection、class-name switch 或全域 static registry 尋找策略。
- 不把 SQL、交易控制、外部呼叫與規則判斷塞進同一個 Factory 或 Strategy。
- 不把資料庫動態代碼硬編碼為 enum；只有固定封閉集合使用 enum。

## Review 輸出

```markdown
## 設計模式檢視

| 項目 | 結論 |
|---|---|
| 問題與變化點 | ... |
| 現有第二種行為 | ... |
| 建議模式 | ...／維持直接實作 |
| 放置層級 | ... |
| 交易與錯誤邊界 | ... |
| 測試方式 | ... |
| 不採用的替代方案 | ... |
```

未找到真實變化點時，結論必須明確寫「維持直接實作」，而不是產生預留架構。

# 保險需求建模參考框架

本文件只定義使用邊界。採用前必須查看官方最新版本、授權及實際交易夥伴要求。

| 框架 | 適用內容 | 不應用來做什麼 |
|---|---|---|
| ACORD | 保險 capability、business glossary、資料交換、P&C／Life & Annuity 訊息對照。 | 不直接複製受授權內容；不因欄位名稱相似就宣稱相容。 |
| BPMN | 可預先定義的投保、批次、收費、通知與標準作業流程。 | 不勉強描述順序不固定的知識工作或複雜決策表。 |
| CMMN | 核保、理賠、覆核、申訴等由案件資料與人工判斷驅動的工作。 | 不取代固定整合流程與程式交易設計。 |
| DMN | 資格、費率、核保、給付、文件需求及路由 decision table。 | 不放置資料庫寫入、UI 流程或未確認的法規結論。 |
| FIBO | 詞彙、資料字典、概念關係及跨系統語意對照。 | 不直接取代 MySQL 關聯式 schema 或保險公司正式資料字典。 |
| ISO 20022 | 收款、扣款、退款及保險給付相關的金融訊息介接。 | 不作為完整保單、核保或理賠核心模型。 |

## 官方來源

- ACORD Standards and Architecture: https://www.acord.org/standards-architecture
- OMG BPMN: https://www.omg.org/bpmn/
- OMG CMMN: https://www.omg.org/cmmn/
- OMG DMN: https://www.omg.org/dmn/
- ISO 20022 Catalogue: https://www.iso20022.org/catalogue-messages
- EDM Council FIBO: https://spec.edmcouncil.org/fibo/index.html

對每次採用記錄 standard、version、profile／message、來源 URL、license／access、local extension 及 deviation。

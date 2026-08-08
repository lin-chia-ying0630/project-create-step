# 介接標準選擇

## ACORD

- 用於交易夥伴明確採用 ACORD，或需要對照保險資料與訊息概念時。
- 記錄 business transaction、message／schema version、request／response sequence 及 local extension。
- P&C XML 支援即時 request／response；AL3 偏向單向批次，但實際使用受 ACORD 授權條款約束。
- Life & Annuity 資產可能需要 ACORD Standards Online 權限；不得從非正式樣本反推完整標準。

## ISO 20022

- 只在銀行、扣款、收款、退款、給付或支付狀態介接需要時使用。
- 從官方 catalogue 選定 message definition、版本與 community usage guide。
- Official message definition 通常允許多種實作；必須另外記錄交易社群 profile 與限制。

## 內部 canonical model

- 外部標準與內部模型之間設置單一 conversion boundary。
- 保存 source value、canonical value、target value 與 conversion version。
- 不讓 ACORD／ISO 欄位直接滲透所有 Entity、DTO 與 UI；只在 integration adapter 使用。

## 必測

- Min／max length、precision、encoding、多位元中文字、空白 padding 與 sign。
- Unknown／inactive code、缺欄、重複欄、無效日期與跨時區。
- 重複訊息、順序顛倒、partial file、timeout、retry 及版本不相容。
- Header／trailer 筆數、金額、checksum 與資料明細一致性。

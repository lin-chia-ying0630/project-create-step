-- 核保審查不只處理拒保；承保類結果須以 01 有效契約狀態寫入同一份 append-only 決行稽核。
ALTER TABLE new_contract.underwriting_decision_audit
  DROP CHECK chk_underwriting_contract_status;

ALTER TABLE new_contract.underwriting_decision_audit
  ADD CONSTRAINT chk_underwriting_contract_status
  CHECK (contract_status_code IN ('01','13','14','15'));

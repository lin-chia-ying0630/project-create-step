CREATE TABLE new_contract.policy_issuance_reversal_audit (
  reversal_audit_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  underwriting_case_no VARCHAR(32) NOT NULL,
  reason_code VARCHAR(32) NOT NULL,
  reason_description VARCHAR(500) NOT NULL,
  operator_id VARCHAR(100) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  before_content JSON NOT NULL,
  after_content JSON NOT NULL,
  before_content_hash CHAR(64) NOT NULL,
  after_content_hash CHAR(64) NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (reversal_audit_id),
  UNIQUE KEY uk_reversal_request (request_id),
  KEY idx_reversal_policy (policy_no, occurred_at),
  KEY idx_reversal_application (application_no, occurred_at)
);

-- 稽核帳號僅允許 INSERT／SELECT；正式環境須由獨立 migration user 建立禁止 UPDATE／DELETE 的權限或 trigger。

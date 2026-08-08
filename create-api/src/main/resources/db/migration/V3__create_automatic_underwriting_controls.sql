CREATE TABLE new_contract.underwriting_rule_definition (
  rule_code VARCHAR(50) NOT NULL,
  rule_version INT NOT NULL,
  rule_name_zh_tw VARCHAR(200) NOT NULL,
  rule_type VARCHAR(30) NOT NULL,
  severity VARCHAR(10) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  effective_start_date DATE NOT NULL,
  effective_end_date DATE NULL,
  configuration_json JSON NULL,
  source_reference VARCHAR(300) NOT NULL,
  approved_by VARCHAR(100) NULL,
  approved_at TIMESTAMP(6) NULL,
  PRIMARY KEY (rule_code, rule_version),
  CONSTRAINT chk_rule_severity CHECK (severity IN ('ERROR', 'WARN'))
);

CREATE TABLE new_contract.underwriting_validation_result (
  validation_result_id VARCHAR(36) NOT NULL,
  underwriting_case_no VARCHAR(32) NOT NULL,
  application_revision BIGINT NOT NULL,
  rule_code VARCHAR(50) NOT NULL,
  rule_version INT NOT NULL,
  result_status VARCHAR(10) NOT NULL,
  result_message VARCHAR(500) NOT NULL,
  evaluated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (validation_result_id),
  UNIQUE KEY uk_validation_once (underwriting_case_no, application_revision, rule_code, rule_version),
  KEY idx_validation_case (underwriting_case_no, evaluated_at),
  CONSTRAINT chk_validation_result CHECK (result_status IN ('PASS', 'FAIL')),
  CONSTRAINT fk_validation_case FOREIGN KEY (underwriting_case_no)
    REFERENCES new_contract.underwriting_case (underwriting_case_no)
);

CREATE TABLE new_contract.application_compliance_evidence (
  evidence_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  application_revision BIGINT NOT NULL,
  evidence_type VARCHAR(50) NOT NULL,
  evidence_status VARCHAR(20) NOT NULL,
  source_system VARCHAR(50) NOT NULL,
  source_reference VARCHAR(200) NOT NULL,
  checked_by VARCHAR(100) NOT NULL,
  checked_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (evidence_id),
  UNIQUE KEY uk_application_evidence (application_no, application_revision, evidence_type),
  CONSTRAINT chk_evidence_status CHECK (evidence_status IN ('VERIFIED', 'FAILED', 'PENDING')),
  CONSTRAINT fk_evidence_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no)
);

CREATE TABLE new_contract.underwriting_inquiry (
  inquiry_no VARCHAR(32) NOT NULL,
  underwriting_case_no VARCHAR(32) NOT NULL,
  application_revision BIGINT NOT NULL,
  inquiry_status VARCHAR(20) NOT NULL,
  issued_at TIMESTAMP(6) NOT NULL,
  resolved_at TIMESTAMP(6) NULL,
  PRIMARY KEY (inquiry_no),
  UNIQUE KEY uk_open_inquiry_revision (underwriting_case_no, application_revision),
  CONSTRAINT fk_inquiry_case FOREIGN KEY (underwriting_case_no)
    REFERENCES new_contract.underwriting_case (underwriting_case_no)
);

CREATE TABLE new_contract.underwriting_inquiry_item (
  inquiry_item_id VARCHAR(36) NOT NULL,
  inquiry_no VARCHAR(32) NOT NULL,
  rule_code VARCHAR(50) NOT NULL,
  item_message VARCHAR(500) NOT NULL,
  response_text VARCHAR(1000) NULL,
  responded_at TIMESTAMP(6) NULL,
  PRIMARY KEY (inquiry_item_id),
  KEY idx_inquiry_item (inquiry_no),
  CONSTRAINT fk_inquiry_item FOREIGN KEY (inquiry_no)
    REFERENCES new_contract.underwriting_inquiry (inquiry_no)
);

-- 只加入不依賴商品／法規門檻的基本規則；需要正式來源的規則不得預設啟用。
INSERT INTO new_contract.underwriting_rule_definition
  (rule_code, rule_version, rule_name_zh_tw, rule_type, severity, enabled, effective_start_date, source_reference, approved_by, approved_at)
VALUES
  ('BASIC_REQUIRED_FIELDS', 1, '基本必填欄位完整性', 'STRUCTURAL', 'ERROR', TRUE, '2026-01-01', 'PROJECT-CONTRACT', 'system-baseline', CURRENT_TIMESTAMP(6)),
  ('BASIC_AMOUNT_RANGE', 1, '保額與保費基本範圍', 'STRUCTURAL', 'ERROR', TRUE, '2026-01-01', 'PROJECT-CONTRACT', 'system-baseline', CURRENT_TIMESTAMP(6)),
  ('BASIC_DATE_ORDER', 1, '申請日與預定生效日順序', 'STRUCTURAL', 'ERROR', TRUE, '2026-01-01', 'PROJECT-CONTRACT', 'system-baseline', CURRENT_TIMESTAMP(6)),
  ('BASIC_DUPLICATE_CONTROL', 1, '重複要保與保單控制', 'STRUCTURAL', 'ERROR', TRUE, '2026-01-01', 'PROJECT-CONTRACT', 'system-baseline', CURRENT_TIMESTAMP(6)),
  ('TW_NB_DOCUMENT_COMPLETE', 1, '要保文件與簽章完整', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'FL040402;FL006767', NULL, NULL),
  ('TW_NB_CONSENT_VERIFIED', 1, '個資健康資料同意與聲明完成', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'FL006767-11', NULL, NULL),
  ('TW_NB_INSURABLE_INTEREST', 1, '保險利益確認', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'FL040402', NULL, NULL),
  ('TW_NB_SUITABILITY', 1, '需求與適合度評估完成', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'FL006828-7', NULL, NULL),
  ('TW_NB_AML_KYC', 1, 'KYC與洗錢防制檢核完成', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'COMPANY-AML-POLICY-PENDING', NULL, NULL),
  ('TW_NB_NOTIFICATION_QUERY', 1, '保險通報資料查詢完成', 'EVIDENCE', 'ERROR', TRUE, '2026-01-01', 'FL006828-7', NULL, NULL);

-- new_contract 與 main 由環境管理者預先建立並授權；Flyway 不負責建立 schema。
-- 以下是本機最小 main 契約表。接既有 main 前，必須以真實 DDL 完成欄位 mapping；不得將此 DDL 套用至已有正式主檔的環境。
CREATE TABLE IF NOT EXISTS main.policy_contract (
  policy_contract_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  source_application_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  underwriting_case_no VARCHAR(32) NOT NULL,
  product_code VARCHAR(32) NOT NULL,
  product_version VARCHAR(32) NOT NULL,
  policy_status VARCHAR(16) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  sum_assured_amount DECIMAL(18,4) NOT NULL,
  premium_amount DECIMAL(18,4) NOT NULL,
  effective_date DATE NOT NULL,
  contract_date DATE NOT NULL,
  record_version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_contract_id),
  UNIQUE KEY uk_policy_no (policy_no),
  UNIQUE KEY uk_policy_source_application_id (source_application_id),
  UNIQUE KEY uk_policy_application_no (application_no),
  UNIQUE KEY uk_policy_underwriting_case_no (underwriting_case_no)
);

CREATE TABLE IF NOT EXISTS main.policy_party (
  policy_party_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  party_role_code VARCHAR(20) NOT NULL,
  party_seq SMALLINT UNSIGNED NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  relationship_to_insured_code VARCHAR(20) NULL,
  customer_snapshot_reference VARCHAR(100) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_party_id),
  UNIQUE KEY uk_policy_party_role (policy_no, party_role_code, party_seq),
  CONSTRAINT fk_policy_party_contract FOREIGN KEY (policy_no) REFERENCES main.policy_contract (policy_no)
);

CREATE TABLE IF NOT EXISTS main.policy_coverage (
  policy_coverage_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  coverage_item_seq SMALLINT UNSIGNED NOT NULL,
  coverage_item_type VARCHAR(10) NOT NULL,
  product_code VARCHAR(32) NOT NULL,
  product_version VARCHAR(32) NOT NULL,
  insured_customer_id VARCHAR(36) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  sum_assured_amount DECIMAL(18,4) NOT NULL,
  premium_amount DECIMAL(18,4) NOT NULL,
  effective_date DATE NOT NULL,
  end_date DATE NULL,
  coverage_status VARCHAR(16) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_coverage_id),
  UNIQUE KEY uk_policy_coverage_seq (policy_no, coverage_item_seq),
  CONSTRAINT fk_policy_coverage_contract FOREIGN KEY (policy_no) REFERENCES main.policy_contract (policy_no),
  CONSTRAINT chk_policy_coverage_type CHECK (coverage_item_type IN ('BASE', 'RIDER'))
);

CREATE TABLE IF NOT EXISTS main.policy_beneficiary (
  policy_beneficiary_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  beneficiary_type_code VARCHAR(20) NOT NULL,
  beneficiary_seq SMALLINT UNSIGNED NOT NULL,
  beneficiary_customer_id VARCHAR(36) NULL,
  beneficiary_designation_code VARCHAR(32) NULL,
  priority_no SMALLINT UNSIGNED NOT NULL,
  allocation_percentage DECIMAL(7,4) NULL,
  relationship_to_insured_code VARCHAR(20) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_beneficiary_id),
  UNIQUE KEY uk_policy_beneficiary (policy_no, beneficiary_type_code, beneficiary_seq),
  CONSTRAINT fk_policy_beneficiary_contract FOREIGN KEY (policy_no) REFERENCES main.policy_contract (policy_no)
);

CREATE TABLE IF NOT EXISTS main.policy_underwriting_condition (
  policy_underwriting_condition_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  condition_type VARCHAR(20) NOT NULL,
  condition_code VARCHAR(32) NOT NULL,
  extra_premium_rate DECIMAL(9,6) NULL,
  effective_start_date DATE NULL,
  effective_end_date DATE NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_underwriting_condition_id),
  KEY idx_policy_underwriting_condition (policy_no),
  CONSTRAINT fk_policy_condition_contract FOREIGN KEY (policy_no) REFERENCES main.policy_contract (policy_no)
);

CREATE TABLE IF NOT EXISTS main.policy_contract_evidence (
  policy_contract_evidence_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  evidence_type VARCHAR(40) NOT NULL,
  source_table VARCHAR(64) NOT NULL,
  source_record_id VARCHAR(36) NOT NULL,
  source_version VARCHAR(32) NULL,
  evidence_reference VARCHAR(200) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (policy_contract_evidence_id),
  UNIQUE KEY uk_policy_evidence_source (policy_no, source_table, source_record_id),
  CONSTRAINT fk_policy_evidence_contract FOREIGN KEY (policy_no) REFERENCES main.policy_contract (policy_no)
);

CREATE TABLE new_contract.insurance_application (
  application_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  application_revision BIGINT NOT NULL DEFAULT 1,
  application_date DATE NOT NULL,
  received_at TIMESTAMP(6) NULL,
  channel_code VARCHAR(20) NOT NULL,
  branch_code VARCHAR(20) NULL,
  insurance_agent_code VARCHAR(32) NULL,
  product_code VARCHAR(32) NOT NULL,
  product_version VARCHAR(32) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  sum_assured_amount DECIMAL(18,4) NOT NULL,
  premium_amount DECIMAL(18,4) NOT NULL,
  payment_mode_code VARCHAR(16) NOT NULL,
  coverage_term_years SMALLINT UNSIGNED NULL,
  premium_payment_term_years SMALLINT UNSIGNED NULL,
  requested_effective_date DATE NOT NULL,
  application_status VARCHAR(20) NOT NULL,
  validation_result_status VARCHAR(10) NULL,
  validation_completed_at TIMESTAMP(6) NULL,
  validation_batch_id VARCHAR(100) NULL,
  submitted_at TIMESTAMP(6) NULL,
  completed_at TIMESTAMP(6) NULL,
  source_system VARCHAR(50) NOT NULL,
  record_version BIGINT NOT NULL DEFAULT 0,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (application_id),
  UNIQUE KEY uk_application_no (application_no),
  KEY idx_application_status_date (application_status, application_date),
  KEY idx_application_product (product_code, product_version),
  CONSTRAINT chk_application_amounts CHECK (sum_assured_amount > 0 AND premium_amount >= 0),
  CONSTRAINT chk_application_terms CHECK (
    (coverage_term_years IS NULL OR coverage_term_years > 0)
    AND (premium_payment_term_years IS NULL OR premium_payment_term_years > 0)
  ),
  CONSTRAINT chk_application_timestamps CHECK (
    (submitted_at IS NULL OR received_at IS NOT NULL)
    AND (completed_at IS NULL OR submitted_at IS NOT NULL)
  ),
  CONSTRAINT chk_application_validation CHECK (
    validation_result_status IS NULL OR validation_result_status IN ('PASS', 'FAIL')
  )
);

CREATE TABLE new_contract.application_party (
  application_party_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  party_role_code VARCHAR(20) NOT NULL,
  party_seq SMALLINT UNSIGNED NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  relationship_to_insured_code VARCHAR(20) NULL,
  customer_snapshot_reference VARCHAR(100) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (application_party_id),
  UNIQUE KEY uk_application_party_role (application_no, party_role_code, party_seq),
  KEY idx_application_party_customer (customer_id),
  CONSTRAINT fk_application_party_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no)
);

CREATE TABLE new_contract.application_coverage (
  application_coverage_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  coverage_item_seq SMALLINT UNSIGNED NOT NULL,
  coverage_item_type VARCHAR(10) NOT NULL,
  product_code VARCHAR(32) NOT NULL,
  product_version VARCHAR(32) NOT NULL,
  insured_customer_id VARCHAR(36) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  sum_assured_amount DECIMAL(18,4) NOT NULL,
  premium_amount DECIMAL(18,4) NOT NULL,
  coverage_term_years SMALLINT UNSIGNED NULL,
  premium_payment_term_years SMALLINT UNSIGNED NULL,
  requested_effective_date DATE NOT NULL,
  requested_end_date DATE NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (application_coverage_id),
  UNIQUE KEY uk_application_coverage_seq (application_no, coverage_item_seq),
  KEY idx_application_coverage_product (product_code, product_version),
  CONSTRAINT fk_application_coverage_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_coverage_type CHECK (coverage_item_type IN ('BASE', 'RIDER')),
  CONSTRAINT chk_coverage_amounts CHECK (sum_assured_amount > 0 AND premium_amount >= 0),
  CONSTRAINT chk_coverage_dates CHECK (requested_end_date IS NULL OR requested_end_date >= requested_effective_date)
);

CREATE TABLE new_contract.application_beneficiary (
  application_beneficiary_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  beneficiary_type_code VARCHAR(20) NOT NULL,
  beneficiary_seq SMALLINT UNSIGNED NOT NULL,
  beneficiary_customer_id VARCHAR(36) NULL,
  beneficiary_designation_code VARCHAR(32) NULL,
  priority_no SMALLINT UNSIGNED NOT NULL,
  allocation_percentage DECIMAL(7,4) NULL,
  relationship_to_insured_code VARCHAR(20) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (application_beneficiary_id),
  UNIQUE KEY uk_application_beneficiary (application_no, beneficiary_type_code, beneficiary_seq),
  CONSTRAINT fk_application_beneficiary_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_beneficiary_target CHECK (
    (beneficiary_customer_id IS NOT NULL AND beneficiary_designation_code IS NULL)
    OR (beneficiary_customer_id IS NULL AND beneficiary_designation_code IS NOT NULL)
  ),
  CONSTRAINT chk_beneficiary_percentage CHECK (
    allocation_percentage IS NULL OR (allocation_percentage > 0 AND allocation_percentage <= 100)
  )
);

CREATE TABLE new_contract.application_declaration (
  application_declaration_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  declaration_type_code VARCHAR(50) NOT NULL,
  declaration_version VARCHAR(32) NOT NULL,
  confirmed_by_party_role VARCHAR(20) NOT NULL,
  confirmation_method VARCHAR(20) NOT NULL,
  confirmed_at TIMESTAMP(6) NOT NULL,
  evidence_reference VARCHAR(200) NOT NULL,
  PRIMARY KEY (application_declaration_id),
  UNIQUE KEY uk_application_declaration (
    application_no, declaration_type_code, declaration_version, confirmed_by_party_role
  ),
  CONSTRAINT fk_application_declaration_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no)
);

CREATE TABLE new_contract.application_signature (
  application_signature_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  signer_party_role VARCHAR(20) NOT NULL,
  signer_customer_id VARCHAR(36) NOT NULL,
  signature_method VARCHAR(20) NOT NULL,
  signature_evidence_reference VARCHAR(200) NOT NULL,
  signed_at TIMESTAMP(6) NOT NULL,
  verified_at TIMESTAMP(6) NULL,
  verified_by VARCHAR(100) NULL,
  PRIMARY KEY (application_signature_id),
  UNIQUE KEY uk_application_signature (application_no, signer_party_role, signer_customer_id),
  CONSTRAINT fk_application_signature_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_signature_verification CHECK (
    (verified_at IS NULL AND verified_by IS NULL)
    OR (verified_at IS NOT NULL AND verified_by IS NOT NULL)
  )
);

CREATE TABLE new_contract.health_disclosure (
  health_disclosure_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  insured_customer_id VARCHAR(36) NOT NULL,
  question_set_code VARCHAR(32) NOT NULL,
  question_set_version VARCHAR(32) NOT NULL,
  disclosure_question_code VARCHAR(32) NOT NULL,
  answer_code_encrypted VARBINARY(512) NOT NULL,
  supplemental_detail_encrypted MEDIUMBLOB NULL,
  encryption_key_version VARCHAR(32) NOT NULL,
  answered_at TIMESTAMP(6) NOT NULL,
  confirmed_at TIMESTAMP(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (health_disclosure_id),
  UNIQUE KEY uk_health_disclosure_question (
    application_no, insured_customer_id, question_set_code, question_set_version, disclosure_question_code
  ),
  KEY idx_health_disclosure_application (application_no, insured_customer_id),
  CONSTRAINT fk_health_disclosure_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_health_disclosure_confirmation CHECK (confirmed_at IS NULL OR confirmed_at >= answered_at)
);

CREATE TABLE new_contract.underwriting_case (
  underwriting_case_no VARCHAR(32) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  underwriting_status VARCHAR(20) NOT NULL,
  underwriting_decision_code VARCHAR(20) NULL,
  underwriter_id VARCHAR(100) NULL,
  decision_reason_code VARCHAR(32) NULL,
  underwritten_at TIMESTAMP(6) NULL,
  policy_no VARCHAR(32) NULL,
  record_version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (underwriting_case_no),
  UNIQUE KEY uk_underwriting_application (application_no),
  UNIQUE KEY uk_underwriting_policy (policy_no),
  CONSTRAINT fk_underwriting_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no)
);

CREATE TABLE new_contract.underwriting_condition (
  underwriting_condition_id VARCHAR(36) NOT NULL,
  underwriting_case_no VARCHAR(32) NOT NULL,
  condition_type VARCHAR(20) NOT NULL,
  condition_code VARCHAR(32) NOT NULL,
  extra_premium_rate DECIMAL(9,6) NULL,
  effective_start_date DATE NULL,
  effective_end_date DATE NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (underwriting_condition_id),
  KEY idx_condition_case (underwriting_case_no),
  CONSTRAINT fk_condition_case FOREIGN KEY (underwriting_case_no)
    REFERENCES new_contract.underwriting_case (underwriting_case_no),
  CONSTRAINT chk_extra_premium_rate CHECK (extra_premium_rate IS NULL OR extra_premium_rate >= 0)
);

CREATE TABLE new_contract.command_idempotency (
  idempotency_key VARCHAR(100) NOT NULL,
  command_type VARCHAR(50) NOT NULL,
  request_hash CHAR(64) NOT NULL,
  resource_key VARCHAR(64) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (idempotency_key, command_type)
);

CREATE TABLE new_contract.outbox_event (
  event_id VARCHAR(36) NOT NULL,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  payload_json JSON NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  published_at TIMESTAMP(6) NULL,
  retry_count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (event_id),
  KEY idx_outbox_pending (published_at, occurred_at)
);

CREATE TABLE new_contract.business_audit_event (
  audit_event_id VARCHAR(36) NOT NULL,
  operation_type VARCHAR(30) NOT NULL,
  operator_id VARCHAR(100) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NULL,
  underwriting_case_no VARCHAR(32) NULL,
  result_code VARCHAR(20) NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (audit_event_id),
  KEY idx_new_contract_audit_application (application_no, occurred_at),
  KEY idx_new_contract_audit_case (underwriting_case_no, occurred_at)
);

CREATE TABLE new_contract.policy_materialization_map (
  materialization_id VARCHAR(36) NOT NULL,
  policy_no VARCHAR(32) NOT NULL,
  source_table VARCHAR(64) NOT NULL,
  source_record_id VARCHAR(36) NOT NULL,
  target_table VARCHAR(64) NOT NULL,
  target_record_id VARCHAR(36) NOT NULL,
  materialized_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (materialization_id),
  UNIQUE KEY uk_materialization_source (source_table, source_record_id),
  UNIQUE KEY uk_materialization_target (target_table, target_record_id),
  KEY idx_materialization_policy (policy_no)
);

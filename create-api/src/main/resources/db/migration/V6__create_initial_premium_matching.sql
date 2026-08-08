CREATE TABLE new_contract.initial_premium_due (
  premium_due_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  application_revision BIGINT NOT NULL,
  currency_code CHAR(3) NOT NULL,
  calculated_premium_amount DECIMAL(18,4) NOT NULL,
  calculation_rule_version VARCHAR(50) NOT NULL,
  due_status VARCHAR(20) NOT NULL,
  calculated_at TIMESTAMP(6) NOT NULL,
  superseded_at TIMESTAMP(6) NULL,
  PRIMARY KEY (premium_due_id),
  UNIQUE KEY uk_premium_due_revision (application_no, application_revision),
  CONSTRAINT fk_premium_due_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_premium_due_amount CHECK (calculated_premium_amount >= 0),
  CONSTRAINT chk_premium_due_status CHECK (due_status IN ('PENDING', 'MATCHED', 'SUPERSEDED', 'CANCELLED'))
);

CREATE TABLE new_contract.remittance_slip (
  remittance_slip_id VARCHAR(36) NOT NULL,
  remittance_slip_no VARCHAR(50) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  payment_method_code VARCHAR(20) NOT NULL,
  payment_reference VARCHAR(100) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  actual_paid_amount DECIMAL(18,4) NOT NULL,
  remittance_status VARCHAR(20) NOT NULL,
  paid_at TIMESTAMP(6) NOT NULL,
  received_at TIMESTAMP(6) NOT NULL,
  payer_relationship_code VARCHAR(20) NULL,
  entered_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (remittance_slip_id),
  UNIQUE KEY uk_remittance_slip_no (remittance_slip_no),
  UNIQUE KEY uk_payment_reference (payment_method_code, payment_reference),
  KEY idx_remittance_application (application_no, remittance_status),
  CONSTRAINT fk_remittance_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_actual_paid_amount CHECK (actual_paid_amount > 0),
  CONSTRAINT chk_remittance_status CHECK (remittance_status IN ('RECEIVED', 'MATCHED', 'REVERSED', 'REJECTED'))
);

CREATE TABLE new_contract.initial_premium_match (
  premium_match_id VARCHAR(36) NOT NULL,
  premium_due_id VARCHAR(36) NOT NULL,
  remittance_slip_id VARCHAR(36) NOT NULL,
  expected_amount DECIMAL(18,4) NOT NULL,
  actual_amount DECIMAL(18,4) NOT NULL,
  difference_amount DECIMAL(18,4) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  match_status VARCHAR(20) NOT NULL,
  mismatch_reason_code VARCHAR(32) NULL,
  matched_at TIMESTAMP(6) NOT NULL,
  matched_by VARCHAR(100) NOT NULL,
  PRIMARY KEY (premium_match_id),
  UNIQUE KEY uk_match_due (premium_due_id),
  UNIQUE KEY uk_match_remittance (remittance_slip_id),
  CONSTRAINT fk_match_due FOREIGN KEY (premium_due_id)
    REFERENCES new_contract.initial_premium_due (premium_due_id),
  CONSTRAINT fk_match_remittance FOREIGN KEY (remittance_slip_id)
    REFERENCES new_contract.remittance_slip (remittance_slip_id),
  CONSTRAINT chk_match_status CHECK (match_status IN ('MATCHED', 'UNDERPAID', 'OVERPAID', 'CURRENCY_MISMATCH', 'NOT_RECEIVED'))
);

ALTER TABLE new_contract.insurance_application
  ADD COLUMN initial_premium_match_status VARCHAR(20) NULL AFTER validation_batch_id,
  ADD COLUMN initial_premium_matched_at TIMESTAMP(6) NULL AFTER initial_premium_match_status,
  ADD CONSTRAINT chk_application_premium_match_status CHECK (
    initial_premium_match_status IS NULL OR initial_premium_match_status IN
      ('MATCHED', 'UNDERPAID', 'OVERPAID', 'CURRENCY_MISMATCH', 'NOT_RECEIVED')
  );

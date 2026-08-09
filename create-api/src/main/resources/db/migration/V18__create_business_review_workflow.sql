CREATE TABLE new_contract.business_review_case (
  review_id VARCHAR(36) NOT NULL,
  operation_type VARCHAR(40) NOT NULL,
  function_code VARCHAR(50) NOT NULL,
  business_key VARCHAR(200) NOT NULL,
  payload_ciphertext LONGBLOB NOT NULL,
  payload_key_version VARCHAR(32) NOT NULL,
  review_status VARCHAR(20) NOT NULL,
  maker_id VARCHAR(100) NOT NULL,
  submitted_at TIMESTAMP(6) NOT NULL,
  reviewer_id VARCHAR(100) NULL,
  review_comment VARCHAR(500) NULL,
  reviewed_at TIMESTAMP(6) NULL,
  result_content JSON NULL,
  record_version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (review_id),
  KEY idx_business_review_queue (review_status, submitted_at),
  KEY idx_business_review_key (function_code, business_key),
  CONSTRAINT chk_business_review_status CHECK (review_status IN ('PENDING', 'APPROVED', 'REJECTED')),
  CONSTRAINT chk_business_review_decision CHECK (
    (review_status = 'PENDING' AND reviewer_id IS NULL AND reviewed_at IS NULL)
    OR (review_status IN ('APPROVED', 'REJECTED') AND reviewer_id IS NOT NULL AND reviewed_at IS NOT NULL)
  )
);

CREATE TABLE new_contract.business_review_audit_event (
  audit_event_id VARCHAR(36) NOT NULL,
  review_id VARCHAR(36) NOT NULL,
  operation_type VARCHAR(30) NOT NULL,
  operator_id VARCHAR(100) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  result_code VARCHAR(20) NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (audit_event_id),
  KEY idx_review_audit_case (review_id, occurred_at),
  CONSTRAINT fk_review_audit_case FOREIGN KEY (review_id)
    REFERENCES new_contract.business_review_case (review_id)
);

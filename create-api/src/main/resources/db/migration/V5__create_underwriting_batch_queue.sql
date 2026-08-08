CREATE TABLE new_contract.underwriting_batch_request (
  batch_request_id VARCHAR(36) NOT NULL,
  application_no VARCHAR(32) NOT NULL,
  requested_business_date DATE NOT NULL,
  request_status VARCHAR(20) NOT NULL,
  requested_by VARCHAR(100) NOT NULL,
  requested_at TIMESTAMP(6) NOT NULL,
  claimed_by_execution_id VARCHAR(36) NULL,
  completed_at TIMESTAMP(6) NULL,
  result_code VARCHAR(32) NULL,
  PRIMARY KEY (batch_request_id),
  UNIQUE KEY uk_pending_batch_application (application_no, requested_business_date),
  KEY idx_batch_request_pending (request_status, requested_business_date, requested_at),
  CONSTRAINT fk_batch_request_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_batch_request_status CHECK (request_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'INQUIRY', 'FAILED'))
);

CREATE TABLE new_contract.underwriting_batch_execution (
  batch_execution_id VARCHAR(36) NOT NULL,
  business_date DATE NOT NULL,
  trigger_type VARCHAR(20) NOT NULL,
  execution_status VARCHAR(20) NOT NULL,
  started_at TIMESTAMP(6) NOT NULL,
  completed_at TIMESTAMP(6) NULL,
  total_count INT NOT NULL DEFAULT 0,
  approved_count INT NOT NULL DEFAULT 0,
  inquiry_count INT NOT NULL DEFAULT 0,
  failed_count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (batch_execution_id),
  KEY idx_batch_execution_date (business_date, started_at),
  CONSTRAINT chk_batch_trigger CHECK (trigger_type IN ('SCHEDULED', 'MANUAL')),
  CONSTRAINT chk_batch_execution_status CHECK (execution_status IN ('RUNNING', 'COMPLETED', 'COMPLETED_WITH_ERROR', 'FAILED'))
);

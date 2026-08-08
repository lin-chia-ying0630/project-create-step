CREATE TABLE pending_business_lock (
  function_code VARCHAR(50) NOT NULL,
  unique_key VARCHAR(200) NOT NULL,
  case_id VARCHAR(36) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (function_code, unique_key),
  UNIQUE KEY uk_pending_case (case_id)
);

CREATE TABLE business_audit_event (
  audit_event_id VARCHAR(36) NOT NULL,
  module_code VARCHAR(50) NOT NULL,
  operation_type VARCHAR(30) NOT NULL,
  operator_id VARCHAR(100) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  result_code VARCHAR(20) NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (audit_event_id),
  KEY idx_audit_request (request_id),
  KEY idx_audit_occurred (occurred_at)
);

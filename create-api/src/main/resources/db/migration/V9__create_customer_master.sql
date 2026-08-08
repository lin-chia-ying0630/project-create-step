CREATE DATABASE IF NOT EXISTS customer CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE customer.customer_master (
  customer_id VARCHAR(36) NOT NULL,
  customer_type_code VARCHAR(20) NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  gender_code VARCHAR(16) NOT NULL,
  birth_date DATE NOT NULL,
  nationality_code CHAR(2) NOT NULL,
  residency_country_code CHAR(2) NOT NULL,
  record_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  record_version BIGINT NOT NULL DEFAULT 0,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_by VARCHAR(100) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (customer_id),
  KEY idx_customer_name (customer_name),
  CONSTRAINT chk_customer_type CHECK (customer_type_code IN ('PERSON','ORGANIZATION')),
  CONSTRAINT chk_customer_status CHECK (record_status IN ('ACTIVE','INACTIVE'))
);

CREATE TABLE customer.customer_identity_document (
  identity_document_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  identity_type_code VARCHAR(20) NOT NULL,
  identity_no_hash CHAR(64) NOT NULL,
  identity_no_ciphertext VARBINARY(512) NOT NULL,
  identity_no_last4 CHAR(4) NOT NULL,
  issuing_country_code CHAR(2) NOT NULL,
  verification_status VARCHAR(20) NOT NULL,
  verified_at TIMESTAMP(6) NULL,
  is_primary BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (identity_document_id),
  UNIQUE KEY uk_customer_identity (identity_type_code, identity_no_hash),
  KEY idx_identity_customer (customer_id),
  CONSTRAINT fk_identity_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_contact (
  contact_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  contact_type_code VARCHAR(20) NOT NULL,
  contact_value_ciphertext VARBINARY(1024) NOT NULL,
  contact_value_hash CHAR(64) NOT NULL,
  contact_value_masked VARCHAR(120) NOT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT TRUE,
  verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  PRIMARY KEY (contact_id),
  KEY idx_contact_customer (customer_id, contact_type_code),
  CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_address (
  address_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  address_type_code VARCHAR(20) NOT NULL,
  postal_code VARCHAR(10) NOT NULL,
  address_ciphertext VARBINARY(2048) NOT NULL,
  address_masked VARCHAR(200) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  PRIMARY KEY (address_id),
  KEY idx_address_customer (customer_id, address_type_code),
  CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_name_history (
  name_history_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  effective_from DATE NOT NULL,
  effective_to DATE NULL,
  change_reason_code VARCHAR(32) NOT NULL,
  PRIMARY KEY (name_history_id),
  KEY idx_name_history_customer (customer_id, effective_from),
  CONSTRAINT fk_name_history_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_kyc_profile (
  kyc_profile_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  occupation_code VARCHAR(32) NOT NULL,
  source_of_funds_code VARCHAR(32) NOT NULL,
  insurance_purpose_code VARCHAR(32) NOT NULL,
  risk_level_code VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  reviewed_at TIMESTAMP(6) NULL,
  next_review_date DATE NULL,
  PRIMARY KEY (kyc_profile_id),
  UNIQUE KEY uk_kyc_customer (customer_id),
  CONSTRAINT fk_kyc_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_consent (
  consent_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  consent_type_code VARCHAR(32) NOT NULL,
  consent_version VARCHAR(32) NOT NULL,
  consent_status VARCHAR(16) NOT NULL,
  consented_at TIMESTAMP(6) NOT NULL,
  revoked_at TIMESTAMP(6) NULL,
  PRIMARY KEY (consent_id),
  UNIQUE KEY uk_customer_consent (customer_id, consent_type_code, consent_version),
  CONSTRAINT fk_consent_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

CREATE TABLE customer.customer_audit_event (
  audit_event_id VARCHAR(36) NOT NULL,
  customer_id VARCHAR(36) NOT NULL,
  operation_type VARCHAR(30) NOT NULL,
  operator_id VARCHAR(100) NOT NULL,
  request_id VARCHAR(36) NOT NULL,
  changed_fields JSON NOT NULL,
  occurred_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (audit_event_id),
  KEY idx_customer_audit (customer_id, occurred_at)
);

GRANT SELECT, INSERT, UPDATE ON customer.* TO 'insurance'@'%';

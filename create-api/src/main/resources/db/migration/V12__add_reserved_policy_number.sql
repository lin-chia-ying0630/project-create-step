ALTER TABLE new_contract.insurance_application
  ADD COLUMN reserved_policy_no VARCHAR(32) NULL AFTER application_no,
  ADD COLUMN policy_no_reserved_at TIMESTAMP(6) NULL AFTER reserved_policy_no,
  ADD UNIQUE KEY uk_application_reserved_policy_no (reserved_policy_no);

CREATE TABLE new_contract.policy_number_sequence (
  sequence_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (sequence_id)
);

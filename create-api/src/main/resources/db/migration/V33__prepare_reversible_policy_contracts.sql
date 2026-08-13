ALTER TABLE main.policy_contract
  ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '新增人員' AFTER record_version,
  ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '最後修改人員' AFTER created_at,
  ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最後修改時間' AFTER updated_by,
  ADD COLUMN reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為NULL' AFTER updated_at,
  ADD COLUMN reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為NULL' AFTER reviewer_id;

INSERT INTO main.policy_contract
  (policy_contract_id,policy_no,source_application_id,application_no,underwriting_case_no,
   product_code,product_version,policy_status,currency_code,sum_assured_amount,premium_amount,
   effective_date,contract_date,record_version,created_by,updated_by,reviewer_id,reviewed_at)
SELECT UUID(),a.policy_no,a.application_id,a.application_no,u.underwriting_case_no,
       a.product_code,a.product_version,'ACTIVE',a.currency_code,a.sum_assured_amount,a.premium_amount,
       a.requested_effective_date,a.application_date,0,a.created_by,a.updated_by,a.reviewer_id,a.reviewed_at
  FROM new_contract.insurance_application a
  JOIN new_contract.underwriting_case u ON u.application_no=a.application_no
 WHERE u.contract_status_code='01'
   AND a.policy_no IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM main.policy_contract p WHERE p.policy_no=a.policy_no);


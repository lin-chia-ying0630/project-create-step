-- 正式業務表固定保存新增、修改與最後覆核人員／時間；完整歷程仍由 append-only 稽核表保存。
ALTER TABLE new_contract.insurance_application
  ADD COLUMN reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為NULL' AFTER updated_at,
  ADD COLUMN reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為NULL' AFTER reviewer_id;

ALTER TABLE new_contract.underwriting_case
  ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '新增人員' AFTER record_version,
  ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '最後修改人員' AFTER created_at,
  ADD COLUMN reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為NULL' AFTER updated_at,
  ADD COLUMN reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為NULL' AFTER reviewer_id;

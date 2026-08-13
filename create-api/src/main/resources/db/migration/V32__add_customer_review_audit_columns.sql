ALTER TABLE customer.customer_master
  ADD COLUMN reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為NULL' AFTER updated_at,
  ADD COLUMN reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為NULL' AFTER reviewer_id;

ALTER TABLE new_contract.underwriting_inquiry
  ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '新增人員' AFTER resolved_at,
  ADD COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間' AFTER created_by,
  ADD COLUMN updated_by VARCHAR(100) NOT NULL DEFAULT 'migration' COMMENT '最後修改人員' AFTER created_at,
  ADD COLUMN updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最後修改時間' AFTER updated_by,
  ADD COLUMN reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員；尚未覆核為NULL' AFTER updated_at,
  ADD COLUMN reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間；尚未覆核為NULL' AFTER reviewer_id;

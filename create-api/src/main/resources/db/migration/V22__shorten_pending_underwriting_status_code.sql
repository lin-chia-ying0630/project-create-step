-- 將既有長碼 SUBMITTED 轉為業務核准的兩碼 PU（待核保）。
-- 本 migration 只更新目前狀態；append-only 稽核歷史保留事件發生當時的原始內容。
UPDATE new_contract.insurance_application
   SET application_status = 'PU'
 WHERE application_status = 'SUBMITTED';

ALTER TABLE new_contract.insurance_application
    MODIFY COLUMN application_status VARCHAR(20) NOT NULL COMMENT '要保案件狀態代碼；PU 表示待核保';

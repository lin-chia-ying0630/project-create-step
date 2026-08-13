-- 目前 main 分支後端仍使用 V17 的批次欄位與客戶類型契約。
-- 先轉回相容值，避免只恢復資料庫 migration 後造成既有 API 寫入失敗。
ALTER TABLE new_contract.underwriting_batch_request
  DROP CHECK chk_batch_request_process_status,
  CHANGE COLUMN process_status request_status VARCHAR(20) NOT NULL
    COMMENT '排程案件狀態：PENDING、RUNNING、COMPLETED、INQUIRY或FAILED';

UPDATE new_contract.underwriting_batch_request
   SET request_status = CASE request_status
     WHEN 'W' THEN 'PENDING'
     WHEN 'P' THEN 'RUNNING'
     WHEN 'S' THEN 'COMPLETED'
     WHEN 'R' THEN 'FAILED'
     WHEN 'C' THEN 'FAILED'
     ELSE request_status
   END;

ALTER TABLE new_contract.underwriting_batch_request
  ADD CONSTRAINT chk_batch_request_status
    CHECK (request_status IN ('PENDING','RUNNING','COMPLETED','INQUIRY','FAILED'));

ALTER TABLE new_contract.underwriting_batch_execution
  DROP CHECK chk_batch_execution_process_status,
  CHANGE COLUMN process_status execution_status VARCHAR(24) NOT NULL
    COMMENT '批次執行狀態：RUNNING、COMPLETED、COMPLETED_WITH_ERROR或FAILED';

UPDATE new_contract.underwriting_batch_execution
   SET execution_status = CASE execution_status
     WHEN 'W' THEN 'RUNNING'
     WHEN 'P' THEN 'RUNNING'
     WHEN 'S' THEN 'COMPLETED'
     WHEN 'R' THEN 'FAILED'
     WHEN 'C' THEN 'FAILED'
     ELSE execution_status
   END;

ALTER TABLE new_contract.underwriting_batch_execution
  ADD CONSTRAINT chk_batch_execution_status
    CHECK (execution_status IN ('RUNNING','COMPLETED','COMPLETED_WITH_ERROR','FAILED'));

ALTER TABLE customer.customer_master
  DROP CHECK chk_customer_type;

UPDATE customer.customer_master
   SET customer_type_code = CASE customer_type_code
     WHEN '1' THEN 'PERSON'
     WHEN '2' THEN 'ORGANIZATION'
     ELSE customer_type_code
   END;

ALTER TABLE customer.customer_master
  ADD CONSTRAINT chk_customer_type
    CHECK (customer_type_code IN ('PERSON','ORGANIZATION'));

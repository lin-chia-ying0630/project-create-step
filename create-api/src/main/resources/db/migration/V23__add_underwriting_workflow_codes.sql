-- 所有案件與批次進度共用 P/S/R/C/W：P處理或受理、S完成或結案、R照會或退回、C取消、W等待或警示。
INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag, effective_from,
   source_system, source_version)
VALUES
  ('underwriting', '新契約核保', 'process_status_code', '案件處理狀態', 'P', '處理中／受理', 'Processing / Accepted', 1, 'Y', '2026-08-09', '本系統案件狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'process_status_code', '案件處理狀態', 'S', '完成／結案', 'Completed / Closed', 2, 'Y', '2026-08-09', '本系統案件狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'process_status_code', '案件處理狀態', 'R', '照會中／退回補正', 'Referred / Returned', 3, 'Y', '2026-08-09', '本系統案件狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'process_status_code', '案件處理狀態', 'C', '取消／退件不續辦', 'Cancelled', 4, 'Y', '2026-08-09', '本系統案件狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'process_status_code', '案件處理狀態', 'W', '等待／警示', 'Waiting / Warning', 5, 'Y', '2026-08-09', '本系統案件狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_stage_code', '核保階段', 'NP', '核保處理中／受理', 'Underwriting processing', 1, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_stage_code', '核保階段', 'NS', '核保完成／結案', 'Underwriting completed', 2, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_stage_code', '核保階段', 'NR', '核保照會／退回', 'Underwriting referred', 3, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_stage_code', '核保階段', 'NC', '核保取消', 'Underwriting cancelled', 4, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_stage_code', '核保階段', 'NW', '核保等待／警示', 'Underwriting waiting', 5, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'inquiry_stage_code', '照會階段', 'UP', '照會處理中／受理', 'Inquiry processing', 1, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'inquiry_stage_code', '照會階段', 'US', '照會完成／結案', 'Inquiry completed', 2, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'inquiry_stage_code', '照會階段', 'UR', '照會退回補正', 'Inquiry returned', 3, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'inquiry_stage_code', '照會階段', 'UC', '照會取消', 'Inquiry cancelled', 4, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'inquiry_stage_code', '照會階段', 'UW', '等待照會回覆／警示', 'Inquiry waiting', 5, 'Y', '2026-08-09', '本系統核保代碼規範', '2.0'),
  ('underwriting', '新契約核保', 'lia_index_stage_code', '公會索引階段', 'LP', '公會索引處理中／受理', 'LIA index processing', 1, 'Y', '2026-08-09', '保險業通報作業資訊系統查詢', '2.0'),
  ('underwriting', '新契約核保', 'lia_index_stage_code', '公會索引階段', 'LS', '公會索引完成', 'LIA index completed', 2, 'Y', '2026-08-09', '保險業通報作業資訊系統查詢', '2.0'),
  ('underwriting', '新契約核保', 'lia_index_stage_code', '公會索引階段', 'LR', '公會索引退回', 'LIA index returned', 3, 'Y', '2026-08-09', '保險業通報作業資訊系統查詢', '2.0'),
  ('underwriting', '新契約核保', 'lia_index_stage_code', '公會索引階段', 'LC', '公會索引取消', 'LIA index cancelled', 4, 'Y', '2026-08-09', '保險業通報作業資訊系統查詢', '2.0'),
  ('underwriting', '新契約核保', 'lia_index_stage_code', '公會索引階段', 'LW', '等待公會回覆／警示', 'LIA index waiting', 5, 'Y', '2026-08-09', '保險業通報作業資訊系統查詢', '2.0'),
  ('underwriting', '新契約核保', 'batch_stage_code', '批次排程階段', 'BP', '批次處理中／受理', 'Batch processing', 1, 'Y', '2026-08-09', '本系統批次狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'batch_stage_code', '批次排程階段', 'BS', '批次完成', 'Batch completed', 2, 'Y', '2026-08-09', '本系統批次狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'batch_stage_code', '批次排程階段', 'BR', '批次退回處理', 'Batch returned', 3, 'Y', '2026-08-09', '本系統批次狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'batch_stage_code', '批次排程階段', 'BC', '批次取消', 'Batch cancelled', 4, 'Y', '2026-08-09', '本系統批次狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'batch_stage_code', '批次排程階段', 'BW', '等待批次執行／警示', 'Batch waiting', 5, 'Y', '2026-08-09', '本系統批次狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'policy_issuance_stage_code', '保單製發階段', 'PP', '製單處理中／受理', 'Policy issuance processing', 1, 'Y', '2026-08-09', '本系統發單狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'policy_issuance_stage_code', '保單製發階段', 'PS', '保單製發完成', 'Policy issuance completed', 2, 'Y', '2026-08-09', '本系統發單狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'policy_issuance_stage_code', '保單製發階段', 'PR', '製單退回處理', 'Policy issuance returned', 3, 'Y', '2026-08-09', '本系統發單狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'policy_issuance_stage_code', '保單製發階段', 'PC', '製單取消', 'Policy issuance cancelled', 4, 'Y', '2026-08-09', '本系統發單狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'policy_issuance_stage_code', '保單製發階段', 'PW', '待發單／警示', 'Waiting for policy issuance', 5, 'Y', '2026-08-09', '本系統發單狀態規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'SA', '標準承保', 'Standard acceptance', 1, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'RA', '加費承保', 'Rated acceptance', 2, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'EA', '除外承保', 'Exclusion acceptance', 3, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'CA', '條件承保', 'Conditional acceptance', 4, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'PA', '部分承保', 'Partial acceptance', 5, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'PO', '延期承保', 'Postponed', 6, 'Y', '2026-08-09', '本系統核保決定規範', '2.0'),
  ('underwriting', '新契約核保', 'underwriting_decision_code', '核保決定', 'DC', '拒絕承保', 'Declined', 7, 'Y', '2026-08-09', '本系統核保決定規範', '2.0');

-- 已完成前置作業的既有案件轉為 PW，供指定執行日批次選件。
UPDATE new_contract.insurance_application
   SET application_status = CASE
       WHEN application_status IN ('PU', 'SUBMITTED') THEN 'PW'
       WHEN application_status = 'UNDERWRITING' THEN 'NW'
       WHEN application_status = 'INQUIRY' THEN 'UW'
       WHEN application_status IN ('UNDERWRITTEN', 'COMPLETED') THEN 'PS'
       ELSE application_status END
 WHERE application_status IN ('PU', 'SUBMITTED', 'UNDERWRITING', 'INQUIRY', 'UNDERWRITTEN', 'COMPLETED');

UPDATE new_contract.underwriting_case
   SET underwriting_status = CASE
       WHEN underwriting_status IN ('PENDING', 'IN_REVIEW') THEN 'NW'
       WHEN underwriting_status IN ('INQUIRY', 'PENDING_INFO') THEN 'NR'
       WHEN underwriting_status IN ('APPROVED', 'COMPLETED') THEN 'NS'
       ELSE underwriting_status END
 WHERE underwriting_status IN ('PENDING', 'IN_REVIEW', 'INQUIRY', 'PENDING_INFO', 'APPROVED', 'COMPLETED');

UPDATE new_contract.underwriting_inquiry
   SET inquiry_status = CASE WHEN inquiry_status = 'OPEN' THEN 'UW' ELSE 'US' END
 WHERE inquiry_status IN ('OPEN', 'RESOLVED', 'CLOSED');

ALTER TABLE new_contract.underwriting_batch_request DROP CHECK chk_batch_request_status;
UPDATE new_contract.underwriting_batch_request
   SET request_status = CASE request_status WHEN 'PENDING' THEN 'W' WHEN 'RUNNING' THEN 'P'
       WHEN 'COMPLETED' THEN 'S' WHEN 'INQUIRY' THEN 'R' WHEN 'FAILED' THEN 'R' ELSE request_status END;
ALTER TABLE new_contract.underwriting_batch_request
  CHANGE COLUMN request_status process_status CHAR(1) NOT NULL COMMENT '處理狀態：P處理、S完成、R退回、C取消、W等待',
  ADD CONSTRAINT chk_batch_request_process_status CHECK (process_status IN ('P','S','R','C','W'));

ALTER TABLE new_contract.underwriting_batch_execution DROP CHECK chk_batch_execution_status;
UPDATE new_contract.underwriting_batch_execution
   SET execution_status = CASE execution_status WHEN 'RUNNING' THEN 'P' WHEN 'COMPLETED' THEN 'S'
       WHEN 'COMPLETED_WITH_ERROR' THEN 'R' WHEN 'FAILED' THEN 'R' ELSE execution_status END;
ALTER TABLE new_contract.underwriting_batch_execution
  CHANGE COLUMN execution_status process_status CHAR(1) NOT NULL COMMENT '處理狀態：P處理、S完成、R退回、C取消、W等待',
  ADD CONSTRAINT chk_batch_execution_process_status CHECK (process_status IN ('P','S','R','C','W'));

ALTER TABLE new_contract.insurance_application
  MODIFY COLUMN application_status VARCHAR(20) NOT NULL COMMENT '新契約階段兩碼；PW待發單、PS發單完成';
ALTER TABLE new_contract.underwriting_case
  MODIFY COLUMN underwriting_status VARCHAR(20) NOT NULL COMMENT '核保階段兩碼；第二碼P/S/R/C/W';
ALTER TABLE new_contract.underwriting_inquiry
  MODIFY COLUMN inquiry_status VARCHAR(20) NOT NULL COMMENT '照會階段兩碼；第二碼P/S/R/C/W';

-- 保單號碼在案件建立時即固定；欄位名稱不得再表達成預編或保留號碼。
ALTER TABLE new_contract.insurance_application
  CHANGE COLUMN reserved_policy_no policy_no VARCHAR(32) NULL COMMENT '案件建立時固定且不得重新配置的正式保單號碼',
  CHANGE COLUMN policy_no_reserved_at policy_no_assigned_at TIMESTAMP(6) NULL COMMENT '正式保單號碼首次配置時間';

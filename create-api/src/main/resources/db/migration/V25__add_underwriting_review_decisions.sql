-- 核保審查結果依核准契約固定映射：拒保 NS/13、延期 DS/14、取消 CS/15。
ALTER TABLE new_contract.underwriting_case
  ADD COLUMN contract_status_code VARCHAR(2) NULL COMMENT '契約狀態：13拒保、14延期、15取消' AFTER underwriting_decision_code;

CREATE TABLE new_contract.underwriting_decision_audit (
  audit_id VARCHAR(36) NOT NULL COMMENT '核保決行稽核識別碼',
  underwriting_case_no VARCHAR(32) NOT NULL COMMENT '核保案件號碼',
  application_no VARCHAR(32) NOT NULL COMMENT '要保書號碼',
  decision_code VARCHAR(2) NOT NULL COMMENT '核保結果代碼',
  stage_code VARCHAR(2) NOT NULL COMMENT '核保階段碼',
  contract_status_code VARCHAR(2) NOT NULL COMMENT '契約狀態代碼',
  reason_code VARCHAR(32) NOT NULL COMMENT '決行原因代碼',
  reason_description VARCHAR(500) NOT NULL COMMENT '決行原因說明，不得記錄健康告知原文',
  operator_id VARCHAR(100) NOT NULL COMMENT '覆核決行人員識別碼',
  occurred_at TIMESTAMP(6) NOT NULL COMMENT '決行時間',
  PRIMARY KEY (audit_id),
  KEY idx_underwriting_decision_audit_case (underwriting_case_no, occurred_at),
  CONSTRAINT fk_underwriting_decision_audit_case FOREIGN KEY (underwriting_case_no)
    REFERENCES new_contract.underwriting_case (underwriting_case_no),
  CONSTRAINT chk_underwriting_contract_status CHECK (contract_status_code IN ('13','14','15'))
) COMMENT='核保審查結果異動稽核檔';

-- V23 的 NS 曾代表核保完成；在 NS 改作拒保完成前，先把既有承保完成資料無損轉為 AS。
UPDATE new_contract.underwriting_case SET underwriting_status='AS' WHERE underwriting_status='NS';
UPDATE new_contract.insurance_application SET application_status='AS' WHERE application_status='NS';
UPDATE main.policy_contract SET policy_status='01' WHERE policy_status='ACTIVE';

UPDATE new_contract.code_definition
   SET code_description_zh_tw='拒保完成',code_description_en='Declined completed',source_version='3.0'
 WHERE code_group='underwriting' AND code_field='underwriting_stage_code' AND code_value='NS';

INSERT INTO new_contract.code_definition
  (code_group,code_group_description_zh_tw,code_field,code_field_description_zh_tw,
   code_value,code_description_zh_tw,code_description_en,display_order,active_flag,effective_from,
   source_system,source_version)
VALUES
	('underwriting','新契約核保','contract_status_code','契約狀態','01','有效','Active',1,'Y','2026-08-09','本系統契約狀態規範','1.0'),
	('underwriting','新契約核保','underwriting_stage_code','核保階段','AS','承保完成','Accepted completed',2,'Y','2026-08-09','本系統核保審查規範','3.0'),
  ('underwriting','新契約核保','underwriting_stage_code','核保階段','DS','延期完成','Postponed completed',6,'Y','2026-08-09','本系統核保審查規範','3.0'),
  ('underwriting','新契約核保','underwriting_stage_code','核保階段','CS','取消完成','Cancelled completed',7,'Y','2026-08-09','本系統核保審查規範','3.0'),
  ('underwriting','新契約核保','underwriting_decision_code','核保決定','CN','取消申請','Cancelled',8,'Y','2026-08-09','本系統核保審查規範','3.0'),
  ('underwriting','新契約核保','contract_status_code','契約狀態','13','拒保','Declined',2,'Y','2026-08-09','本系統契約狀態規範','1.0'),
  ('underwriting','新契約核保','contract_status_code','契約狀態','14','延期','Postponed',3,'Y','2026-08-09','本系統契約狀態規範','1.0'),
  ('underwriting','新契約核保','contract_status_code','契約狀態','15','取消','Cancelled',4,'Y','2026-08-09','本系統契約狀態規範','1.0'),
  ('policy_service','保全／契約變更','contract_status_code','契約狀態','26','猶豫期變更','Free-look period change',1,'Y','2026-08-09','本系統保全狀態規範','1.0');

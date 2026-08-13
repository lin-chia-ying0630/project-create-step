-- NS 是新契約受理檔與核保案件的「照會結束／待核保審查」階段；拒保完成另以 RS 表示。
UPDATE new_contract.code_definition
   SET code_description_zh_tw='照會結束／待核保審查',
       code_description_en='Inquiry completed / awaiting underwriting review',
       source_version='4.0'
 WHERE code_group='underwriting' AND code_field='underwriting_stage_code' AND code_value='NS';

INSERT INTO new_contract.code_definition
  (code_group,code_group_description_zh_tw,code_field,code_field_description_zh_tw,
   code_value,code_description_zh_tw,code_description_en,display_order,active_flag,effective_from,
   source_system,source_version)
VALUES
  ('underwriting','新契約核保','underwriting_stage_code','核保階段','RS','拒保完成',
   'Declined completed',8,'Y','2026-08-09','本系統核保審查規範','4.0')
ON DUPLICATE KEY UPDATE code_description_zh_tw=VALUES(code_description_zh_tw),
  code_description_en=VALUES(code_description_en),source_version=VALUES(source_version);

-- 已決定拒保的現行案件轉為 RS，避免與待審查 NS 混用；歷史稽核保留當時快照不回寫。
UPDATE new_contract.underwriting_case
   SET underwriting_status='RS'
 WHERE underwriting_status='NS' AND underwriting_decision_code='DC';

UPDATE new_contract.insurance_application a
JOIN new_contract.underwriting_case u ON u.application_no=a.application_no
   SET a.application_status='RS'
 WHERE u.underwriting_status='RS' AND u.underwriting_decision_code='DC';

-- 本機虛構照會案件模擬照會已結束，供待核保審查清單端到端驗收。
UPDATE new_contract.underwriting_inquiry
   SET inquiry_status='US'
 WHERE underwriting_case_no='DEMO-UW-INQ-001';
UPDATE new_contract.underwriting_case
   SET underwriting_status='NS'
 WHERE underwriting_case_no='DEMO-UW-INQ-001';
UPDATE new_contract.insurance_application
   SET application_status='NS'
 WHERE application_no='DEMO-NC-INQ-001';

-- 將跨受理、核保、照會與發單流程的階段統一到同一欄位名稱及完整英文名稱。
INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag, effective_from,
   source_system, source_version)
VALUES
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'AP', '要保受理',
   'Application Accepted', 1, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'PW', '待發單／等待',
   'Waiting for Policy Issuance', 2, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'NP', '核保處理中／受理',
   'Underwriting Processing', 3, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'NW', '核保等待／警示',
   'Underwriting Waiting', 4, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'NR', '核保照會／退回',
   'Underwriting Referred', 5, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'UW', '等待照會回覆',
   'Waiting for Inquiry Response', 6, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'US', '照會完成',
   'Inquiry Completed', 7, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'NS', '照會結束／待核保審查',
   'Pending Underwriting Review', 8, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'AS', '承保完成／結案',
   'Underwriting Accepted', 9, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'RS', '拒保完成',
   'Underwriting Declined', 10, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'DS', '延期完成',
   'Underwriting Postponed', 11, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'CS', '取消完成',
   'Underwriting Cancelled', 12, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0'),
  ('new-contract', '新契約', 'new_contract_stage_code', '新契約階段碼', 'PS', '保單製發完成',
   'Policy Issued', 13, 'Y', '2026-08-15', '本系統新契約階段規範', '1.0')
ON DUPLICATE KEY UPDATE
  code_description_zh_tw=VALUES(code_description_zh_tw),
  code_description_en=VALUES(code_description_en),
  display_order=VALUES(display_order),
  active_flag=VALUES(active_flag),
  source_system=VALUES(source_system),
  source_version=VALUES(source_version);

ALTER TABLE new_contract.insurance_application
  MODIFY COLUMN application_status VARCHAR(20) NOT NULL
  COMMENT '新契約階段碼；對應 code_definition.new_contract_stage_code';

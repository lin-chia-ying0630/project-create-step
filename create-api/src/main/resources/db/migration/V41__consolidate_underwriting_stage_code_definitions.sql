-- 將核保、照會、公會索引與保單製發階段收斂為同一個新契約階段代碼欄位。
-- 實際業務表仍保留各自的狀態欄位，以維持流程責任及既有 API 相容性；本 migration
-- 只統一供查詢與畫面選擇使用的 code_definition metadata。
INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag, effective_from,
   classification_code, classification_description_zh_tw, breakdown_code,
   breakdown_description_zh_tw, nature_of_work_zh_tw, source_system, source_version)
SELECT 'new-contract',
       '新契約',
       'new_contract_stage_code',
       '新契約階段碼',
       code_value,
       code_description_zh_tw,
       code_description_en,
       CASE code_field
         WHEN 'underwriting_stage_code' THEN 100
         WHEN 'inquiry_stage_code' THEN 200
         WHEN 'lia_index_stage_code' THEN 300
         WHEN 'policy_issuance_stage_code' THEN 400
       END + display_order,
       'Y',
       effective_from,
       classification_code,
       classification_description_zh_tw,
       breakdown_code,
       breakdown_description_zh_tw,
       nature_of_work_zh_tw,
       source_system,
       '2.1'
  FROM new_contract.code_definition
 WHERE code_group = 'underwriting'
   AND active_flag = 'Y'
   AND effective_from <= '2026-08-15'
   AND (effective_to IS NULL OR effective_to >= '2026-08-15')
   AND code_field IN (
     'underwriting_stage_code',
     'inquiry_stage_code',
     'lia_index_stage_code',
     'policy_issuance_stage_code'
   )
ON DUPLICATE KEY UPDATE
  active_flag = 'Y',
  effective_to = NULL;

UPDATE new_contract.code_definition
   SET active_flag = 'N',
       effective_to = '2026-08-15',
       source_version = '2.1'
 WHERE code_group = 'underwriting'
   AND code_field IN (
     'underwriting_stage_code',
     'inquiry_stage_code',
     'lia_index_stage_code',
     'policy_issuance_stage_code'
   );

-- 批次只領取新契約階段 PW，並將案件更新為 PS 或 PR；案件進度仍以
-- new_contract_stage_code 為準，不另維護 BP／BS／BR／BC／BW 第二套階段代碼。
UPDATE new_contract.code_definition
   SET active_flag = 'N',
       effective_to = '2026-08-15',
       source_version = '2.1'
 WHERE code_group = 'underwriting'
   AND code_field = 'batch_stage_code';

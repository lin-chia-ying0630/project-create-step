-- 26「十天猶豫期變更」仍屬新契約期間，不歸入保全／契約變更代碼群組。
UPDATE new_contract.code_definition
   SET code_group='underwriting',
       code_group_description_zh_tw='新契約核保',
       display_order=5,
       source_system='本系統新契約狀態規範',
       source_version='1.1'
 WHERE code_group='policy_service'
   AND code_field='contract_status_code'
   AND code_value='26';

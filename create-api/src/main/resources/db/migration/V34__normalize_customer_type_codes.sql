-- 客戶類型主檔只保存短代碼；英文識別與繁體中文名稱由代碼定義集中管理。
INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag,
   effective_from, source_system, source_version)
VALUES
  ('customer-master', '客戶主檔', 'customer_type_code', '客戶類型',
   '1', '自然人', 'PERSON', 1, 'Y', '2026-08-09', '新契約客戶作業', '1.0'),
  ('customer-master', '客戶主檔', 'customer_type_code', '客戶類型',
   '2', '公司', 'ORGANIZATION', 2, 'Y', '2026-08-09', '新契約客戶作業', '1.0');

ALTER TABLE customer.customer_master DROP CHECK chk_customer_type;

UPDATE customer.customer_master
   SET customer_type_code = CASE customer_type_code
     WHEN 'PERSON' THEN '1'
     WHEN 'ORGANIZATION' THEN '2'
     ELSE customer_type_code
   END;

ALTER TABLE customer.customer_master
  ADD CONSTRAINT chk_customer_type CHECK (customer_type_code IN ('1', '2'));

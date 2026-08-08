CREATE TABLE new_contract.code_definition (
  code_group VARCHAR(64) NOT NULL,
  code_field VARCHAR(64) NOT NULL,
  code_value VARCHAR(64) NOT NULL,
  code_description_zh_tw VARCHAR(255) NOT NULL,
  code_description_en VARCHAR(255),
  display_order INT NOT NULL DEFAULT 0,
  active_flag CHAR(1) NOT NULL DEFAULT 'Y',
  effective_from DATE NOT NULL DEFAULT (CURRENT_DATE),
  effective_to DATE,
  PRIMARY KEY (code_group, code_field, code_value),
  CONSTRAINT ck_new_contract_code_active CHECK (active_flag IN ('Y', 'N'))
);

INSERT INTO new_contract.code_definition
  (code_group, code_field, code_value, code_description_zh_tw, code_description_en, display_order, active_flag)
VALUES
  ('customer-kyc', 'occupation_code', 'OFFICE_WORKER', '內勤人員', 'Office worker', 10, 'Y'),
  ('customer-kyc', 'occupation_code', 'PROFESSIONAL', '專業人員', 'Professional', 20, 'Y'),
  ('customer-kyc', 'occupation_code', 'SELF_EMPLOYED', '自營業者', 'Self-employed', 30, 'Y'),
  ('customer-kyc', 'occupation_code', 'STUDENT', '學生', 'Student', 40, 'Y'),
  ('customer-kyc', 'occupation_code', 'HOMEMAKER', '家管', 'Homemaker', 50, 'Y'),
  ('customer-kyc', 'occupation_code', 'RETIRED', '退休人員', 'Retired', 60, 'Y'),
  ('customer-kyc', 'occupation_code', 'BUSINESS_ENTITY', '公司／行號', 'Business entity', 70, 'Y'),
  ('customer-kyc', 'source_of_funds_code', 'SALARY', '薪資所得', 'Salary income', 10, 'Y'),
  ('customer-kyc', 'source_of_funds_code', 'SAVINGS', '儲蓄', 'Savings', 20, 'Y'),
  ('customer-kyc', 'source_of_funds_code', 'INVESTMENT', '投資所得', 'Investment income', 30, 'Y'),
  ('customer-kyc', 'source_of_funds_code', 'BUSINESS_INCOME', '營業收入', 'Business income', 40, 'Y'),
  ('customer-kyc', 'source_of_funds_code', 'OTHER', '其他', 'Other', 90, 'Y'),
  ('customer-kyc', 'insurance_purpose_code', 'PROTECTION', '保障需求', 'Protection', 10, 'Y'),
  ('customer-kyc', 'insurance_purpose_code', 'RETIREMENT', '退休規劃', 'Retirement planning', 20, 'Y'),
  ('customer-kyc', 'insurance_purpose_code', 'EDUCATION', '教育準備', 'Education planning', 30, 'Y'),
  ('customer-kyc', 'insurance_purpose_code', 'ESTATE_PLANNING', '資產傳承', 'Estate planning', 40, 'Y'),
  ('customer-kyc', 'insurance_purpose_code', 'OTHER', '其他', 'Other', 90, 'Y');

-- 商品維護與新契約登打驗證補強：所有欄位保留繁中 COMMENT 供資料字典使用。
ALTER TABLE new_contract.insurance_product_definition
  ADD COLUMN product_risk_level_code CHAR(2) NULL COMMENT '投資型商品風險等級代碼；傳統型商品為NULL' AFTER currency_code,
  ADD COLUMN minimum_coverage_term_years INT NULL COMMENT '最低保險期間年數' AFTER maximum_sum_assured,
  ADD COLUMN maximum_coverage_term_years INT NULL COMMENT '最高保險期間年數' AFTER minimum_coverage_term_years,
  ADD COLUMN minimum_payment_term_years INT NULL COMMENT '最低繳費期間年數' AFTER maximum_coverage_term_years,
  ADD COLUMN maximum_payment_term_years INT NULL COMMENT '最高繳費期間年數' AFTER minimum_payment_term_years,
  ADD CONSTRAINT chk_product_definition_coverage_term CHECK (minimum_coverage_term_years IS NULL OR maximum_coverage_term_years IS NULL OR maximum_coverage_term_years >= minimum_coverage_term_years),
  ADD CONSTRAINT chk_product_definition_payment_term CHECK (minimum_payment_term_years IS NULL OR maximum_payment_term_years IS NULL OR maximum_payment_term_years >= minimum_payment_term_years);

CREATE TABLE new_contract.insurance_product_payment_mode (
  product_code VARCHAR(32) NOT NULL COMMENT '商品代碼', product_version VARCHAR(32) NOT NULL COMMENT '商品版本',
  payment_mode_code VARCHAR(20) NOT NULL COMMENT '可使用繳別代碼',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員', created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '最後修改人員', updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最後修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員', reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間',
  PRIMARY KEY (product_code, product_version, payment_mode_code),
  CONSTRAINT fk_product_payment_mode_product FOREIGN KEY (product_code, product_version) REFERENCES new_contract.insurance_product_definition(product_code, product_version)
) COMMENT='保險商品可使用繳別明細檔';

CREATE TABLE new_contract.insurance_product_rider_rule (
  base_product_code VARCHAR(32) NOT NULL COMMENT '主約商品代碼', base_product_version VARCHAR(32) NOT NULL COMMENT '主約商品版本',
  rider_product_code VARCHAR(32) NOT NULL COMMENT '可搭配附約商品代碼', rider_product_version VARCHAR(32) NOT NULL COMMENT '可搭配附約商品版本',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員', created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '建立時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '最後修改人員', updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '最後修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '最後覆核人員', reviewed_at TIMESTAMP(6) NULL COMMENT '最後覆核時間',
  PRIMARY KEY (base_product_code, base_product_version, rider_product_code, rider_product_version),
  CONSTRAINT fk_product_rider_rule_base FOREIGN KEY (base_product_code, base_product_version) REFERENCES new_contract.insurance_product_definition(product_code, product_version),
  CONSTRAINT fk_product_rider_rule_rider FOREIGN KEY (rider_product_code, rider_product_version) REFERENCES new_contract.insurance_product_definition(product_code, product_version)
) COMMENT='主約與附約商品搭配規則檔';

ALTER TABLE new_contract.application_attachment
  ADD COLUMN file_size_bytes BIGINT NULL COMMENT '附件檔案大小位元組' AFTER file_hash,
  ADD CONSTRAINT chk_application_attachment_size CHECK (file_size_bytes IS NULL OR (file_size_bytes > 0 AND file_size_bytes <= 10485760));

UPDATE new_contract.insurance_product_definition SET product_risk_level_code=CASE WHEN product_type_code='I' THEN 'R3' ELSE NULL END,
 minimum_coverage_term_years=1, maximum_coverage_term_years=99, minimum_payment_term_years=1, maximum_payment_term_years=30;

INSERT INTO new_contract.insurance_product_payment_mode (product_code,product_version,payment_mode_code,created_by,updated_by,reviewer_id,reviewed_at)
SELECT product_code,product_version,mode_code,'migration','migration','migration',CURRENT_TIMESTAMP(6) FROM new_contract.insurance_product_definition
CROSS JOIN (SELECT 'MONTHLY' mode_code UNION ALL SELECT 'QUARTERLY' UNION ALL SELECT 'SEMI_ANNUAL' UNION ALL SELECT 'ANNUAL') modes;

INSERT INTO new_contract.insurance_product_rider_rule (base_product_code,base_product_version,rider_product_code,rider_product_version,created_by,updated_by,reviewer_id,reviewed_at)
SELECT base.product_code,base.product_version,rider.product_code,rider.product_version,'migration','migration','migration',CURRENT_TIMESTAMP(6)
FROM new_contract.insurance_product_definition base JOIN new_contract.insurance_product_definition rider ON rider.coverage_item_type='RIDER' WHERE base.coverage_item_type='BASE';

INSERT INTO new_contract.code_definition
 (code_group,code_group_description_zh_tw,code_field,code_field_description_zh_tw,
  code_value,code_description_zh_tw,code_description_en,display_order,active_flag,
  effective_from,source_system,source_version) VALUES
 ('new-contract','新契約作業','customer_risk_level_code','客戶投資風險等級','R1','保守型','CONSERVATIVE',1,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','customer_risk_level_code','客戶投資風險等級','R2','安穩型','CAUTIOUS',2,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','customer_risk_level_code','客戶投資風險等級','R3','穩健型','BALANCED',3,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','customer_risk_level_code','客戶投資風險等級','R4','成長型','GROWTH',4,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','customer_risk_level_code','客戶投資風險等級','R5','積極型','AGGRESSIVE',5,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','product_risk_level_code','商品投資風險等級','R1','低風險','LOW',1,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','product_risk_level_code','商品投資風險等級','R2','中低風險','MEDIUM_LOW',2,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','product_risk_level_code','商品投資風險等級','R3','中度風險','MEDIUM',3,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','product_risk_level_code','商品投資風險等級','R4','中高風險','MEDIUM_HIGH',4,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','product_risk_level_code','商品投資風險等級','R5','高風險','HIGH',5,'Y','2026-08-09','新契約作業','1.0');

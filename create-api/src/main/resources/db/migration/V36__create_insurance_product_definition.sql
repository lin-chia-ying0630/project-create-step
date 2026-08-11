-- 保險商品定義檔：作為保單登打判斷傳統型壽險或投資型保險的唯一來源。
CREATE TABLE new_contract.insurance_product_definition (
  product_code VARCHAR(32) NOT NULL COMMENT '商品代碼',
  product_version VARCHAR(32) NOT NULL COMMENT '商品版本',
  product_name_zh_tw VARCHAR(200) NOT NULL COMMENT '商品繁體中文名稱',
  product_name_en VARCHAR(200) NULL COMMENT '商品英文名稱',
  product_type_code CHAR(1) NOT NULL COMMENT '商品類型：L傳統型壽險、I投資型保險',
  coverage_item_type VARCHAR(10) NOT NULL COMMENT '保障項目適用類型：BASE主約、RIDER附約',
  currency_code CHAR(3) NOT NULL COMMENT '商品幣別',
  minimum_entry_age INT NULL COMMENT '最低投保年齡',
  maximum_entry_age INT NULL COMMENT '最高投保年齡',
  minimum_sum_assured DECIMAL(18,4) NULL COMMENT '最低保險金額',
  maximum_sum_assured DECIMAL(18,4) NULL COMMENT '最高保險金額',
  minimum_premium DECIMAL(18,4) NULL COMMENT '最低保險費',
  effective_from DATE NOT NULL COMMENT '商品生效日',
  effective_to DATE NULL COMMENT '商品停售日',
  product_status CHAR(1) NOT NULL COMMENT '商品狀態：P受理中、S完成上架、C取消、W等待',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '新增時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '修改人員',
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '覆核人員',
  reviewed_at TIMESTAMP(6) NULL COMMENT '覆核時間',
  PRIMARY KEY (product_code, product_version),
  KEY idx_product_definition_active (product_status, effective_from, effective_to),
  CONSTRAINT chk_product_definition_type CHECK (product_type_code IN ('L','I')),
  CONSTRAINT chk_product_definition_coverage CHECK (coverage_item_type IN ('BASE','RIDER')),
  CONSTRAINT chk_product_definition_status CHECK (product_status IN ('P','S','C','W')),
  CONSTRAINT chk_product_definition_age CHECK (
    minimum_entry_age IS NULL OR maximum_entry_age IS NULL OR maximum_entry_age >= minimum_entry_age
  ),
  CONSTRAINT chk_product_definition_amount CHECK (
    minimum_sum_assured IS NULL OR maximum_sum_assured IS NULL OR maximum_sum_assured >= minimum_sum_assured
  ),
  CONSTRAINT chk_product_definition_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
) COMMENT='保險商品定義檔';

INSERT INTO new_contract.insurance_product_definition
  (product_code, product_version, product_name_zh_tw, product_name_en, product_type_code,
   coverage_item_type, currency_code, minimum_entry_age, maximum_entry_age, minimum_sum_assured,
   maximum_sum_assured, minimum_premium, effective_from, effective_to, product_status,
   created_by, updated_by, reviewer_id, reviewed_at)
VALUES
  ('LIFE-DEMO','1.0','安心終身壽險（示範）','DEMO WHOLE LIFE','L','BASE','TWD',0,70,100000,10000000,1000,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('LIFE-SAVING','1.0','穩健儲蓄壽險（示範）','DEMO SAVINGS LIFE','L','BASE','TWD',0,65,100000,20000000,5000,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('LIFE-TERM','1.0','定期壽險（示範）','DEMO TERM LIFE','L','BASE','TWD',18,70,500000,30000000,1000,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('LIFE-ENTRY','1.0','入門型壽險（示範）','DEMO ENTRY LIFE','L','BASE','TWD',0,75,100000,5000000,500,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('INV-LINK-DEMO','1.0','變額壽險（投資型示範）','DEMO VARIABLE LIFE','I','BASE','TWD',18,65,100000,20000000,10000,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('RIDER-ACC-DEMO','1.0','傷害附約（示範）','DEMO ACCIDENT RIDER','L','RIDER','TWD',0,70,100000,5000000,100,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6)),
  ('RIDER-MED-DEMO','1.0','醫療附約（示範）','DEMO MEDICAL RIDER','L','RIDER','TWD',0,65,10000,2000000,100,'2026-01-01',NULL,'S','migration','migration','migration',CURRENT_TIMESTAMP(6));

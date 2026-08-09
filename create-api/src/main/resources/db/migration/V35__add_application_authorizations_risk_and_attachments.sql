-- 要保書付款授權：只保存支付 Token 與遮罩號碼，不保存完整銀行帳號、卡號或安全碼。
CREATE TABLE new_contract.initial_premium_authorization (
  authorization_id VARCHAR(36) NOT NULL COMMENT '首期保費授權識別碼',
  application_no VARCHAR(32) NOT NULL COMMENT '要保書號碼',
  authorization_type_code CHAR(1) NOT NULL COMMENT '授權方式：B銀行帳戶、C信用卡',
  payer_role_code VARCHAR(20) NOT NULL COMMENT '繳款人角色代碼',
  payer_customer_id VARCHAR(36) NOT NULL COMMENT '繳款人客戶識別碼',
  payer_relationship_code VARCHAR(20) NOT NULL COMMENT '繳款人與要保人關係代碼',
  payer_name VARCHAR(100) NOT NULL COMMENT '授權人姓名',
  institution_code VARCHAR(3) NULL COMMENT '金融機構代碼',
  branch_code VARCHAR(4) NULL COMMENT '分行代碼',
  payment_token VARCHAR(100) NOT NULL COMMENT '付款工具不可逆代碼',
  masked_number VARCHAR(32) NOT NULL COMMENT '銀行帳號或卡號遮罩值',
  expiry_month CHAR(2) NULL COMMENT '信用卡有效月份',
  expiry_year CHAR(4) NULL COMMENT '信用卡有效年份',
  authorization_date DATE NOT NULL COMMENT '授權日期',
  authorization_version VARCHAR(20) NOT NULL COMMENT '授權書版本',
  validation_status CHAR(1) NOT NULL COMMENT '驗證狀態：S格式完成、W等待外部授權、N失敗',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '新增時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '修改人員',
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '覆核人員',
  reviewed_at TIMESTAMP(6) NULL COMMENT '覆核時間',
  PRIMARY KEY (authorization_id),
  UNIQUE KEY uk_initial_premium_authorization_application (application_no),
  CONSTRAINT fk_initial_premium_authorization_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_initial_premium_authorization_type CHECK (authorization_type_code IN ('B','C')),
  CONSTRAINT chk_initial_premium_authorization_status CHECK (validation_status IN ('S','W','N'))
) COMMENT='首期保費付款授權資料';

-- 共同行銷同意可拒絕且不得影響要保案件受理；資料範圍與接收公司保存當時版本快照。
CREATE TABLE new_contract.cross_selling_consent (
  consent_id VARCHAR(36) NOT NULL COMMENT '共同行銷同意識別碼',
  application_no VARCHAR(32) NOT NULL COMMENT '要保書號碼',
  agreed_flag CHAR(1) NOT NULL COMMENT '是否同意共同行銷',
  consent_version VARCHAR(20) NOT NULL COMMENT '同意書版本',
  recipient_companies VARCHAR(500) NULL COMMENT '接收資料之子公司清單',
  data_scope_codes VARCHAR(200) NULL COMMENT '同意交互運用的資料範圍代碼',
  stop_method_acknowledged CHAR(1) NOT NULL COMMENT '是否已告知停止交互運用方式',
  confirmed_at TIMESTAMP(6) NOT NULL COMMENT '確認時間',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '新增時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '修改人員',
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '覆核人員',
  reviewed_at TIMESTAMP(6) NULL COMMENT '覆核時間',
  PRIMARY KEY (consent_id),
  UNIQUE KEY uk_cross_selling_consent_application (application_no),
  CONSTRAINT fk_cross_selling_consent_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_cross_selling_consent_flags CHECK (agreed_flag IN ('Y','N') AND stop_method_acknowledged IN ('Y','N'))
) COMMENT='金融控股公司子公司間共同行銷同意書';

CREATE TABLE new_contract.investment_risk_assessment (
  assessment_id VARCHAR(36) NOT NULL COMMENT '投資風險評估識別碼',
  application_no VARCHAR(32) NOT NULL COMMENT '要保書號碼',
  questionnaire_version VARCHAR(20) NOT NULL COMMENT '風險問卷版本',
  customer_risk_level VARCHAR(4) NOT NULL COMMENT '客戶風險等級',
  product_risk_level VARCHAR(4) NOT NULL COMMENT '商品風險等級',
  risk_score INT NOT NULL COMMENT '風險問卷分數',
  suitable_flag CHAR(1) NOT NULL COMMENT '適合度判定',
  allocation_summary VARCHAR(1000) NOT NULL COMMENT '投資標的配置快照',
  disclosure_confirmed CHAR(1) NOT NULL COMMENT '投資風險是否確認',
  proposal_delivered CHAR(1) NOT NULL COMMENT '建議書與說明書是否交付',
  recording_required CHAR(1) NOT NULL COMMENT '是否需要錄音錄影或電子軌跡',
  recording_reference VARCHAR(200) NULL COMMENT '錄音錄影或電子軌跡參照',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '新增時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '修改人員',
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '覆核人員',
  reviewed_at TIMESTAMP(6) NULL COMMENT '覆核時間',
  PRIMARY KEY (assessment_id),
  UNIQUE KEY uk_investment_risk_application (application_no),
  CONSTRAINT fk_investment_risk_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_investment_risk_flags CHECK (suitable_flag IN ('Y','N') AND disclosure_confirmed IN ('Y','N')
    AND proposal_delivered IN ('Y','N') AND recording_required IN ('Y','N'))
) COMMENT='投資型保險風險屬性與商品適合度評估';

CREATE TABLE new_contract.application_attachment (
  attachment_id VARCHAR(36) NOT NULL COMMENT '附件識別碼',
  application_no VARCHAR(32) NOT NULL COMMENT '要保書號碼',
  attachment_type_code VARCHAR(32) NOT NULL COMMENT '附件類型代碼',
  owner_party_role VARCHAR(20) NOT NULL COMMENT '附件所屬關係人角色',
  document_no_masked VARCHAR(100) NULL COMMENT '文件編號遮罩值',
  file_name VARCHAR(255) NOT NULL COMMENT '原始檔名',
  file_reference VARCHAR(500) NOT NULL COMMENT '受控檔案儲存參照',
  file_hash VARCHAR(100) NULL COMMENT '檔案完整性雜湊',
  page_count INT NULL COMMENT '附件頁數',
  issue_date DATE NULL COMMENT '文件發證日',
  expiry_date DATE NULL COMMENT '文件到期日',
  verification_status CHAR(1) NOT NULL DEFAULT 'P' COMMENT '檢核狀態：P待檢核、S完成、R退回、C作廢',
  created_by VARCHAR(100) NOT NULL COMMENT '新增人員',
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '新增時間',
  updated_by VARCHAR(100) NOT NULL COMMENT '修改人員',
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改時間',
  reviewer_id VARCHAR(100) NULL COMMENT '覆核人員',
  reviewed_at TIMESTAMP(6) NULL COMMENT '覆核時間',
  PRIMARY KEY (attachment_id),
  KEY idx_application_attachment_application (application_no, attachment_type_code),
  CONSTRAINT fk_application_attachment_application FOREIGN KEY (application_no)
    REFERENCES new_contract.insurance_application (application_no),
  CONSTRAINT chk_application_attachment_status CHECK (verification_status IN ('P','S','R','C')),
  CONSTRAINT chk_application_attachment_dates CHECK (expiry_date IS NULL OR issue_date IS NULL OR expiry_date >= issue_date)
) COMMENT='要保案件附件資料';

INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag,
   effective_from, source_system, source_version)
VALUES
 ('new-contract','新契約作業','authorization_type_code','首期保費授權方式','B','銀行帳戶','BANK_ACCOUNT',1,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','authorization_type_code','首期保費授權方式','C','信用卡','CREDIT_CARD',2,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','APP','人身保險要保書','APPLICATION',1,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','ID','身分證明文件','IDENTITY',2,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','PAY','首期保費授權書','PAYMENT_AUTHORIZATION',3,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','CS','共同行銷同意書','CROSS_SELLING_CONSENT',4,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','INV','投資型商品風險文件','INVESTMENT_RISK',5,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','MED','健康告知／醫療文件','MEDICAL',6,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','FIN','財務／資金來源證明','FINANCIAL',7,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','TAX','CRS／FATCA聲明','TAX_RESIDENCY',8,'Y','2026-08-09','新契約作業','1.0'),
 ('new-contract','新契約作業','attachment_type_code','附件類型','OTH','其他補充文件','OTHER',99,'Y','2026-08-09','新契約作業','1.0');

-- 虛構的核保照會案例，只供本機查詢與 PDF 端到端測試。
INSERT INTO new_contract.insurance_application
(application_id,application_no,application_revision,application_date,received_at,channel_code,product_code,
 product_version,currency_code,sum_assured_amount,premium_amount,payment_mode_code,requested_effective_date,
 application_status,validation_result_status,validation_completed_at,submitted_at,source_system,created_by,updated_by)
VALUES
('00000000-0000-0000-0000-000000001101','DEMO-NC-INQ-001',1,CURRENT_DATE,CURRENT_TIMESTAMP(6),'AGENT',
 'LIFE-DEMO','1.0','TWD',800000.0000,9600.0000,'ANNUAL',DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),
 'UNDERWRITING','FAIL',CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6),'LOCAL_DEMO','system','system');

INSERT INTO new_contract.underwriting_case
(underwriting_case_no,application_no,underwriting_status,underwriting_decision_code,decision_reason_code,underwritten_at)
VALUES ('DEMO-UW-INQ-001','DEMO-NC-INQ-001','INQUIRY','PENDING_DOCUMENT','DOCUMENT_INCOMPLETE',CURRENT_TIMESTAMP(6));

INSERT INTO new_contract.underwriting_inquiry
(inquiry_no,underwriting_case_no,application_revision,inquiry_status,issued_at)
VALUES ('DEMO-INQ-001','DEMO-UW-INQ-001',1,'OPEN',CURRENT_TIMESTAMP(6));

INSERT INTO new_contract.underwriting_inquiry_item
(inquiry_item_id,inquiry_no,rule_code,item_message)
VALUES
('00000000-0000-0000-0000-000000001111','DEMO-INQ-001','TW_NB_DOCUMENT_COMPLETE','要保文件檢核未通過：請補附被保險人身分證明文件。'),
('00000000-0000-0000-0000-000000001112','DEMO-INQ-001','TW_NB_CONSENT_VERIFIED','聲明與同意檢核未通過：請確認健康告知聲明簽署內容。');

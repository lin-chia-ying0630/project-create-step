-- 第一組案例可能已在端到端驗證中撤回；保留第二組虛構案件供畫面操作。
INSERT INTO new_contract.insurance_application
(application_id,application_no,application_revision,application_date,received_at,channel_code,product_code,
 product_version,currency_code,sum_assured_amount,premium_amount,payment_mode_code,requested_effective_date,
 application_status,submitted_at,source_system,created_by,updated_by)
VALUES ('00000000-0000-0000-0000-000000000103','DEMO-NC-REV-002',1,CURRENT_DATE,CURRENT_TIMESTAMP(6),'WEB',
 'LIFE-DEMO','1.0','TWD',800000.0000,8000.0000,'ANNUAL',DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),
 'COMPLETED',CURRENT_TIMESTAMP(6),'LOCAL_DEMO','system','system');

INSERT INTO new_contract.underwriting_case
(underwriting_case_no,application_no,underwriting_status,underwriting_decision_code,underwriter_id,
 underwritten_at,policy_no)
VALUES ('DEMO-UW-REV-002','DEMO-NC-REV-002','APPROVED','STANDARD','demo-underwriter',
 CURRENT_TIMESTAMP(6),'DEMO-POL-002');

INSERT INTO main.policy_contract
(policy_contract_id,policy_no,source_application_id,application_no,underwriting_case_no,product_code,
 product_version,policy_status,currency_code,sum_assured_amount,premium_amount,effective_date,contract_date)
VALUES ('00000000-0000-0000-0000-000000000302','DEMO-POL-002',
 '00000000-0000-0000-0000-000000000103','DEMO-NC-REV-002','DEMO-UW-REV-002','LIFE-DEMO','1.0',
 'PENDING','TWD',800000.0000,8000.0000,DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),CURRENT_DATE);

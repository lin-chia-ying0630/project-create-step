-- 僅使用虛構編號與非個人資料，供本機 Docker 端到端測試。
INSERT INTO new_contract.insurance_application
(application_id,application_no,application_revision,application_date,received_at,channel_code,product_code,
 product_version,currency_code,sum_assured_amount,premium_amount,payment_mode_code,requested_effective_date,
 application_status,submitted_at,source_system,created_by,updated_by)
VALUES
('00000000-0000-0000-0000-000000000101','DEMO-NC-001',1,CURRENT_DATE,CURRENT_TIMESTAMP(6),'WEB',
 'LIFE-DEMO','1.0','TWD',1000000.0000,12000.0000,'ANNUAL',DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),
 'SUBMITTED',CURRENT_TIMESTAMP(6),'LOCAL_DEMO','system','system'),
('00000000-0000-0000-0000-000000000102','DEMO-NC-REV-001',1,CURRENT_DATE,CURRENT_TIMESTAMP(6),'WEB',
 'LIFE-DEMO','1.0','TWD',500000.0000,6000.0000,'ANNUAL',DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),
 'COMPLETED',CURRENT_TIMESTAMP(6),'LOCAL_DEMO','system','system');

INSERT INTO new_contract.initial_premium_due
(premium_due_id,application_no,application_revision,currency_code,calculated_premium_amount,
 calculation_rule_version,due_status,calculated_at)
VALUES ('00000000-0000-0000-0000-000000000201','DEMO-NC-001',1,'TWD',12000.0000,'DEMO-V1','PENDING',CURRENT_TIMESTAMP(6));

INSERT INTO new_contract.underwriting_case
(underwriting_case_no,application_no,underwriting_status,underwriting_decision_code,underwriter_id,
 underwritten_at,policy_no)
VALUES ('DEMO-UW-REV-001','DEMO-NC-REV-001','APPROVED','STANDARD','demo-underwriter',
 CURRENT_TIMESTAMP(6),'DEMO-POL-001');

INSERT INTO main.policy_contract
(policy_contract_id,policy_no,source_application_id,application_no,underwriting_case_no,product_code,
 product_version,policy_status,currency_code,sum_assured_amount,premium_amount,effective_date,contract_date)
VALUES ('00000000-0000-0000-0000-000000000301','DEMO-POL-001',
 '00000000-0000-0000-0000-000000000102','DEMO-NC-REV-001','DEMO-UW-REV-001','LIFE-DEMO','1.0',
 'PENDING','TWD',500000.0000,6000.0000,DATE_ADD(CURRENT_DATE,INTERVAL 7 DAY),CURRENT_DATE);

INSERT INTO new_contract.underwriting_batch_execution
(batch_execution_id,business_date,trigger_type,execution_status,started_at,completed_at,total_count,
 approved_count,inquiry_count,failed_count)
VALUES ('00000000-0000-0000-0000-000000000401',CURRENT_DATE,'SCHEDULED','COMPLETED',
 DATE_SUB(CURRENT_TIMESTAMP(6),INTERVAL 5 MINUTE),CURRENT_TIMESTAMP(6),3,1,1,1);

-- 建立 100 筆完全虛構的新契約受理與核保案件；13 個正式階段各至少 7 筆。
INSERT INTO new_contract.insurance_application
  (application_id,application_no,policy_no,policy_no_assigned_at,application_revision,application_date,received_at,
   channel_code,branch_code,insurance_agent_code,product_code,product_version,currency_code,sum_assured_amount,
   premium_amount,payment_mode_code,coverage_term_years,premium_payment_term_years,requested_effective_date,
   application_status,validation_result_status,validation_completed_at,submitted_at,completed_at,source_system,
   created_by,updated_by)
WITH RECURSIVE sequence_no AS (
  SELECT 1 AS number_value
  UNION ALL
  SELECT number_value + 1 FROM sequence_no WHERE number_value < 100
), test_case AS (
  SELECT number_value,
         ELT(MOD(number_value - 1,13) + 1,'AP','PW','NP','NW','NR','UW','US','NS','AS','RS','DS','CS','PS') AS stage_code
    FROM sequence_no
)
SELECT CONCAT('10000000-0000-0000-0000-',LPAD(number_value,12,'0')),
       CONCAT('TEST-NC-',LPAD(number_value,4,'0')),
       CONCAT('TEST-POL-',LPAD(number_value,4,'0')),
       CURRENT_TIMESTAMP(6),1,DATE_SUB(CURRENT_DATE,INTERVAL MOD(number_value,30) DAY),CURRENT_TIMESTAMP(6),
       CASE MOD(number_value,3) WHEN 0 THEN 'WEB' WHEN 1 THEN 'AGENT' ELSE 'BANK' END,
       CONCAT('TEST-BR-',LPAD(MOD(number_value,5) + 1,2,'0')),
       CONCAT('TEST-AGENT-',LPAD(MOD(number_value,10) + 1,2,'0')),
       CASE MOD(number_value,3) WHEN 0 THEN 'LIFE-DEMO' WHEN 1 THEN 'LIFE-TERM' ELSE 'LIFE-SAVING' END,
       '1.0','TWD',500000.0000 + number_value * 10000,6000.0000 + number_value * 100,
       CASE MOD(number_value,2) WHEN 0 THEN 'ANNUAL' ELSE 'MONTHLY' END,
       20,10,DATE_ADD(CURRENT_DATE,INTERVAL MOD(number_value,20) + 1 DAY),stage_code,'PASS',
       CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6),
       CASE WHEN stage_code IN ('AS','RS','DS','CS','PS') THEN CURRENT_TIMESTAMP(6) ELSE NULL END,
       'LOCAL_TEST_DATA','test-seeder','test-seeder'
  FROM test_case;

INSERT INTO new_contract.underwriting_case
  (underwriting_case_no,application_no,underwriting_status,underwriting_decision_code,contract_status_code,
   underwriter_id,decision_reason_code,underwritten_at,policy_no)
WITH RECURSIVE sequence_no AS (
  SELECT 1 AS number_value
  UNION ALL
  SELECT number_value + 1 FROM sequence_no WHERE number_value < 100
), test_case AS (
  SELECT number_value,
         ELT(MOD(number_value - 1,13) + 1,'AP','PW','NP','NW','NR','UW','US','NS','AS','RS','DS','CS','PS') AS stage_code
    FROM sequence_no
)
SELECT CONCAT('TEST-UW-',LPAD(number_value,4,'0')),
       CONCAT('TEST-NC-',LPAD(number_value,4,'0')),stage_code,
       CASE stage_code WHEN 'AS' THEN 'SA' WHEN 'RS' THEN 'DC' WHEN 'DS' THEN 'PO' WHEN 'CS' THEN 'CN' ELSE NULL END,
       CASE stage_code WHEN 'AS' THEN '01' WHEN 'RS' THEN '13' WHEN 'DS' THEN '14' WHEN 'CS' THEN '15' ELSE NULL END,
       CASE WHEN stage_code IN ('AS','RS','DS','CS') THEN 'test-underwriter' ELSE NULL END,
       CASE WHEN stage_code IN ('AS','RS','DS','CS') THEN 'TEST_DECISION' ELSE NULL END,
       CASE WHEN stage_code IN ('AS','RS','DS','CS') THEN CURRENT_TIMESTAMP(6) ELSE NULL END,
       CONCAT('TEST-POL-',LPAD(number_value,4,'0'))
  FROM test_case;

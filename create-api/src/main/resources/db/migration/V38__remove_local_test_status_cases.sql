-- V30 的 TEST-* 案件只適用本機展示；正式環境不可保留測試案件。
-- 使用 source_system 與固定前綴雙重限制刪除範圍，避免影響一般業務資料。
DELETE p
  FROM main.policy_contract p
  JOIN new_contract.insurance_application a
    ON a.application_no = p.application_no
 WHERE a.source_system = 'LOCAL_TEST_DATA'
   AND a.application_no LIKE 'TEST-NC-%';

DELETE u
  FROM new_contract.underwriting_case u
  JOIN new_contract.insurance_application a
    ON a.application_no = u.application_no
 WHERE a.source_system = 'LOCAL_TEST_DATA'
   AND a.application_no LIKE 'TEST-NC-%';

DELETE FROM new_contract.insurance_application
 WHERE source_system = 'LOCAL_TEST_DATA'
   AND application_no LIKE 'TEST-NC-%';

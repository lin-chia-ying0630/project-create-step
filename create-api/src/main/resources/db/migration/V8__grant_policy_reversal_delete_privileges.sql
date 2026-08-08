-- 承保撤回 use case 僅能刪除正式保單物化邊界，不授權 main schema 其他資料表。
GRANT DELETE ON main.policy_underwriting_condition TO 'insurance'@'%';
GRANT DELETE ON main.policy_beneficiary TO 'insurance'@'%';
GRANT DELETE ON main.policy_coverage TO 'insurance'@'%';
GRANT DELETE ON main.policy_party TO 'insurance'@'%';
GRANT DELETE ON main.policy_contract_evidence TO 'insurance'@'%';
GRANT DELETE ON main.policy_contract TO 'insurance'@'%';

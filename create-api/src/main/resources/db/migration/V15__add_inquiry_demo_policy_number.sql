UPDATE new_contract.insurance_application
   SET reserved_policy_no='DEMO-POL-INQ-001',
       policy_no_reserved_at=COALESCE(policy_no_reserved_at,CURRENT_TIMESTAMP(6)),
       updated_by='flyway',updated_at=CURRENT_TIMESTAMP(6)
 WHERE application_no='DEMO-NC-INQ-001'
   AND reserved_policy_no IS NULL;

INSERT INTO new_contract.application_party
(application_party_id,application_no,party_role_code,party_seq,customer_id,relationship_to_insured_code,customer_snapshot_reference)
SELECT UUID(),'DEMO-NC-INQ-001','APPLICANT',1,c.customer_id,'SELF',CONCAT('CUSTOMER:',c.customer_id,':V',c.record_version)
  FROM customer.customer_master c
 WHERE c.record_status='ACTIVE'
 ORDER BY c.created_at LIMIT 1;

INSERT INTO new_contract.application_party
(application_party_id,application_no,party_role_code,party_seq,customer_id,relationship_to_insured_code,customer_snapshot_reference)
SELECT UUID(),'DEMO-NC-INQ-001','INSURED',1,c.customer_id,'SELF',CONCAT('CUSTOMER:',c.customer_id,':V',c.record_version)
  FROM customer.customer_master c
 WHERE c.record_status='ACTIVE'
 ORDER BY c.created_at LIMIT 1;

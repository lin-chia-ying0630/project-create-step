ALTER TABLE customer.customer_master
  MODIFY COLUMN gender_code VARCHAR(16) NULL,
  MODIFY COLUMN birth_date DATE NULL;

CREATE TABLE customer.organization_profile (
  customer_id VARCHAR(36) NOT NULL,
  establishment_date DATE NULL,
  responsible_person_name VARCHAR(100) NOT NULL,
  industry_code VARCHAR(32) NOT NULL,
  organization_type_code VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (customer_id),
  CONSTRAINT fk_organization_customer FOREIGN KEY (customer_id) REFERENCES customer.customer_master(customer_id)
);

package tw.com.insurance.api.customer;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {
    @Insert("""
      INSERT INTO customer.customer_master
      (customer_id,customer_type_code,customer_name,gender_code,birth_date,nationality_code,
       residency_country_code,record_status,created_by,updated_by)
      VALUES (#{id},'PERSON',#{name},#{gender},#{birthDate},#{nationality},#{residency},
              'ACTIVE','local-tester','local-tester')
      """)
    int insertCustomer(@Param("id") String id,@Param("name") String name,@Param("gender") String gender,
        @Param("birthDate") LocalDate birthDate,@Param("nationality") String nationality,
        @Param("residency") String residency);

    @Insert("""
      INSERT INTO customer.customer_identity_document
      (identity_document_id,customer_id,identity_type_code,identity_no_hash,identity_no_ciphertext,
       identity_no_last4,issuing_country_code,verification_status,verified_at,is_primary)
      VALUES (#{id},#{customerId},#{type},#{hash},#{ciphertext},#{last4},#{country},'FORMAT_VERIFIED',CURRENT_TIMESTAMP(6),TRUE)
      """)
    int insertIdentity(@Param("id") String id,@Param("customerId") String customerId,@Param("type") String type,
        @Param("hash") String hash,@Param("ciphertext") byte[] ciphertext,@Param("last4") String last4,
        @Param("country") String country);

    @Insert("""
      INSERT INTO customer.customer_contact
      (contact_id,customer_id,contact_type_code,contact_value_ciphertext,contact_value_hash,
       contact_value_masked,is_primary,verification_status,effective_from)
      VALUES (#{id},#{customerId},#{type},#{ciphertext},#{hash},#{masked},TRUE,'UNVERIFIED',CURRENT_DATE)
      """)
    int insertContact(@Param("id") String id,@Param("customerId") String customerId,@Param("type") String type,
        @Param("ciphertext") byte[] ciphertext,@Param("hash") String hash,@Param("masked") String masked);

    @Insert("""
      INSERT INTO customer.customer_address
      (address_id,customer_id,address_type_code,postal_code,address_ciphertext,address_masked,effective_from)
      VALUES (#{id},#{customerId},'CONTACT',#{postalCode},#{ciphertext},#{masked},CURRENT_DATE)
      """)
    int insertAddress(@Param("id") String id,@Param("customerId") String customerId,
        @Param("postalCode") String postalCode,@Param("ciphertext") byte[] ciphertext,@Param("masked") String masked);

    @Insert("""
      INSERT INTO customer.customer_name_history
      (name_history_id,customer_id,customer_name,effective_from,change_reason_code)
      VALUES (#{id},#{customerId},#{name},CURRENT_DATE,'INITIAL_CREATE')
      """)
    int insertNameHistory(@Param("id") String id,@Param("customerId") String customerId,@Param("name") String name);

    @Insert("""
      INSERT INTO customer.customer_kyc_profile
      (kyc_profile_id,customer_id,occupation_code,source_of_funds_code,insurance_purpose_code,risk_level_code)
      VALUES (#{id},#{customerId},#{occupation},#{funds},#{purpose},'PENDING')
      """)
    int insertKyc(@Param("id") String id,@Param("customerId") String customerId,
        @Param("occupation") String occupation,@Param("funds") String funds,@Param("purpose") String purpose);

    @Insert("""
      INSERT INTO customer.customer_consent
      (consent_id,customer_id,consent_type_code,consent_version,consent_status,consented_at)
      VALUES (#{id},#{customerId},'PERSONAL_DATA_PROCESSING',#{version},'GRANTED',CURRENT_TIMESTAMP(6))
      """)
    int insertConsent(@Param("id") String id,@Param("customerId") String customerId,@Param("version") String version);

    @Insert("""
      INSERT INTO customer.customer_audit_event
      (audit_event_id,customer_id,operation_type,operator_id,request_id,changed_fields,occurred_at)
      VALUES (#{id},#{customerId},'CREATE','local-tester',#{requestId},JSON_ARRAY('CUSTOMER_CORE','IDENTITY','CONTACT','ADDRESS','KYC','CONSENT'),CURRENT_TIMESTAMP(6))
      """)
    int insertAudit(@Param("id") String id,@Param("customerId") String customerId,@Param("requestId") String requestId);
}

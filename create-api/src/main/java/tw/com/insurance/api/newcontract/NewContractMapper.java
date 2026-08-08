package tw.com.insurance.api.newcontract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NewContractMapper {
    @Insert("""
        INSERT INTO new_contract.insurance_application
        (application_id,application_no,application_revision,application_date,received_at,channel_code,
         product_code,product_version,currency_code,sum_assured_amount,premium_amount,payment_mode_code,
         requested_effective_date,application_status,submitted_at,source_system,created_by,updated_by)
        VALUES (#{id},#{applicationNo},1,#{applicationDate},CURRENT_TIMESTAMP(6),#{channelCode},#{productCode},
          #{productVersion},#{currencyCode},#{sumAssured},#{premium},#{paymentMode},#{effectiveDate},
          'SUBMITTED',CURRENT_TIMESTAMP(6),'CREATE_WEB','local-tester','local-tester')
        """)
    int insertApplication(@Param("id") String id,@Param("applicationNo") String applicationNo,
        @Param("applicationDate") LocalDate applicationDate,@Param("channelCode") String channelCode,
        @Param("productCode") String productCode,@Param("productVersion") String productVersion,
        @Param("currencyCode") String currencyCode,@Param("sumAssured") BigDecimal sumAssured,
        @Param("premium") BigDecimal premium,@Param("paymentMode") String paymentMode,
        @Param("effectiveDate") LocalDate effectiveDate);
    @Insert("""
        INSERT INTO new_contract.initial_premium_due
        (premium_due_id,application_no,application_revision,currency_code,calculated_premium_amount,
         calculation_rule_version,due_status,calculated_at)
        VALUES (#{id},#{applicationNo},1,#{currencyCode},#{premium},'ENTRY-V1','PENDING',CURRENT_TIMESTAMP(6))
        """)
    int insertPremiumDue(@Param("id") String id,@Param("applicationNo") String applicationNo,
                         @Param("currencyCode") String currencyCode,@Param("premium") BigDecimal premium);

    @Select("""
        SELECT application_no, premium_due_id, currency_code, calculated_premium_amount,
               calculation_rule_version, due_status
          FROM new_contract.initial_premium_due
         WHERE application_no=#{applicationNo} AND due_status IN ('PENDING','MATCHED')
         ORDER BY application_revision DESC LIMIT 1
        """)
    Map<String,Object> findPremiumDue(String applicationNo);

    @Insert("""
        INSERT INTO new_contract.remittance_slip
        (remittance_slip_id,remittance_slip_no,application_no,payment_method_code,payment_reference,
         currency_code,actual_paid_amount,remittance_status,paid_at,received_at,payer_relationship_code,entered_by)
        VALUES (#{id},#{slipNo},#{applicationNo},#{method},#{reference},#{currency},#{amount},#{status},
                #{paidAt},CURRENT_TIMESTAMP(6),#{relationship},'local-tester')
        """)
    int insertRemittance(@Param("id") String id, @Param("slipNo") String slipNo,
        @Param("applicationNo") String applicationNo, @Param("method") String method,
        @Param("reference") String reference, @Param("currency") String currency,
        @Param("amount") BigDecimal amount, @Param("status") String status,
        @Param("paidAt") LocalDateTime paidAt, @Param("relationship") String relationship);

    @Insert("""
        INSERT INTO new_contract.initial_premium_match
        (premium_match_id,premium_due_id,remittance_slip_id,expected_amount,actual_amount,difference_amount,
         currency_code,match_status,mismatch_reason_code,matched_at,matched_by)
        VALUES (#{id},#{dueId},#{slipId},#{expected},#{actual},#{difference},#{currency},#{status},
                #{reason},CURRENT_TIMESTAMP(6),'local-tester')
        """)
    int insertPremiumMatch(@Param("id") String id, @Param("dueId") String dueId,
        @Param("slipId") String slipId, @Param("expected") BigDecimal expected,
        @Param("actual") BigDecimal actual, @Param("difference") BigDecimal difference,
        @Param("currency") String currency, @Param("status") String status, @Param("reason") String reason);

    @Update("UPDATE new_contract.remittance_slip SET remittance_status=#{status} WHERE remittance_slip_id=#{id}")
    int updateRemittanceStatus(@Param("id") String id, @Param("status") String status);
    @Update("UPDATE new_contract.initial_premium_due SET due_status=#{status} WHERE premium_due_id=#{id}")
    int updateDueStatus(@Param("id") String id, @Param("status") String status);
    @Update("""
        UPDATE new_contract.insurance_application
           SET initial_premium_match_status=#{status},
               initial_premium_matched_at=CASE WHEN #{status}='MATCHED' THEN CURRENT_TIMESTAMP(6) ELSE NULL END,
               record_version=record_version+1,updated_by='local-tester',updated_at=CURRENT_TIMESTAMP(6)
         WHERE application_no=#{applicationNo}
        """)
    int updateApplicationMatch(@Param("applicationNo") String applicationNo, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM new_contract.insurance_application WHERE application_no=#{applicationNo}")
    int countApplication(String applicationNo);
    @Insert("""
        INSERT INTO new_contract.underwriting_batch_request
        (batch_request_id,application_no,requested_business_date,request_status,requested_by,requested_at)
        VALUES (#{id},#{applicationNo},#{businessDate},'PENDING','local-tester',CURRENT_TIMESTAMP(6))
        """)
    int insertBatchRequest(@Param("id") String id, @Param("applicationNo") String applicationNo,
                           @Param("businessDate") LocalDate businessDate);
    @Select("""
        SELECT batch_execution_id,business_date,execution_status,started_at,completed_at,total_count,
               approved_count,inquiry_count,failed_count
          FROM new_contract.underwriting_batch_execution ORDER BY started_at DESC LIMIT 20
        """)
    List<Map<String,Object>> findLatestExecutions();

    @Select("""
        SELECT p.policy_no,p.application_no,p.underwriting_case_no,p.policy_status,p.effective_date,
               p.record_version policy_version,a.application_status,a.record_version application_version,
               u.underwriting_status,u.record_version underwriting_version
          FROM main.policy_contract p
          JOIN new_contract.insurance_application a ON a.application_no=p.application_no
          JOIN new_contract.underwriting_case u ON u.underwriting_case_no=p.underwriting_case_no
         WHERE p.policy_no=#{policyNo}
        """)
    Map<String,Object> findPolicyForReversal(String policyNo);
    @Select("SELECT COUNT(*) FROM main.policy_contract WHERE policy_no=#{policyNo}")
    int countPolicy(String policyNo);
    @Delete("DELETE FROM main.policy_contract WHERE policy_no=#{policyNo} AND record_version=#{version}")
    int deletePolicy(@Param("policyNo") String policyNo, @Param("version") long version);
    @Update("""
        UPDATE new_contract.insurance_application SET application_status='SUBMITTED',validation_result_status=NULL,
          validation_completed_at=NULL,validation_batch_id=NULL,completed_at=NULL,record_version=record_version+1,
          updated_by='local-tester',updated_at=CURRENT_TIMESTAMP(6)
        WHERE application_no=#{applicationNo} AND record_version=#{version}
        """)
    int resetApplication(@Param("applicationNo") String applicationNo, @Param("version") long version);
    @Update("""
        UPDATE new_contract.underwriting_case SET underwriting_status='PENDING',underwriting_decision_code=NULL,
          underwriter_id=NULL,decision_reason_code=NULL,underwritten_at=NULL,policy_no=NULL,
          record_version=record_version+1,updated_at=CURRENT_TIMESTAMP(6)
        WHERE underwriting_case_no=#{caseNo} AND record_version=#{version}
        """)
    int resetUnderwriting(@Param("caseNo") String caseNo, @Param("version") long version);
    @Insert("""
        INSERT INTO new_contract.policy_issuance_reversal_audit
        (reversal_audit_id,policy_no,application_no,underwriting_case_no,reason_code,reason_description,
         operator_id,request_id,before_content,after_content,before_content_hash,after_content_hash,occurred_at)
        VALUES (#{auditId},#{policyNo},#{applicationNo},#{caseNo},#{reasonCode},#{reasonDescription},
          'local-tester',#{requestId},CAST(#{beforeJson} AS JSON),CAST(#{afterJson} AS JSON),
          #{beforeHash},#{afterHash},CURRENT_TIMESTAMP(6))
        """)
    int insertReversalAudit(@Param("auditId") String auditId, @Param("policyNo") String policyNo,
        @Param("applicationNo") String applicationNo, @Param("caseNo") String caseNo,
        @Param("reasonCode") String reasonCode, @Param("reasonDescription") String reasonDescription,
        @Param("requestId") String requestId, @Param("beforeJson") String beforeJson,
        @Param("afterJson") String afterJson, @Param("beforeHash") String beforeHash,
        @Param("afterHash") String afterHash);
}

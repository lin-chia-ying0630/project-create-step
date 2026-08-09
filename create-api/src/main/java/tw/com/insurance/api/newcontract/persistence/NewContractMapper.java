package tw.com.insurance.api.newcontract.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NewContractMapper {
	int insertApplication(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("applicationDate") LocalDate applicationDate, @Param("channelCode") String channelCode,
			@Param("branchCode") String branchCode, @Param("agentCode") String agentCode,
			@Param("productCode") String productCode, @Param("productVersion") String productVersion,
			@Param("currencyCode") String currencyCode, @Param("sumAssured") BigDecimal sumAssured,
			@Param("premium") BigDecimal premium, @Param("paymentMode") String paymentMode,
			@Param("effectiveDate") LocalDate effectiveDate, @Param("applicationStatus") String applicationStatus);
	Long findCustomerVersion(String customerId);
	int nextPolicyNumber();
	long lastInsertId();
	int reservePolicyNumber(@Param("applicationNo") String applicationNo, @Param("policyNo") String policyNo);
	List<Map<String, Object>> findApplicationsByQuery(String query);
	List<Map<String, Object>> findCoverageDetails(String applicationNo);
	List<Map<String, Object>> findBeneficiaryDetails(String applicationNo);
	List<Map<String, Object>> findHealthDisclosureDetails(String applicationNo);
	List<Map<String, Object>> findDeclarationDetails(String applicationNo);
	List<Map<String, Object>> findSignatureDetails(String applicationNo);
	List<Map<String, Object>> findCustomerContactDetails(String applicationNo);
	List<Map<String, Object>> findCustomerAddressDetails(String applicationNo);
	List<Map<String, Object>> findPremiumDueDetails(String applicationNo);
	int insertParty(@Param("id") String id, @Param("applicationNo") String applicationNo, @Param("role") String role,
			@Param("customerId") String customerId, @Param("relationship") String relationship,
			@Param("snapshot") String snapshot);
	int insertCoverage(@Param("id") String id, @Param("applicationNo") String applicationNo, @Param("seq") int seq,
			@Param("type") String type, @Param("productCode") String productCode,
			@Param("productVersion") String productVersion, @Param("insuredId") String insuredId,
			@Param("currency") String currency, @Param("sumAssured") BigDecimal sumAssured,
			@Param("premium") BigDecimal premium, @Param("coverageYears") Integer coverageYears,
			@Param("paymentYears") Integer paymentYears, @Param("effectiveDate") LocalDate effectiveDate);
	int insertBeneficiary(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("type") String type, @Param("seq") int seq, @Param("customerId") String customerId,
			@Param("designation") String designation, @Param("priority") int priority,
			@Param("allocation") BigDecimal allocation, @Param("relationship") String relationship);
	int insertHealthDisclosure(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("insuredId") String insuredId, @Param("questionCode") String questionCode,
			@Param("answer") byte[] answer, @Param("detail") byte[] detail);
	int insertDeclaration(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("type") String type, @Param("role") String role, @Param("evidence") String evidence);
	int insertSignature(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("role") String role, @Param("customerId") String customerId, @Param("method") String method,
			@Param("evidence") String evidence);
	int insertComplianceEvidence(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("type") String type, @Param("reference") String reference);
	int insertPremiumDue(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("currencyCode") String currencyCode, @Param("premium") BigDecimal premium);

	Map<String, Object> findPremiumDue(String applicationNo);

	int insertRemittance(@Param("id") String id, @Param("slipNo") String slipNo,
			@Param("applicationNo") String applicationNo, @Param("method") String method,
			@Param("reference") String reference, @Param("currency") String currency,
			@Param("amount") BigDecimal amount, @Param("status") String status, @Param("paidAt") LocalDateTime paidAt,
			@Param("relationship") String relationship);

	int insertPremiumMatch(@Param("id") String id, @Param("dueId") String dueId, @Param("slipId") String slipId,
			@Param("expected") BigDecimal expected, @Param("actual") BigDecimal actual,
			@Param("difference") BigDecimal difference, @Param("currency") String currency,
			@Param("status") String status, @Param("reason") String reason);

	int updateRemittanceStatus(@Param("id") String id, @Param("status") String status);
	int updateDueStatus(@Param("id") String id, @Param("status") String status);
	int updateApplicationMatch(@Param("applicationNo") String applicationNo, @Param("status") String status,
			@Param("readyStatus") String readyStatus);

	int countApplication(String applicationNo);
	String resolveApplicationNo(String number);
	String findReservedPolicyNo(String applicationNo);
	int insertBatchRequest(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("businessDate") LocalDate businessDate);
	List<Map<String, Object>> findLatestExecutions();

	Map<String, Object> findPolicyForReversal(String policyNo);
	int countPolicy(String policyNo);
	int deletePolicy(@Param("policyNo") String policyNo, @Param("version") long version);
	int resetApplication(@Param("applicationNo") String applicationNo, @Param("version") long version,
			@Param("applicationStatus") String applicationStatus);
	int resetUnderwriting(@Param("caseNo") String caseNo, @Param("version") long version,
			@Param("underwritingStatus") String underwritingStatus);
	int insertReversalAudit(@Param("auditId") String auditId, @Param("policyNo") String policyNo,
			@Param("applicationNo") String applicationNo, @Param("caseNo") String caseNo,
			@Param("reasonCode") String reasonCode, @Param("reasonDescription") String reasonDescription,
			@Param("requestId") String requestId, @Param("beforeJson") String beforeJson,
			@Param("afterJson") String afterJson, @Param("beforeHash") String beforeHash,
			@Param("afterHash") String afterHash);
}

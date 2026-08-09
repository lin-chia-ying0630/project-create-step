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
	long countApplicationQuery(@Param("query") String query);
	List<Map<String, Object>> findApplicationQueryPage(@Param("query") String query, @Param("offset") int offset,
			@Param("pageSize") int pageSize, @Param("sortField") String sortField,
			@Param("sortDirection") String sortDirection);
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
	int insertPremiumAuthorization(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("type") String type, @Param("payerRole") String payerRole, @Param("payerId") String payerId,
			@Param("relationship") String relationship, @Param("payerName") String payerName,
			@Param("institution") String institution, @Param("branch") String branch,
			@Param("token") String token, @Param("masked") String masked, @Param("expiryMonth") String expiryMonth,
			@Param("expiryYear") String expiryYear, @Param("authorizationDate") LocalDate authorizationDate,
			@Param("version") String version);
	int insertCrossSellingConsent(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("agreed") boolean agreed, @Param("version") String version,
			@Param("recipients") String recipients, @Param("scopes") String scopes,
			@Param("stopAcknowledged") boolean stopAcknowledged);
	int insertInvestmentRisk(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("version") String version, @Param("customerRisk") String customerRisk,
			@Param("productRisk") String productRisk, @Param("score") Integer score,
			@Param("suitable") boolean suitable, @Param("allocation") String allocation,
			@Param("disclosure") boolean disclosure, @Param("proposal") boolean proposal,
			@Param("recordingRequired") boolean recordingRequired,
			@Param("recordingReference") String recordingReference);
	int insertAttachment(@Param("id") String id, @Param("applicationNo") String applicationNo,
			@Param("type") String type, @Param("ownerRole") String ownerRole,
			@Param("documentNo") String documentNo, @Param("fileName") String fileName,
			@Param("fileReference") String fileReference, @Param("fileHash") String fileHash,
			@Param("pageCount") Integer pageCount, @Param("issueDate") LocalDate issueDate,
			@Param("expiryDate") LocalDate expiryDate);

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
	long countUnderwritingReviewCandidates(@Param("query") String query);
	List<Map<String, Object>> findUnderwritingReviewCandidates(@Param("query") String query, @Param("offset") int offset,
			@Param("pageSize") int pageSize, @Param("sortField") String sortField,
			@Param("sortDirection") String sortDirection);
	Map<String, Object> findUnderwritingReview(String query);
	int updateUnderwritingDecision(@Param("caseNo") String caseNo, @Param("version") long version,
			@Param("stageCode") String stageCode, @Param("decisionCode") String decisionCode,
			@Param("contractStatusCode") String contractStatusCode, @Param("reasonCode") String reasonCode,
			@Param("underwriterId") String underwriterId);
	int updateApplicationUnderwritingStage(@Param("applicationNo") String applicationNo,
			@Param("stageCode") String stageCode, @Param("reviewerId") String reviewerId);
	int insertUnderwritingDecisionAudit(@Param("auditId") String auditId, @Param("caseNo") String caseNo,
			@Param("applicationNo") String applicationNo, @Param("decisionCode") String decisionCode,
			@Param("stageCode") String stageCode, @Param("contractStatusCode") String contractStatusCode,
			@Param("reasonCode") String reasonCode, @Param("reasonDescription") String reasonDescription,
			@Param("operatorId") String operatorId);

	Map<String, Object> findPolicyForReversal(String policyNo);
	long countReversiblePolicies();
	List<Map<String, Object>> findReversiblePolicies(@Param("offset") int offset, @Param("pageSize") int pageSize,
			@Param("sortField") String sortField, @Param("sortDirection") String sortDirection);
	int countPolicy(String policyNo);
	int clearUnderwritingContractStatus(@Param("caseNo") String caseNo, @Param("version") long version,
			@Param("reviewerId") String reviewerId);
	int insertReversalAudit(@Param("auditId") String auditId, @Param("policyNo") String policyNo,
			@Param("applicationNo") String applicationNo, @Param("caseNo") String caseNo,
			@Param("reasonCode") String reasonCode, @Param("reasonDescription") String reasonDescription,
			@Param("requestId") String requestId, @Param("beforeJson") String beforeJson,
			@Param("afterJson") String afterJson, @Param("beforeHash") String beforeHash,
			@Param("afterHash") String afterHash);
}

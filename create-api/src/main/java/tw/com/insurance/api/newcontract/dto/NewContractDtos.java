package tw.com.insurance.api.newcontract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class NewContractDtos {
	private NewContractDtos() {
	}

	public record PremiumDuePreview(String applicationNo, String premiumDueId, String currencyCode,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal calculatedPremiumAmount,
			String calculationRuleVersion, String dueStatus, String dueStatusDescription) {
	}
	/** 新增送金單的可寫入欄位；核准前僅保存於加密覆核 payload。 */
	public record RemittanceSlipRequest(@NotBlank @Size(max = 32) String applicationNo,
			@JsonAlias("remittanceSlipNo") @NotBlank @Size(max = 50) String paymentReceiptNo,
			@JsonAlias("paymentMethodCode") @NotBlank @Size(max = 20) String paymentChannelCode,
			@JsonAlias("paymentReference") @NotBlank @Size(max = 100) String collectionReference,
			@NotBlank @Size(min = 3, max = 3) String currencyCode,
			@JsonAlias("actualPaidAmount") @NotNull @DecimalMin("0.0001") BigDecimal receivedAmount,
			@JsonAlias("paidAt") @NotNull LocalDateTime receivedAt,
			@JsonAlias("payerRelationshipCode") @Size(max = 20) String payerRoleCode) {
	}
	public record PremiumMatchResult(String premiumMatchId, String matchStatus, String matchStatusDescription,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal expectedAmount,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal actualAmount,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal differenceAmount, boolean mayUnderwrite) {
	}
	/** 指定單一保單與批次執行日；舊欄位 requestedBusinessDate 僅保留輸入相容。 */
	public record UnderwritingBatchRequest(@NotBlank @Size(max = 32) String applicationNo,
			@JsonAlias("requestedBusinessDate") @NotNull LocalDate executionDate) {
	}
	/** 覆核核准後建立的核保排程結果與預定啟動時間。 */
	public record UnderwritingBatchRequestResult(String batchRequestId, String applicationNo, String requestStatus,
			LocalDateTime scheduledAt) {
	}
	public record UnderwritingBatchExecutionSummary(String batchExecutionId, LocalDate businessDate,
			String executionStatus, LocalDateTime startedAt, LocalDateTime completedAt, int totalCount,
			int approvedCount, int inquiryCount, int failedCount) {
	}
	/** 人工核保審查送覆核的最小異動內容；原因不得包含健康告知原文。 */
	public record UnderwritingDecisionRequest(@NotBlank @Size(max = 32) String applicationNo,
			@NotBlank @Size(max = 2) String decisionCode, @NotBlank @Size(max = 32) String reasonCode,
			@NotBlank @Size(min = 5, max = 500) String reasonDescription, @NotNull Long expectedVersion) {
	}
	public record UnderwritingReviewPreview(String applicationNo, String policyNo, String underwritingCaseNo,
			String productCode, LocalDate applicationDate, LocalDate requestedEffectiveDate, String currencyCode,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal sumAssuredAmount,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal premiumAmount, String currentStageCode,
			String currentStageDescription, String currentDecisionCode, String currentContractStatusCode,
			String currentContractStatusDescription, String createdBy, LocalDateTime createdAt, String updatedBy,
			LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt, long recordVersion) {
	}
	/** 新契約受理檔中已完成照會、等待人工核保審查的清單資料。 */
	public record UnderwritingReviewSummary(String applicationNo, String policyNo, String underwritingCaseNo,
			String productCode, LocalDate applicationDate, LocalDate requestedEffectiveDate, String currentStageCode,
			String currentStageDescription, String createdBy, LocalDateTime createdAt, String updatedBy,
			LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt) {
	}
	/** 核保審查候選案件的標準分頁結果。 */
	public record UnderwritingReviewPage(List<UnderwritingReviewSummary> items, long totalItems, int page, int pageSize,
			int totalPages) {
	}
	/** 核保審查畫面使用的固定結果選項，由後端 enum 提供唯一代碼與狀態映射。 */
	public record UnderwritingOutcomeOption(String decisionCode, String decisionDescription, String stageCode,
			String stageDescription, String contractStatusCode, String contractStatusDescription, boolean insurable) {
	}
	public record UnderwritingDecisionResult(String applicationNo, String underwritingCaseNo, String decisionCode,
			String decisionDescription, String stageCode, String stageDescription, String contractStatusCode,
			String contractStatusDescription) {
	}
	public record PolicyReversalPreview(String policyNo, String applicationNo, String underwritingCaseNo,
			String policyStatus, String applicationStatus, String underwritingStatus, LocalDate effectiveDate,
			long policyVersion, long applicationVersion, long underwritingVersion, Map<String, Integer> deleteCounts,
			List<String> blockers, String confirmToken) {
	}
	public record PolicyReversalSummary(String policyNo, String applicationNo, String productCode,
			String contractStatusCode, LocalDate effectiveDate, String createdBy, LocalDateTime createdAt,
			String updatedBy, LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt) {
	}
	public record PolicyReversalPage(List<PolicyReversalSummary> items, long totalItems, int page, int pageSize,
			int totalPages) {
	}
	public record PolicyReversalRequest(@NotBlank @Size(max = 32) String policyNo,
			@NotBlank @Size(max = 32) String reasonCode, @NotBlank @Size(min = 10, max = 500) String reasonDescription,
			long expectedPolicyVersion, long expectedApplicationVersion, long expectedUnderwritingVersion,
			@NotBlank String confirmToken) {
	}
	public record CoverageInput(@NotBlank String coverageItemType, @NotBlank @Size(max = 32) String productCode,
			@NotBlank @Size(max = 32) String productVersion, @NotNull @DecimalMin("0.0001") BigDecimal sumAssuredAmount,
			@NotNull @DecimalMin("0.0000") BigDecimal premiumAmount, @Min(1) Integer coverageTermYears,
			@Min(1) Integer premiumPaymentTermYears) {
	}
	public record BeneficiaryInput(@NotBlank @Size(max = 20) String beneficiaryTypeCode,
			@Size(max = 36) String beneficiaryCustomerId, @Size(max = 32) String beneficiaryDesignationCode,
			@Min(1) int priorityNo, @DecimalMin("0.0001") BigDecimal allocationPercentage,
			@Size(max = 20) String relationshipToInsuredCode) {
	}
	public record HealthDisclosureInput(@NotBlank @Size(max = 32) String questionCode,
			@NotBlank @Size(max = 10) String answerCode, @Size(max = 2000) String supplementalDetail) {
	}
	public record PaymentInstrumentValidationRequest(@NotBlank @Size(max = 4) String instrumentTypeCode,
			@NotBlank @Size(max = 32) String instrumentNumber, @Size(max = 3) String bankCode,
			@Size(max = 4) String branchCode, @Size(max = 2) String expiryMonth, @Size(max = 4) String expiryYear) {
	}
	public record PaymentInstrumentValidationResult(String paymentToken, String maskedNumber, String validationStatus,
			String institutionCode) {
	}
	public record InitialPremiumAuthorizationInput(@NotBlank @Size(max = 4) String authorizationTypeCode,
			@NotBlank @Size(max = 20) String payerRoleCode, @NotBlank @Size(max = 36) String payerCustomerId,
			@NotBlank @Size(max = 20) String payerRelationshipCode, @NotBlank @Size(max = 100) String payerName,
			@Size(max = 3) String institutionCode, @Size(max = 4) String branchCode,
			@NotBlank @Size(max = 100) String paymentToken, @NotBlank @Size(max = 32) String maskedNumber,
			@Size(max = 2) String expiryMonth, @Size(max = 4) String expiryYear, @NotNull LocalDate authorizationDate,
			@NotBlank @Size(max = 20) String authorizationVersion,
			@AssertTrue(message = "須確認首期保費授權") boolean confirmed) {
	}
	public record CrossSellingConsentInput(boolean applicable, boolean agreed, @Size(max = 20) String consentVersion,
			@Size(max = 500) String recipientCompanies, @Size(max = 200) String dataScopeCodes,
			boolean stopMethodAcknowledged) {
	}
	public record InvestmentRiskInput(boolean applicable, @Size(max = 20) String questionnaireVersion,
			@Size(max = 4) String customerRiskLevel, @Size(max = 4) String productRiskLevel, Integer riskScore,
			boolean suitable, @Size(max = 1000) String allocationSummary, boolean disclosureConfirmed,
			boolean proposalDelivered, boolean recordingRequired, @Size(max = 200) String recordingReference) {
	}
	public record ApplicationAttachmentInput(@NotBlank @Size(max = 32) String attachmentTypeCode,
			@NotBlank @Size(max = 20) String ownerPartyRole, @Size(max = 100) String documentNoMasked,
			@NotBlank @Size(max = 255) String fileName, @NotBlank @Size(max = 500) String fileReference,
			@Size(max = 100) String fileHash, @Min(1) Long fileSizeBytes, Integer pageCount, LocalDate issueDate,
			LocalDate expiryDate) {
	}
	public record CreateApplicationRequest(@NotBlank @Size(max = 32) String applicationNo,
			@NotNull LocalDate applicationDate, @NotBlank @Size(max = 20) String channelCode,
			@Size(max = 20) String branchCode, @Size(max = 32) String insuranceAgentCode,
			@NotBlank @Size(max = 36) String applicantCustomerId, @NotBlank @Size(max = 36) String insuredCustomerId,
			@NotBlank @Size(max = 20) String applicantRelationshipToInsuredCode,
			@NotBlank @Size(min = 3, max = 3) String currencyCode, @NotBlank @Size(max = 16) String paymentModeCode,
			@NotNull LocalDate requestedEffectiveDate, boolean electronicPolicy, boolean investmentProduct,
			@NotBlank @Size(max = 32) String fundsSourceCode, @NotBlank @Size(max = 32) String insurancePurposeCode,
			@NotNull @Size(min = 1, max = 20) List<@Valid CoverageInput> coverages,
			@NotNull @Size(min = 1, max = 20) List<@Valid BeneficiaryInput> beneficiaries,
			@NotNull @Size(min = 1, max = 100) List<@Valid HealthDisclosureInput> healthDisclosures,
			@AssertTrue(message = "須確認據實告知") boolean truthfulDisclosureConfirmed,
			@AssertTrue(message = "須同意個人資料處理") boolean personalDataConsentConfirmed,
			@AssertTrue(message = "須確認已審閱條款") boolean termsReviewedConfirmed,
			@AssertTrue(message = "要保人須完成簽署") boolean applicantSignatureConfirmed,
			@AssertTrue(message = "被保險人須完成簽署") boolean insuredSignatureConfirmed,
			@NotBlank @Size(max = 20) String signatureMethod,
			@NotNull @Valid InitialPremiumAuthorizationInput initialPremiumAuthorization,
			@NotNull @Valid CrossSellingConsentInput crossSellingConsent,
			@NotNull @Valid InvestmentRiskInput investmentRisk,
			@NotNull @Size(min = 1, max = 30) List<@Valid ApplicationAttachmentInput> attachments) {
	}
	public record CreateApplicationResult(String applicationId, String applicationNo, String applicationStatus,
			String premiumDueId, @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal calculatedPremiumAmount,
			String currencyCode) {
	}
	public record PolicyNumberReservationResult(String applicationNo, String policyNo, String policyNumberStatus,
			LocalDateTime reservedAt) {
	}
	public record CoverageDetail(int coverageItemSeq, String coverageItemType, String productCode,
			String productVersion, String currencyCode, BigDecimal sumAssuredAmount, BigDecimal premiumAmount,
			Integer coverageTermYears, Integer premiumPaymentTermYears, LocalDate requestedEffectiveDate) {
	}
	public record BeneficiaryDetail(String beneficiaryTypeCode, int beneficiarySeq, String beneficiaryCustomerReference,
			String beneficiaryDesignationCode, int priorityNo, BigDecimal allocationPercentage,
			String relationshipToInsuredCode) {
	}
	public record HealthDisclosureDetail(String questionSetCode, String questionSetVersion, String questionCode,
			String answerCode, String supplementalDetail, LocalDateTime answeredAt, LocalDateTime confirmedAt) {
	}
	public record DeclarationDetail(String declarationTypeCode, String declarationVersion, String confirmedByPartyRole,
			String confirmationMethod, LocalDateTime confirmedAt) {
	}
	public record SignatureDetail(String signerPartyRole, String signerCustomerReference, String signatureMethod,
			LocalDateTime signedAt, LocalDateTime verifiedAt) {
	}
	public record CustomerContactDetail(String partyRoleCode, String contactTypeCode, String contactValueMasked,
			boolean primaryContact, String verificationStatus, LocalDate effectiveFrom, LocalDate effectiveTo) {
	}
	public record CustomerAddressDetail(String partyRoleCode, String addressTypeCode, String postalCode,
			String addressMasked, LocalDate effectiveFrom, LocalDate effectiveTo) {
	}
	public record PremiumDueDetail(String premiumDueId, String currencyCode, BigDecimal calculatedPremiumAmount,
			String calculationRuleVersion, String dueStatus, LocalDateTime calculatedAt) {
	}
	public record ApplicationQueryResult(String applicationNo, String policyNo, String policyNumberStatus,
			String applicationStatus, String applicationStatusDescription, LocalDate applicationDate,
			String newContractStage, String newContractStageDescription, String contractStatus,
			String contractStatusDescription, LocalDate requestedEffectiveDate, String channelCode, String branchCode,
			String insuranceAgentCode, String productCode, String productVersion, String paymentModeCode,
			String currencyCode, @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal sumAssuredAmount,
			@JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal premiumAmount, String applicantCustomerId,
			String applicantName, String insuredCustomerId, String insuredName, List<CoverageDetail> coverages,
			List<BeneficiaryDetail> beneficiaries, List<HealthDisclosureDetail> healthDisclosures,
			List<DeclarationDetail> declarations, List<SignatureDetail> signatures,
			List<CustomerContactDetail> customerContacts, List<CustomerAddressDetail> customerAddresses,
			List<PremiumDueDetail> premiumDues) {
	}
	public record ApplicationQuerySummary(String applicationNo, String policyNo, String productCode,
			String applicationStatus, String applicationStatusDescription, LocalDate applicationDate,
			LocalDate requestedEffectiveDate, String createdBy, LocalDateTime createdAt, String updatedBy,
			LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt) {
	}
	public record ApplicationQueryPage(List<ApplicationQuerySummary> items, long totalItems, int page, int pageSize,
			int totalPages) {
	}
	public record PolicyReversalResult(String reversalAuditId, String policyNo, String applicationNo,
			String applicationStatus, String underwritingStatus) {
	}
}

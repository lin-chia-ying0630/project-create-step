package tw.com.insurance.api.newcontract.service.impl;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.BeneficiaryDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.BeneficiaryInput;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CoverageDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CoverageInput;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CustomerAddressDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CustomerContactDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.DeclarationDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.HealthDisclosureDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.HealthDisclosureInput;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyNumberReservationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDueDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDuePreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumMatchResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.RemittanceSlipRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.SignatureDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchExecutionSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequestResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.newcontract.domain.NewContractApplicationStatus;
import tw.com.insurance.api.newcontract.domain.NewContractErrorCode;
import tw.com.insurance.api.newcontract.persistence.NewContractMapper;
import tw.com.insurance.api.newcontract.service.NewContractService;

@Service
public class NewContractServiceImpl implements NewContractService {
	private final NewContractMapper mapper;
	private final byte[] piiKey;

	public NewContractServiceImpl(NewContractMapper mapper, @Value("${app.pii-encryption-key}") String keyText) {
		this.mapper = mapper;
		if (keyText == null || keyText.length() < 24)
			throw new IllegalStateException("PII_ENCRYPTION_KEY 長度至少需要 24 字元");
		this.piiKey = sha256(keyText);
	}

	@Transactional
	public CreateApplicationResult createApplication(CreateApplicationRequest request) {
		if (request.requestedEffectiveDate().isBefore(request.applicationDate()))
			throw new BusinessException(NewContractErrorCode.INVALID_EFFECTIVE_DATE);
		Long applicantVersion = mapper.findCustomerVersion(request.applicantCustomerId());
		Long insuredVersion = mapper.findCustomerVersion(request.insuredCustomerId());
		if (applicantVersion == null || insuredVersion == null)
			throw new BusinessException(NewContractErrorCode.INVALID_PARTY);
		List<CoverageInput> bases = request.coverages().stream().filter(c -> "BASE".equals(c.coverageItemType()))
				.toList();
		if (bases.size() != 1)
			throw new BusinessException(NewContractErrorCode.INVALID_BASE_COVERAGE);
		if (request.coverages().stream().anyMatch(c -> !List.of("BASE", "RIDER").contains(c.coverageItemType())))
			throw new BusinessException(NewContractErrorCode.INVALID_COVERAGE_TYPE);
		validateBeneficiaries(request.beneficiaries());
		request.healthDisclosures().forEach(h -> {
			if ("YES".equals(h.answerCode()) && (h.supplementalDetail() == null || h.supplementalDetail().isBlank()))
				throw new BusinessException(NewContractErrorCode.HEALTH_DETAIL_REQUIRED);
		});
		BigDecimal sumAssured = request.coverages().stream().map(CoverageInput::sumAssuredAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal premium = request.coverages().stream().map(CoverageInput::premiumAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		CoverageInput base = bases.get(0);
		String applicationId = UUID.randomUUID().toString(), dueId = UUID.randomUUID().toString();
		try {
			mapper.insertApplication(applicationId, request.applicationNo(), request.applicationDate(),
					request.channelCode(), request.branchCode(), request.insuranceAgentCode(), base.productCode(),
					base.productVersion(), request.currencyCode(), sumAssured, premium, request.paymentModeCode(),
					request.requestedEffectiveDate());
			mapper.insertParty(UUID.randomUUID().toString(), request.applicationNo(), "APPLICANT",
					request.applicantCustomerId(), request.applicantRelationshipToInsuredCode(),
					snapshot(request.applicantCustomerId(), applicantVersion));
			mapper.insertParty(UUID.randomUUID().toString(), request.applicationNo(), "INSURED",
					request.insuredCustomerId(), "SELF", snapshot(request.insuredCustomerId(), insuredVersion));
			int seq = 1;
			for (CoverageInput coverage : request.coverages())
				mapper.insertCoverage(UUID.randomUUID().toString(), request.applicationNo(), seq++,
						coverage.coverageItemType(), coverage.productCode(), coverage.productVersion(),
						request.insuredCustomerId(), request.currencyCode(), coverage.sumAssuredAmount(),
						coverage.premiumAmount(), coverage.coverageTermYears(), coverage.premiumPaymentTermYears(),
						request.requestedEffectiveDate());
			seq = 1;
			for (BeneficiaryInput beneficiary : request.beneficiaries())
				mapper.insertBeneficiary(UUID.randomUUID().toString(), request.applicationNo(),
						beneficiary.beneficiaryTypeCode(), seq++, blankToNull(beneficiary.beneficiaryCustomerId()),
						blankToNull(beneficiary.beneficiaryDesignationCode()), beneficiary.priorityNo(),
						beneficiary.allocationPercentage(), beneficiary.relationshipToInsuredCode());
			for (HealthDisclosureInput health : request.healthDisclosures())
				mapper.insertHealthDisclosure(UUID.randomUUID().toString(), request.applicationNo(),
						request.insuredCustomerId(), health.questionCode(), encrypt(health.answerCode()),
						health.supplementalDetail() == null || health.supplementalDetail().isBlank()
								? null
								: encrypt(health.supplementalDetail()));
			mapper.insertDeclaration(UUID.randomUUID().toString(), request.applicationNo(), "TRUTHFUL_DISCLOSURE",
					"INSURED", "UI-CHECKBOX");
			mapper.insertDeclaration(UUID.randomUUID().toString(), request.applicationNo(), "PERSONAL_DATA_CONSENT",
					"APPLICANT", "UI-CHECKBOX");
			mapper.insertDeclaration(UUID.randomUUID().toString(), request.applicationNo(), "TERMS_REVIEWED",
					"APPLICANT", "UI-CHECKBOX");
			if (request.electronicPolicy())
				mapper.insertDeclaration(UUID.randomUUID().toString(), request.applicationNo(), "E_POLICY_CONSENT",
						"APPLICANT", "UI-CHECKBOX");
			mapper.insertSignature(UUID.randomUUID().toString(), request.applicationNo(), "APPLICANT",
					request.applicantCustomerId(), request.signatureMethod(), "UI-CONFIRMATION");
			mapper.insertSignature(UUID.randomUUID().toString(), request.applicationNo(), "INSURED",
					request.insuredCustomerId(), request.signatureMethod(), "UI-CONFIRMATION");
			mapper.insertComplianceEvidence(UUID.randomUUID().toString(), request.applicationNo(), "FUNDS_SOURCE",
					request.fundsSourceCode());
			mapper.insertComplianceEvidence(UUID.randomUUID().toString(), request.applicationNo(), "INSURANCE_PURPOSE",
					request.insurancePurposeCode());
			mapper.insertPremiumDue(dueId, request.applicationNo(), request.currencyCode(), premium);
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(NewContractErrorCode.DUPLICATE_APPLICATION);
		}
		return new CreateApplicationResult(applicationId, request.applicationNo(), "SUBMITTED", dueId, premium,
				request.currencyCode());
	}

	@Transactional
	public PolicyNumberReservationResult reservePolicyNumber(String applicationNo) {
		Map<String, Object> current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElse(null);
		if (current == null)
			throw new BusinessException(NewContractErrorCode.APPLICATION_NOT_FOUND);
		String existing = text(current, "reserved_policy_no");
		if (existing != null && !existing.isBlank())
			return new PolicyNumberReservationResult(applicationNo, existing, "RESERVED",
					localDateTime(current.get("policy_no_reserved_at")));
		mapper.nextPolicyNumber();
		String policyNo = "N" + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
				+ String.format("%010d", mapper.lastInsertId());
		if (mapper.reservePolicyNumber(applicationNo, policyNo) != 1) {
			current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElse(null);
			return new PolicyNumberReservationResult(applicationNo, text(current, "reserved_policy_no"), "RESERVED",
					localDateTime(current.get("policy_no_reserved_at")));
		}
		current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElseThrow();
		return new PolicyNumberReservationResult(applicationNo, policyNo, "RESERVED",
				localDateTime(current.get("policy_no_reserved_at")));
	}

	public List<ApplicationQueryResult> queryApplication(String query) {
		List<Map<String, Object>> rows = mapper.findApplicationsByQuery(query);
		if (rows.isEmpty())
			throw new BusinessException(NewContractErrorCode.QUERY_NOT_FOUND);
		return rows.stream().map(this::toApplicationQueryResult).toList();
	}

	private ApplicationQueryResult toApplicationQueryResult(Map<String, Object> row) {
		String status = text(row, "application_status");
		String policyNo = text(row, "reserved_policy_no");
		var applicationStatus = NewContractApplicationStatus.fromCode(status);
		String applicationNo = text(row, "application_no");
		List<CoverageDetail> coverages = mapper.findCoverageDetails(applicationNo).stream()
				.map(x -> new CoverageDetail(number(x, "coverage_item_seq"), text(x, "coverage_item_type"),
						text(x, "product_code"), text(x, "product_version"), text(x, "currency_code"),
						decimal(x, "sum_assured_amount"), decimal(x, "premium_amount"),
						nullableNumber(x, "coverage_term_years"), nullableNumber(x, "premium_payment_term_years"),
						localDate(x.get("requested_effective_date"))))
				.toList();
		List<BeneficiaryDetail> beneficiaries = mapper.findBeneficiaryDetails(applicationNo).stream()
				.map(x -> new BeneficiaryDetail(text(x, "beneficiary_type_code"), number(x, "beneficiary_seq"),
						maskReference(text(x, "beneficiary_customer_id")), text(x, "beneficiary_designation_code"),
						number(x, "priority_no"), decimal(x, "allocation_percentage"),
						text(x, "relationship_to_insured_code")))
				.toList();
		List<HealthDisclosureDetail> disclosures = mapper.findHealthDisclosureDetails(applicationNo).stream()
				.map(x -> new HealthDisclosureDetail(text(x, "question_set_code"), text(x, "question_set_version"),
						text(x, "disclosure_question_code"), decrypt((byte[]) x.get("answer_code_encrypted")),
						decrypt((byte[]) x.get("supplemental_detail_encrypted")), localDateTime(x.get("answered_at")),
						localDateTime(x.get("confirmed_at"))))
				.toList();
		List<DeclarationDetail> declarations = mapper.findDeclarationDetails(applicationNo).stream()
				.map(x -> new DeclarationDetail(text(x, "declaration_type_code"), text(x, "declaration_version"),
						text(x, "confirmed_by_party_role"), text(x, "confirmation_method"),
						localDateTime(x.get("confirmed_at"))))
				.toList();
		List<SignatureDetail> signatures = mapper.findSignatureDetails(applicationNo).stream()
				.map(x -> new SignatureDetail(text(x, "signer_party_role"),
						maskReference(text(x, "signer_customer_id")), text(x, "signature_method"),
						localDateTime(x.get("signed_at")), localDateTime(x.get("verified_at"))))
				.toList();
		List<CustomerContactDetail> contacts = mapper.findCustomerContactDetails(applicationNo).stream()
				.map(x -> new CustomerContactDetail(text(x, "party_role_code"), text(x, "contact_type_code"),
						text(x, "contact_value_masked"), Boolean.TRUE.equals(x.get("is_primary")),
						text(x, "verification_status"), localDate(x.get("effective_from")),
						localDate(x.get("effective_to"))))
				.toList();
		List<CustomerAddressDetail> addresses = mapper.findCustomerAddressDetails(applicationNo).stream()
				.map(x -> new CustomerAddressDetail(text(x, "party_role_code"), text(x, "address_type_code"),
						text(x, "postal_code"), text(x, "address_masked"), localDate(x.get("effective_from")),
						localDate(x.get("effective_to"))))
				.toList();
		List<PremiumDueDetail> premiumDues = mapper.findPremiumDueDetails(applicationNo).stream()
				.map(x -> new PremiumDueDetail(text(x, "premium_due_id"), text(x, "currency_code"),
						decimal(x, "calculated_premium_amount"), text(x, "calculation_rule_version"),
						text(x, "due_status"), localDateTime(x.get("calculated_at"))))
				.toList();
		return new ApplicationQueryResult(text(row, "application_no"), policyNo,
				policyNo == null ? "NOT_RESERVED" : "RESERVED", status, applicationStatus.description(),
				localDate(row.get("application_date")), applicationStatus.stageCode(),
				applicationStatus.stageDescription(), applicationStatus.contractStatusCode(),
				applicationStatus.contractStatusDescription(), localDate(row.get("requested_effective_date")),
				text(row, "channel_code"), text(row, "branch_code"), text(row, "insurance_agent_code"),
				text(row, "product_code"), text(row, "product_version"), text(row, "payment_mode_code"),
				text(row, "currency_code"), decimal(row, "sum_assured_amount"), decimal(row, "premium_amount"),
				maskReference(text(row, "applicant_customer_id")), maskName(text(row, "applicant_name")),
				maskReference(text(row, "insured_customer_id")), maskName(text(row, "insured_name")), coverages,
				beneficiaries, disclosures, declarations, signatures, contacts, addresses, premiumDues);
	}

	private static String maskReference(String value) {
		if (value == null || value.length() < 9)
			return "****";
		return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
	}
	private static String maskName(String value) {
		if (value == null || value.isBlank())
			return "***";
		if (value.length() == 1)
			return "*";
		if (value.length() == 2)
			return value.substring(0, 1) + "*";
		return value.substring(0, 1) + "*".repeat(Math.min(4, value.length() - 2))
				+ value.substring(value.length() - 1);
	}
	private String decrypt(byte[] value) {
		if (value == null)
			return null;
		try {
			byte[] iv = Arrays.copyOfRange(value, 0, 12);
			byte[] encrypted = Arrays.copyOfRange(value, 12, value.length);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(piiKey, "AES"), new GCMParameterSpec(128, iv));
			return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("敏感資料解密失敗", e);
		}
	}

	private static void validateBeneficiaries(List<BeneficiaryInput> beneficiaries) {
		boolean invalidTarget = beneficiaries.stream()
				.anyMatch(b -> (hasText(b.beneficiaryCustomerId()) == hasText(b.beneficiaryDesignationCode())));
		if (invalidTarget)
			throw new BusinessException(NewContractErrorCode.INVALID_BENEFICIARY);
		List<BeneficiaryInput> allocated = beneficiaries.stream().filter(b -> b.allocationPercentage() != null)
				.toList();
		if (!allocated.isEmpty() && allocated.stream().map(BeneficiaryInput::allocationPercentage)
				.reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(new BigDecimal("100")) != 0)
			throw new BusinessException(NewContractErrorCode.INVALID_BENEFICIARY_ALLOCATION);
	}
	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
	private static String blankToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}
	private static String snapshot(String customerId, long version) {
		return "CUSTOMER:" + customerId + ":V" + version;
	}
	private byte[] encrypt(String value) {
		try {
			byte[] iv = new byte[12];
			new SecureRandom().nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(piiKey, "AES"), new GCMParameterSpec(128, iv));
			byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
			byte[] result = Arrays.copyOf(iv, iv.length + encrypted.length);
			System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
			return result;
		} catch (Exception e) {
			throw new IllegalStateException("健康告知資料加密失敗", e);
		}
	}
	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public PremiumDuePreview getPremiumDue(String applicationNo) {
		Map<String, Object> row = mapper.findPremiumDue(applicationNo);
		if (row == null)
			throw new BusinessException(NewContractErrorCode.PREMIUM_DUE_NOT_FOUND);
		return new PremiumDuePreview(text(row, "application_no"), text(row, "premium_due_id"),
				text(row, "currency_code"), decimal(row, "calculated_premium_amount"),
				text(row, "calculation_rule_version"), text(row, "due_status"),
				dueStatusDescription(text(row, "due_status")));
	}

	@Transactional
	public PremiumMatchResult matchPremium(RemittanceSlipRequest request) {
		PremiumDuePreview due = getPremiumDue(request.applicationNo());
		BigDecimal difference = request.receivedAmount().subtract(due.calculatedPremiumAmount());
		String status;
		String reason = null;
		if (!due.currencyCode().equals(request.currencyCode())) {
			status = "CURRENCY_MISMATCH";
			reason = "CURRENCY_MISMATCH";
		} else if (difference.signum() < 0) {
			status = "UNDERPAID";
			reason = "AMOUNT_UNDERPAID";
		} else if (difference.signum() > 0) {
			status = "OVERPAID";
			reason = "AMOUNT_OVERPAID";
		} else
			status = "MATCHED";
		String slipId = UUID.randomUUID().toString(), matchId = UUID.randomUUID().toString();
		try {
			mapper.insertRemittance(slipId, request.paymentReceiptNo(), request.applicationNo(),
					request.paymentChannelCode(), request.collectionReference(), request.currencyCode(),
					request.receivedAmount(), status.equals("MATCHED") ? "MATCHED" : "RECEIVED", request.receivedAt(),
					request.payerRoleCode());
			mapper.insertPremiumMatch(matchId, due.premiumDueId(), slipId, due.calculatedPremiumAmount(),
					request.receivedAmount(), difference, request.currencyCode(), status, reason);
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(NewContractErrorCode.PAYMENT_ALREADY_MATCHED);
		}
		if (status.equals("MATCHED"))
			mapper.updateDueStatus(due.premiumDueId(), "MATCHED");
		mapper.updateApplicationMatch(request.applicationNo(), status);
		return new PremiumMatchResult(matchId, status, matchStatusDescription(status), due.calculatedPremiumAmount(),
				request.receivedAmount(), difference, status.equals("MATCHED"));
	}

	private static String dueStatusDescription(String status) {
		return switch (status) {
			case "PENDING" -> "待收款";
			case "MATCHED" -> "已銷帳";
			default -> status;
		};
	}
	private static String matchStatusDescription(String status) {
		return switch (status) {
			case "MATCHED" -> "已銷帳";
			case "UNDERPAID" -> "短收";
			case "OVERPAID" -> "溢收";
			case "CURRENCY_MISMATCH" -> "幣別不符";
			default -> status;
		};
	}

	@Transactional
	public UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request) {
		String applicationNo = mapper.resolveApplicationNo(request.applicationNo());
		if (applicationNo == null)
			throw new BusinessException(NewContractErrorCode.APPLICATION_NOT_FOUND);
		if (mapper.findReservedPolicyNo(applicationNo) == null)
			reservePolicyNumber(applicationNo);
		String id = UUID.randomUUID().toString();
		try {
			mapper.insertBatchRequest(id, applicationNo, request.requestedBusinessDate());
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(NewContractErrorCode.DUPLICATE_BATCH_REQUEST);
		}
		return new UnderwritingBatchRequestResult(id, applicationNo, "PENDING",
				LocalDateTime.of(request.requestedBusinessDate(), LocalTime.of(21, 0)));
	}

	public List<UnderwritingBatchExecutionSummary> latestExecutions() {
		return mapper.findLatestExecutions().stream()
				.map(row -> new UnderwritingBatchExecutionSummary(text(row, "batch_execution_id"),
						localDate(row.get("business_date")), text(row, "execution_status"),
						localDateTime(row.get("started_at")), localDateTime(row.get("completed_at")),
						number(row, "total_count"), number(row, "approved_count"), number(row, "inquiry_count"),
						number(row, "failed_count")))
				.toList();
	}

	public PolicyReversalPreview previewReversal(String policyNo) {
		Map<String, Object> row = mapper.findPolicyForReversal(policyNo);
		if (row == null)
			throw new BusinessException(NewContractErrorCode.POLICY_NOT_FOUND);
		List<String> blockers = "PENDING".equals(text(row, "policy_status"))
				? List.of()
				: List.of("保單狀態不是 PENDING，可能已生效或已有權利義務，禁止直接刪除");
		Map<String, Integer> counts = new LinkedHashMap<>();
		counts.put("main.policy_underwriting_condition", 0);
		counts.put("main.policy_beneficiary", 0);
		counts.put("main.policy_coverage", 0);
		counts.put("main.policy_party", 0);
		counts.put("main.policy_contract", mapper.countPolicy(policyNo));
		long policyVersion = longNumber(row, "policy_version"), appVersion = longNumber(row, "application_version"),
				uwVersion = longNumber(row, "underwriting_version");
		return new PolicyReversalPreview(policyNo, text(row, "application_no"), text(row, "underwriting_case_no"),
				text(row, "policy_status"), text(row, "application_status"), text(row, "underwriting_status"),
				localDate(row.get("effective_date")), policyVersion, appVersion, uwVersion, counts, blockers,
				hash(policyNo + ":" + policyVersion + ":" + appVersion + ":" + uwVersion));
	}

	@Transactional
	public PolicyReversalResult reverse(PolicyReversalRequest request, String requestId) {
		PolicyReversalPreview preview = previewReversal(request.policyNo());
		if (!preview.blockers().isEmpty())
			throw new BusinessException(NewContractErrorCode.POLICY_REVERSAL_BLOCKED);
		if (preview.policyVersion() != request.expectedPolicyVersion()
				|| preview.applicationVersion() != request.expectedApplicationVersion()
				|| preview.underwritingVersion() != request.expectedUnderwritingVersion()
				|| !preview.confirmToken().equals(request.confirmToken()))
			throw new BusinessException(NewContractErrorCode.CONCURRENT_MODIFICATION);
		String auditId = UUID.randomUUID().toString();
		String before = "{\"policyNo\":\"" + preview.policyNo() + "\",\"policyStatus\":\"" + preview.policyStatus()
				+ "\",\"applicationStatus\":\"" + preview.applicationStatus() + "\",\"underwritingStatus\":\""
				+ preview.underwritingStatus() + "\"}";
		String after = "{\"policyDeleted\":true,\"applicationStatus\":\"SUBMITTED\",\"underwritingStatus\":\"PENDING\"}";
		if (mapper.deletePolicy(request.policyNo(), request.expectedPolicyVersion()) != 1
				|| mapper.resetApplication(preview.applicationNo(), request.expectedApplicationVersion()) != 1
				|| mapper.resetUnderwriting(preview.underwritingCaseNo(), request.expectedUnderwritingVersion()) != 1)
			throw new BusinessException(NewContractErrorCode.CONCURRENT_MODIFICATION);
		mapper.insertReversalAudit(auditId, request.policyNo(), preview.applicationNo(), preview.underwritingCaseNo(),
				request.reasonCode(), request.reasonDescription(), requestId, before, after, hash(before), hash(after));
		return new PolicyReversalResult(auditId, request.policyNo(), preview.applicationNo(), "SUBMITTED", "PENDING");
	}

	private static String text(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : String.valueOf(value);
	}
	private static BigDecimal decimal(Map<String, Object> row, String key) {
		return (BigDecimal) row.get(key);
	}
	private static int number(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).intValue();
	}
	private static Integer nullableNumber(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : ((Number) value).intValue();
	}
	private static long longNumber(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).longValue();
	}
	private static LocalDate localDate(Object value) {
		return value instanceof Date d ? d.toLocalDate() : (LocalDate) value;
	}
	private static LocalDateTime localDateTime(Object value) {
		return value == null ? null : value instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) value;
	}
	private static String hash(String value) {
		try {
			return HexFormat.of()
					.formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}
}

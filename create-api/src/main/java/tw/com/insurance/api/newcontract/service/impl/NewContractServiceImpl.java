package tw.com.insurance.api.newcontract.service.impl;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationAttachmentInput;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQuerySummary;
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
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDueDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDuePreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumMatchResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.RemittanceSlipRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.SignatureDetail;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchExecutionSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequestResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingDecisionRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingDecisionResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingOutcomeOption;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import tw.com.insurance.api.common.util.PageSortRequest;
import tw.com.insurance.api.newcontract.domain.NewContractApplicationStatus;
import tw.com.insurance.api.newcontract.domain.NewContractErrorCode;
import tw.com.insurance.api.newcontract.domain.UnderwritingDecisionOutcome;
import tw.com.insurance.api.newcontract.codedefinition.service.CodeDefinitionService;
import tw.com.insurance.api.newcontract.persistence.NewContractMapper;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.service.ProductDefinitionService;
import tw.com.insurance.api.newcontract.service.NewContractService;

@Service
public class NewContractServiceImpl implements NewContractService {
	private final NewContractMapper mapper;
	private final CodeDefinitionService codeDefinitionService;
	private final ProductDefinitionService productDefinitionService;
	private final byte[] piiKey;

	public NewContractServiceImpl(NewContractMapper mapper, CodeDefinitionService codeDefinitionService,
			ProductDefinitionService productDefinitionService, @Value("${app.pii-encryption-key}") String keyText) {
		this.mapper = mapper;
		this.codeDefinitionService = codeDefinitionService;
		this.productDefinitionService = productDefinitionService;
		if (keyText == null || keyText.length() < 24)
			throw new IllegalStateException("PII_ENCRYPTION_KEY 長度至少需要 24 字元");
		this.piiKey = sha256(keyText);
	}

	/** 驗證銀行帳號或信用卡格式並產生不可逆 Token；方法不保存完整號碼。 */
	@Override
	public PaymentInstrumentValidationResult validatePaymentInstrument(PaymentInstrumentValidationRequest request) {
		String number = request.instrumentNumber().replaceAll("[\\s-]", "");
		boolean bank = "B".equals(request.instrumentTypeCode());
		boolean card = "C".equals(request.instrumentTypeCode());
		boolean valid = bank
				? number.matches("\\d{6,20}") && request.bankCode() != null && request.bankCode().matches("\\d{3}")
				: card && number.matches("\\d{13,19}") && luhn(number)
						&& validExpiry(request.expiryMonth(), request.expiryYear());
		if (!valid)
			throw new BusinessException(NewContractErrorCode.INVALID_PAYMENT_INSTRUMENT);
		String tokenSource = HexFormat.of().formatHex(piiKey) + ":" + request.instrumentTypeCode() + ":" + number;
		String token = "PAY-" + HexFormat.of().formatHex(sha256(tokenSource));
		String masked = "*".repeat(Math.max(0, number.length() - 4)) + number.substring(number.length() - 4);
		return new PaymentInstrumentValidationResult(token, masked, "S", request.bankCode());
	}

	/** 信用卡基本檢查碼；通過不代表發卡機構已授權扣款。 */
	private boolean luhn(String number) {
		int sum = 0;
		boolean doubleDigit = false;
		for (int index = number.length() - 1; index >= 0; index--) {
			int digit = number.charAt(index) - '0';
			if (doubleDigit && (digit *= 2) > 9)
				digit -= 9;
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return sum % 10 == 0;
	}

	/** 驗證信用卡有效年月仍在臺北目前月份之後。 */
	private boolean validExpiry(String month, String year) {
		if (month == null || year == null || !month.matches("0[1-9]|1[0-2]") || !year.matches("\\d{4}"))
			return false;
		java.time.YearMonth expiry = java.time.YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
		return !expiry.isBefore(java.time.YearMonth.now(java.time.ZoneId.of("Asia/Taipei")));
	}

	@Transactional
	public CreateApplicationResult createApplication(CreateApplicationRequest request) {
		if (!codeDefinitionService.isActiveCode("new-contract", "currency_code", request.currencyCode()))
			throw new BusinessException(NewContractErrorCode.INVALID_CURRENCY);
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
		ProductDefinitionDto baseProduct = null;
		LocalDate insuredBirthDate = mapper.findCustomerBirthDate(request.insuredCustomerId());
		CoverageInput baseCoverage = bases.get(0);
		for (CoverageInput coverage : request.coverages()) {
			ProductDefinitionDto product = productDefinitionService.requireActiveProduct(coverage.productCode(),
					coverage.productVersion());
			if (!coverage.coverageItemType().equals(product.coverageItemType())
					|| !request.currencyCode().equals(product.currencyCode()))
				throw new BusinessException(NewContractErrorCode.INVALID_PRODUCT);
			validateProductAmountLimits(coverage, product);
			validateProductTerms(coverage, product);
			validateEntryAge(insuredBirthDate, request.applicationDate(), product);
			if (!productDefinitionService.supportsPaymentMode(coverage.productCode(), coverage.productVersion(),
					request.paymentModeCode()))
				throw new BusinessException(NewContractErrorCode.PRODUCT_PAYMENT_MODE_VIOLATION);
			if ("RIDER".equals(coverage.coverageItemType())
					&& !productDefinitionService.supportsRider(baseCoverage.productCode(),
							baseCoverage.productVersion(), coverage.productCode(), coverage.productVersion()))
				throw new BusinessException(NewContractErrorCode.PRODUCT_RIDER_VIOLATION);
			if ("BASE".equals(coverage.coverageItemType()))
				baseProduct = product;
		}
		boolean investmentProduct = baseProduct != null && baseProduct.investmentProduct();
		request.attachments().forEach(this::validateAttachment);
		validateBeneficiaries(request.beneficiaries());
		request.healthDisclosures().forEach(h -> {
			if ("YES".equals(h.answerCode()) && (h.supplementalDetail() == null || h.supplementalDetail().isBlank()))
				throw new BusinessException(NewContractErrorCode.HEALTH_DETAIL_REQUIRED);
		});
		if (!request.initialPremiumAuthorization().paymentToken().startsWith("PAY-")
				|| request.initialPremiumAuthorization().maskedNumber().length() < 4)
			throw new BusinessException(NewContractErrorCode.PAYMENT_INSTRUMENT_NOT_VALIDATED);
		if (investmentProduct && (!request.investmentRisk().applicable() || !request.investmentRisk().suitable()
				|| !request.investmentRisk().disclosureConfirmed() || !request.investmentRisk().proposalDelivered()))
			throw new BusinessException(NewContractErrorCode.INVESTMENT_SUITABILITY_REQUIRED);
		if (investmentProduct && (!codeDefinitionService.isActiveCode("new-contract", "customer_risk_level_code",
				request.investmentRisk().customerRiskLevel())
				|| !codeDefinitionService.isActiveCode("new-contract", "product_risk_level_code",
						request.investmentRisk().productRiskLevel())
				|| !baseProduct.productRiskLevelCode().equals(request.investmentRisk().productRiskLevel())))
			throw new BusinessException(NewContractErrorCode.INVESTMENT_SUITABILITY_REQUIRED);
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
					request.requestedEffectiveDate(),
					NewContractApplicationStatus.APPLICATION_ACCEPTED.newContractStageCode());
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
			var authorization = request.initialPremiumAuthorization();
			mapper.insertPremiumAuthorization(UUID.randomUUID().toString(), request.applicationNo(),
					authorization.authorizationTypeCode(), authorization.payerRoleCode(),
					authorization.payerCustomerId(), authorization.payerRelationshipCode(), authorization.payerName(),
					authorization.institutionCode(), authorization.branchCode(), authorization.paymentToken(),
					authorization.maskedNumber(), authorization.expiryMonth(), authorization.expiryYear(),
					authorization.authorizationDate(), authorization.authorizationVersion());
			if (request.crossSellingConsent().applicable()) {
				var consent = request.crossSellingConsent();
				mapper.insertCrossSellingConsent(UUID.randomUUID().toString(), request.applicationNo(),
						consent.agreed(), consent.consentVersion(), consent.recipientCompanies(),
						consent.dataScopeCodes(), consent.stopMethodAcknowledged());
			}
			if (investmentProduct) {
				var risk = request.investmentRisk();
				mapper.insertInvestmentRisk(UUID.randomUUID().toString(), request.applicationNo(),
						risk.questionnaireVersion(), risk.customerRiskLevel(), risk.productRiskLevel(),
						risk.riskScore(), risk.suitable(), risk.allocationSummary(), risk.disclosureConfirmed(),
						risk.proposalDelivered(), risk.recordingRequired(), risk.recordingReference());
			}
			request.attachments()
					.forEach(attachment -> mapper.insertAttachment(UUID.randomUUID().toString(),
							request.applicationNo(), attachment.attachmentTypeCode(), attachment.ownerPartyRole(),
							attachment.documentNoMasked(), attachment.fileName(), attachment.fileReference(),
							attachment.fileHash(), attachment.fileSizeBytes(), attachment.pageCount(),
							attachment.issueDate(), attachment.expiryDate()));
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(NewContractErrorCode.DUPLICATE_APPLICATION);
		}
		reservePolicyNumber(request.applicationNo());
		var acceptedStage = NewContractApplicationStatus.APPLICATION_ACCEPTED;
		return new CreateApplicationResult(applicationId, request.applicationNo(),
				acceptedStage.newContractStageCode(), acceptedStage.newContractStageNameEn(),
				acceptedStage.newContractStageDescriptionZhTw(), dueId, premium, request.currencyCode());
	}

	/** 驗證登打金額落在商品定義的承保範圍，避免前端提示被繞過。 */
	private void validateProductAmountLimits(CoverageInput coverage, ProductDefinitionDto product) {
		if ((product.minimumSumAssured() != null
				&& coverage.sumAssuredAmount().compareTo(product.minimumSumAssured()) < 0)
				|| (product.maximumSumAssured() != null
						&& coverage.sumAssuredAmount().compareTo(product.maximumSumAssured()) > 0)
				|| (product.minimumPremium() != null
						&& coverage.premiumAmount().compareTo(product.minimumPremium()) < 0))
			throw new BusinessException(NewContractErrorCode.PRODUCT_LIMIT_VIOLATION);
	}

	/** 驗證保險期間與繳費期間符合商品版本限制。 */
	private void validateProductTerms(CoverageInput coverage, ProductDefinitionDto product) {
		if (outside(coverage.coverageTermYears(), product.minimumCoverageTermYears(),
				product.maximumCoverageTermYears())
				|| outside(coverage.premiumPaymentTermYears(), product.minimumPaymentTermYears(),
						product.maximumPaymentTermYears()))
			throw new BusinessException(NewContractErrorCode.PRODUCT_TERM_VIOLATION);
	}

	/** 以要保日足歲驗證商品版本投保年齡。 */
	private void validateEntryAge(LocalDate birthDate, LocalDate applicationDate, ProductDefinitionDto product) {
		if (birthDate == null)
			return;
		int age = Period.between(birthDate, applicationDate).getYears();
		if (outside(age, product.minimumEntryAge(), product.maximumEntryAge()))
			throw new BusinessException(NewContractErrorCode.INVALID_PRODUCT);
	}

	/** 附件僅接受安全檔名、受控參照、10MB 以下檔案與選填 SHA-256。 */
	private void validateAttachment(ApplicationAttachmentInput attachment) {
		String lowerName = attachment.fileName().toLowerCase(java.util.Locale.ROOT);
		boolean allowedExtension = lowerName.endsWith(".pdf") || lowerName.endsWith(".jpg")
				|| lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
		boolean invalidReference = attachment.fileReference().contains("..")
				|| attachment.fileReference().matches("(?i)^https?://.*");
		boolean invalidHash = attachment.fileHash() != null && !attachment.fileHash().isBlank()
				&& !attachment.fileHash().matches("(?i)^[0-9a-f]{64}$");
		boolean invalidSize = attachment.fileSizeBytes() != null && attachment.fileSizeBytes() > 10_485_760L;
		if (!allowedExtension || invalidReference || invalidHash || invalidSize)
			throw new BusinessException(NewContractErrorCode.INVALID_ATTACHMENT);
	}

	/** 判斷數值是否超出可為空白的上下限。 */
	private boolean outside(Integer value, Integer minimum, Integer maximum) {
		return value != null && ((minimum != null && value < minimum) || (maximum != null && value > maximum));
	}

	@Transactional
	public PolicyNumberReservationResult reservePolicyNumber(String applicationNo) {
		Map<String, Object> current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElse(null);
		if (current == null)
			throw new BusinessException(NewContractErrorCode.APPLICATION_NOT_FOUND);
		String existing = text(current, "policy_no");
		if (existing != null && !existing.isBlank())
			return new PolicyNumberReservationResult(applicationNo, existing, "ASSIGNED",
					localDateTime(current.get("policy_no_assigned_at")));
		mapper.nextPolicyNumber();
		String policyNo = "N" + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
				+ String.format("%010d", mapper.lastInsertId());
		if (mapper.reservePolicyNumber(applicationNo, policyNo) != 1) {
			current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElse(null);
			return new PolicyNumberReservationResult(applicationNo, text(current, "policy_no"), "ASSIGNED",
					localDateTime(current.get("policy_no_assigned_at")));
		}
		current = mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElseThrow();
		return new PolicyNumberReservationResult(applicationNo, policyNo, "ASSIGNED",
				localDateTime(current.get("policy_no_assigned_at")));
	}

	public List<ApplicationQueryResult> queryApplication(String query) {
		List<Map<String, Object>> rows = mapper.findApplicationsByQuery(query);
		if (rows.isEmpty())
			throw new BusinessException(NewContractErrorCode.QUERY_NOT_FOUND);
		return rows.stream().map(this::toApplicationQueryResult).toList();
	}

	/** 以後端分頁列出保單資料，查詢條件僅接受完整客戶或保單識別值。 */
	@Override
	@Transactional(readOnly = true)
	public ApplicationQueryPage queryApplications(String query, int page, int pageSize, String sort) {
		PageSortRequest pageQuery = PageSortRequest.of(page, pageSize, sort,
				Set.of("applicationNo", "policyNo", "productCode"), "applicationNo");
		String exactQuery = query == null || query.isBlank() ? null : query.trim();
		long totalItems = mapper.countApplicationQuery(exactQuery);
		List<ApplicationQuerySummary> items = mapper.findApplicationQueryPage(exactQuery, pageQuery.offset(),
				pageQuery.pageSize(), pageQuery.sortField(), pageQuery.sortDirection()).stream().map(row -> {
					NewContractApplicationStatus status = NewContractApplicationStatus
							.fromCode(text(row, "application_status"));
					return new ApplicationQuerySummary(text(row, "application_no"), text(row, "policy_no"),
							text(row, "product_code"), status.newContractStageCode(),
							status.newContractStageNameEn(), status.newContractStageDescriptionZhTw(),
							localDate(row.get("application_date")), localDate(row.get("requested_effective_date")),
							text(row, "created_by"), localDateTime(row.get("created_at")), text(row, "updated_by"),
							localDateTime(row.get("updated_at")), text(row, "reviewer_id"),
							localDateTime(row.get("reviewed_at")));
				}).toList();
		return new ApplicationQueryPage(items, totalItems, pageQuery.page(), pageQuery.pageSize(),
				pageQuery.totalPages(totalItems));
	}

	private ApplicationQueryResult toApplicationQueryResult(Map<String, Object> row) {
		String status = text(row, "application_status");
		String policyNo = text(row, "policy_no");
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
				policyNo == null ? "NOT_ASSIGNED" : "ASSIGNED", applicationStatus.newContractStageCode(),
				applicationStatus.newContractStageNameEn(), applicationStatus.newContractStageDescriptionZhTw(),
				localDate(row.get("application_date")), applicationStatus.contractStatusCode(),
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

	/** 覆核核准後建立送金單與銷帳結果；多表寫入在同一交易內同成同敗。 */
	@Transactional
	public PremiumMatchResult matchPremium(RemittanceSlipRequest request) {
		// 繳費管道與繳款人身分是營運可維護代碼，正式寫入前必須再次以資料庫代碼定義驗證。
		if (!codeDefinitionService.isActiveCode("initial-premium", "payment_channel_code",
				request.paymentChannelCode()))
			throw new BusinessException(NewContractErrorCode.INVALID_PAYMENT_CHANNEL);
		if (!codeDefinitionService.isActiveCode("initial-premium", "payer_role_code", request.payerRoleCode()))
			throw new BusinessException(NewContractErrorCode.INVALID_PAYER_ROLE);
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
		mapper.updateApplicationMatch(request.applicationNo(), status,
				NewContractApplicationStatus.WAITING_POLICY_ISSUANCE.newContractStageCode());
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

	/** 核准後將保單排入指定執行日；過期日期與重複排程均拒絕寫入。 */
	@Transactional
	public UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request) {
		// 執行日以排程器相同的 Asia/Taipei 日界線判斷；已過日期不得補排，避免產生永遠不會被領取的案件。
		if (request.executionDate().isBefore(LocalDate.now(java.time.ZoneId.of("Asia/Taipei"))))
			throw new BusinessException(NewContractErrorCode.INVALID_BATCH_EXECUTION_DATE);
		String applicationNo = mapper.resolveApplicationNo(request.applicationNo());
		if (applicationNo == null)
			throw new BusinessException(NewContractErrorCode.APPLICATION_NOT_FOUND);
		if (!NewContractApplicationStatus.WAITING_POLICY_ISSUANCE.newContractStageCode()
				.equals(text(mapper.findApplicationsByQuery(applicationNo).stream().findFirst().orElseThrow(),
						"application_status")))
			throw new BusinessException(NewContractErrorCode.APPLICATION_NOT_READY_FOR_BATCH);
		String id = UUID.randomUUID().toString();
		try {
			mapper.insertBatchRequest(id, applicationNo, request.executionDate());
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(NewContractErrorCode.DUPLICATE_BATCH_REQUEST);
		}
		return new UnderwritingBatchRequestResult(id, applicationNo, "W",
				LocalDateTime.of(request.executionDate(), LocalTime.of(21, 0)));
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

	/** 以新契約受理檔為清單主體，只提供 NS 照會結束且待審查的案件。 */
	@Override
	@Transactional(readOnly = true)
	public UnderwritingReviewPage findUnderwritingReviewCandidates(String queryValue, int page, int pageSize,
			String sort) {
		PageSortRequest query = PageSortRequest.of(page, pageSize, sort,
				Set.of("applicationNo", "policyNo", "productCode"), "applicationNo");
		String exactQuery = queryValue == null || queryValue.isBlank() ? null : queryValue.trim();
		long totalItems = mapper.countUnderwritingReviewCandidates(exactQuery);
		List<UnderwritingReviewSummary> items = mapper
				.findUnderwritingReviewCandidates(exactQuery, query.offset(), query.pageSize(), query.sortField(),
						query.sortDirection())
				.stream()
				.map(row -> new UnderwritingReviewSummary(text(row, "application_no"), text(row, "policy_no"),
						text(row, "underwriting_case_no"), text(row, "product_code"),
						localDate(row.get("application_date")), localDate(row.get("requested_effective_date")),
						stage(text(row, "underwriting_status")).newContractStageCode(),
						stage(text(row, "underwriting_status")).newContractStageNameEn(),
						stage(text(row, "underwriting_status")).newContractStageDescriptionZhTw(),
						text(row, "created_by"), localDateTime(row.get("created_at")), text(row, "updated_by"),
						localDateTime(row.get("updated_at")), text(row, "reviewer_id"),
						localDateTime(row.get("reviewed_at"))))
				.toList();
		return new UnderwritingReviewPage(items, totalItems, query.page(), query.pageSize(),
				query.totalPages(totalItems));
	}

	/** 查詢人工核保審查所需的目前階段、結果、契約狀態與樂觀鎖版本。 */
	@Override
	@Transactional(readOnly = true)
	public UnderwritingReviewPreview previewUnderwritingReview(String query) {
		Map<String, Object> row = mapper.findUnderwritingReview(query);
		if (row == null)
			throw new BusinessException(NewContractErrorCode.UNDERWRITING_CASE_NOT_FOUND);
		NewContractApplicationStatus newContractStage = stage(text(row, "underwriting_status"));
		String contractStatusCode = text(row, "contract_status_code");
		return new UnderwritingReviewPreview(text(row, "application_no"), text(row, "policy_no"),
				text(row, "underwriting_case_no"), text(row, "product_code"), localDate(row.get("application_date")),
				localDate(row.get("requested_effective_date")), text(row, "currency_code"),
				decimal(row, "sum_assured_amount"), decimal(row, "premium_amount"),
				newContractStage.newContractStageCode(), newContractStage.newContractStageNameEn(),
				newContractStage.newContractStageDescriptionZhTw(), text(row, "underwriting_decision_code"),
				contractStatusCode,
				contractStatusDescription(contractStatusCode), text(row, "created_by"),
				localDateTime(row.get("created_at")), text(row, "updated_by"), localDateTime(row.get("updated_at")),
				text(row, "reviewer_id"), localDateTime(row.get("reviewed_at")), longNumber(row, "record_version"));
	}

	/** 由固定 enum 回傳所有可承保及不承保結果，避免前端維護第二份對照。 */
	@Override
	public List<UnderwritingOutcomeOption> findUnderwritingOutcomes() {
		return Arrays.stream(UnderwritingDecisionOutcome.values())
				.map(outcome -> new UnderwritingOutcomeOption(outcome.decisionCode(), outcome.decisionDescription(),
						outcome.newContractStageCode(), outcome.newContractStageNameEn(),
						outcome.newContractStageDescriptionZhTw(), outcome.contractStatusCode(),
						outcome.contractStatusDescription(), outcome.insurable()))
				.toList();
	}

	/** 套用固定核保結果對照，並在同一交易更新案件及寫入決行稽核。 */
	@Override
	@Transactional
	public UnderwritingDecisionResult decideUnderwriting(UnderwritingDecisionRequest request, String operatorId) {
		UnderwritingDecisionOutcome outcome;
		try {
			outcome = UnderwritingDecisionOutcome.fromDecisionCode(request.decisionCode());
		} catch (IllegalArgumentException exception) {
			throw new BusinessException(NewContractErrorCode.INVALID_UNDERWRITING_DECISION);
		}
		Map<String, Object> row = mapper.findUnderwritingReview(request.applicationNo());
		if (row == null)
			throw new BusinessException(NewContractErrorCode.UNDERWRITING_CASE_NOT_FOUND);
		String caseNo = text(row, "underwriting_case_no");
		if (mapper.updateUnderwritingDecision(caseNo, request.expectedVersion(), outcome.newContractStageCode(),
				outcome.decisionCode(), outcome.contractStatusCode(), request.reasonCode(), operatorId) != 1)
			throw new BusinessException(NewContractErrorCode.UNDERWRITING_CONCURRENT_MODIFICATION);
		mapper.updateApplicationUnderwritingStage(request.applicationNo(), outcome.newContractStageCode(), operatorId);
		mapper.insertUnderwritingDecisionAudit(UUID.randomUUID().toString(), caseNo, request.applicationNo(),
				outcome.decisionCode(), outcome.newContractStageCode(), outcome.contractStatusCode(), request.reasonCode(),
				request.reasonDescription(), operatorId);
		return new UnderwritingDecisionResult(request.applicationNo(), caseNo, outcome.decisionCode(),
				outcome.decisionDescription(), outcome.newContractStageCode(), outcome.newContractStageNameEn(),
				outcome.newContractStageDescriptionZhTw(),
				outcome.contractStatusCode(), outcome.contractStatusDescription());
	}

	public PolicyReversalPreview previewReversal(String policyNo) {
		Map<String, Object> row = mapper.findPolicyForReversal(policyNo);
		if (row == null)
			throw new BusinessException(NewContractErrorCode.POLICY_NOT_FOUND);
		List<String> blockers = "01".equals(text(row, "contract_status_code"))
				? List.of()
				: List.of("契約狀態不是 01 有效，不符合承保撤回條件");
		Map<String, Integer> counts = new LinkedHashMap<>();
		counts.put("保留正式保單主檔", mapper.countPolicy(policyNo));
		counts.put("契約狀態 01 改為空白", 1);
		long policyVersion = longNumber(row, "policy_version"), appVersion = longNumber(row, "application_version"),
				uwVersion = longNumber(row, "underwriting_version");
		NewContractApplicationStatus applicationStage = stage(text(row, "application_status"));
		return new PolicyReversalPreview(policyNo, text(row, "application_no"), text(row, "underwriting_case_no"),
				text(row, "policy_status"), applicationStage.newContractStageCode(),
				applicationStage.newContractStageNameEn(), applicationStage.newContractStageDescriptionZhTw(),
				text(row, "underwriting_status"),
				localDate(row.get("effective_date")), policyVersion, appVersion, uwVersion, counts, blockers,
				hash(policyNo + ":" + policyVersion + ":" + appVersion + ":" + uwVersion));
	}

	/** 分頁列出契約狀態為 01、可進一步檢查承保撤回條件的保單。 */
	@Override
	@Transactional(readOnly = true)
	public PolicyReversalPage findReversiblePolicies(int page, int pageSize, String sort) {
		PageSortRequest query = PageSortRequest.of(page, pageSize, sort,
				Set.of("policyNo", "applicationNo", "productCode"), "policyNo");
		long totalItems = mapper.countReversiblePolicies();
		List<PolicyReversalSummary> items = mapper
				.findReversiblePolicies(query.offset(), query.pageSize(), query.sortField(), query.sortDirection())
				.stream()
				.map(row -> new PolicyReversalSummary(text(row, "policy_no"), text(row, "application_no"),
						text(row, "product_code"), text(row, "contract_status_code"),
						localDate(row.get("effective_date")), text(row, "created_by"),
						localDateTime(row.get("created_at")), text(row, "updated_by"),
						localDateTime(row.get("updated_at")), text(row, "reviewer_id"),
						localDateTime(row.get("reviewed_at"))))
				.toList();
		return new PolicyReversalPage(items, totalItems, query.page(), query.pageSize(), query.totalPages(totalItems));
	}

	@Transactional
	public PolicyReversalResult reverse(PolicyReversalRequest request, String requestId, String reviewerId) {
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
		String after = "{\"policyDeleted\":false,\"contractStatusCode\":null}";
		if (mapper.clearUnderwritingContractStatus(preview.underwritingCaseNo(), request.expectedUnderwritingVersion(),
				reviewerId) != 1)
			throw new BusinessException(NewContractErrorCode.CONCURRENT_MODIFICATION);
		mapper.insertReversalAudit(auditId, request.policyNo(), preview.applicationNo(), preview.underwritingCaseNo(),
				request.reasonCode(), request.reasonDescription(), requestId, before, after, hash(before), hash(after));
		return new PolicyReversalResult(auditId, request.policyNo(), preview.applicationNo(),
				preview.newContractStageCode(), preview.newContractStageNameEn(),
				preview.newContractStageDescriptionZhTw(), preview.underwritingStatus());
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
	/** 將固定案件階段碼轉為繁中說明；未知值視為資料契約錯誤。 */
	private static NewContractApplicationStatus stage(String stageCode) {
		return NewContractApplicationStatus.fromCode(stageCode);
	}
	/** 依新契約固定狀態回傳繁中說明，NULL 代表案件仍在受理流程。 */
	private static String contractStatusDescription(String contractStatusCode) {
		if (contractStatusCode == null)
			return "受理";
		return switch (contractStatusCode) {
			case "01" -> "有效";
			case "13" -> "拒保";
			case "14" -> "延期";
			case "15" -> "取消";
			case "26" -> "十天猶豫期變更";
			default -> throw new IllegalArgumentException("未知的新契約契約狀態: " + contractStatusCode);
		};
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

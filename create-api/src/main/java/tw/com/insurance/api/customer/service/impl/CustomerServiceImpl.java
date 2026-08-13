package tw.com.insurance.api.customer.service.impl;

import static tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerResult;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerPage;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerSummary;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.common.util.PageSortRequest;
import tw.com.insurance.api.customer.persistence.CustomerMapper;
import tw.com.insurance.api.customer.service.CustomerService;
import tw.com.insurance.api.customer.domain.CustomerErrorCode;
import tw.com.insurance.api.newcontract.codedefinition.service.CodeDefinitionService;

@Service
public class CustomerServiceImpl implements CustomerService {
	private final CustomerMapper mapper;
	private final CodeDefinitionService codeDefinitionService;
	private final byte[] key;
	public CustomerServiceImpl(CustomerMapper mapper, CodeDefinitionService codeDefinitionService,
			@Value("${app.pii-encryption-key}") String keyText) {
		this.mapper = mapper;
		this.codeDefinitionService = codeDefinitionService;
		if (keyText == null || keyText.length() < 24)
			throw new IllegalStateException("PII_ENCRYPTION_KEY 長度至少需要 24 字元");
		this.key = sha256(keyText);
	}
	@Transactional
	public CustomerResult create(CreateCustomerRequest request, String requestId, String reviewerId) {
		String identity = normalize(request.identityNo());
		String customerType = request.customerTypeCode() == null || request.customerTypeCode().isBlank()
				? "1"
				: request.customerTypeCode();
		if (!codeDefinitionService.isActiveCode("customer-master", "customer_type_code", customerType))
			throw new BusinessException(CustomerErrorCode.INVALID_CUSTOMER_TYPE);
		boolean organization = "2".equals(customerType);
		if (!organization && !"1".equals(customerType))
			throw new BusinessException(CustomerErrorCode.INVALID_CUSTOMER_TYPE);
		if (!organization
				&& (request.genderCode() == null || request.genderCode().isBlank() || request.birthDate() == null))
			throw new BusinessException(CustomerErrorCode.PERSON_REQUIRED_FIELDS);
		if (organization && (request.responsiblePersonName() == null || request.responsiblePersonName().isBlank()
				|| request.industryCode() == null || request.industryCode().isBlank()
				|| request.organizationTypeCode() == null || request.organizationTypeCode().isBlank()))
			throw new BusinessException(CustomerErrorCode.ORGANIZATION_REQUIRED_FIELDS);
		if (!organization && "NATIONAL_ID".equals(request.identityTypeCode()) && !validTaiwanId(identity))
			throw new BusinessException(CustomerErrorCode.INVALID_NATIONAL_ID);
		if (organization
				&& (!"BUSINESS_REGISTRATION_NO".equals(request.identityTypeCode()) || !identity.matches("[0-9]{8}")))
			throw new BusinessException(CustomerErrorCode.INVALID_BUSINESS_NO);
		validateKycCodes(request);
		String id = UUID.randomUUID().toString();
		String mobile = normalizeContact(request.mobilePhone()),
				email = request.email().trim().toLowerCase(Locale.ROOT);
		try {
			mapper.insertCustomer(id, customerType, request.customerName().trim(),
					organization ? null : request.genderCode(), organization ? null : request.birthDate(),
					request.nationalityCode().toUpperCase(Locale.ROOT),
					request.residencyCountryCode().toUpperCase(Locale.ROOT));
			if (organization)
				mapper.insertOrganizationProfile(id, request.establishmentDate(),
						request.responsiblePersonName().trim(), request.industryCode().trim(),
						request.organizationTypeCode());
			mapper.insertIdentity(UUID.randomUUID().toString(), id, request.identityTypeCode(),
					HexFormat.of().formatHex(sha256(identity)), encrypt(identity),
					identity.substring(identity.length() - 4), request.nationalityCode().toUpperCase(Locale.ROOT));
			mapper.insertContact(UUID.randomUUID().toString(), id, "MOBILE", encrypt(mobile),
					HexFormat.of().formatHex(sha256(mobile)), maskPhone(mobile));
			mapper.insertContact(UUID.randomUUID().toString(), id, "EMAIL", encrypt(email),
					HexFormat.of().formatHex(sha256(email)), maskEmail(email));
			mapper.insertAddress(UUID.randomUUID().toString(), id, request.postalCode(),
					encrypt(request.contactAddress().trim()), "地址已加密");
			mapper.insertNameHistory(UUID.randomUUID().toString(), id, request.customerName().trim());
			mapper.insertKyc(UUID.randomUUID().toString(), id, request.occupationCode(), request.sourceOfFundsCode(),
					request.insurancePurposeCode());
			mapper.insertConsent(UUID.randomUUID().toString(), id, request.consentVersion());
			mapper.insertAudit(UUID.randomUUID().toString(), id, requestId);
			mapper.markReviewed(id, reviewerId);
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(CustomerErrorCode.DUPLICATE_IDENTITY);
		}
		return new CustomerResult(id, customerType, request.identityTypeCode(), mask(identity),
				request.customerName().trim(), request.genderCode(), request.birthDate(), maskPhone(mobile),
				maskEmail(email), "ACTIVE", 0);
	}

	/** 分頁列出不含證件號碼及聯絡資料的客戶主檔摘要。 */
	@Override
	@Transactional(readOnly = true)
	public CustomerPage findPage(String queryText, int page, int pageSize, String sort) {
		PageSortRequest query = PageSortRequest.of(page, pageSize, sort,
				Set.of("customerId", "customerTypeCode", "customerName"), "customerId");
		String exactQuery = queryText == null ? "" : queryText.trim();
		long totalItems = mapper.countCustomers(exactQuery);
		List<CustomerSummary> items = mapper.findCustomerPage(exactQuery, query.offset(), query.pageSize(), query.sortField(),
				query.sortDirection()).stream()
				.map(row -> new CustomerSummary(text(row, "customer_id"), text(row, "customer_type_code"),
						text(row, "customer_name"), text(row, "nationality_code"), text(row, "record_status"),
						text(row, "created_by"), time(row, "created_at"), text(row, "updated_by"),
						time(row, "updated_at"), text(row, "reviewer_id"), time(row, "reviewed_at")))
				.toList();
		return new CustomerPage(items, totalItems, query.page(), query.pageSize(), query.totalPages(totalItems));
	}

	/** 將 MyBatis row 的空值安全轉為字串。 */
	private static String text(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : value.toString();
	}

	/** 將 MyBatis timestamp 轉為 API 使用的本地日期時間。 */
	private static java.time.LocalDateTime time(Map<String, Object> row, String key) {
		Object value = row.get(key);
		if (value == null)
			return null;
		return value instanceof java.sql.Timestamp timestamp ? timestamp.toLocalDateTime()
				: (java.time.LocalDateTime) value;
	}
	/** 驗證 KYC 動態代碼均來自新契約自己的有效代碼設定。 */
	private void validateKycCodes(CreateCustomerRequest request) {
		boolean validOccupation = codeDefinitionService.isActiveCode("customer-kyc", "occupation_code",
				request.occupationCode());
		boolean validFunds = codeDefinitionService.isActiveCode("customer-kyc", "source_of_funds_code",
				request.sourceOfFundsCode());
		boolean validPurpose = codeDefinitionService.isActiveCode("customer-kyc", "insurance_purpose_code",
				request.insurancePurposeCode());
		if (!validOccupation || !validFunds || !validPurpose)
			throw new BusinessException(CustomerErrorCode.INVALID_KYC_CODE);
		boolean validNationality = codeDefinitionService.isActiveCode("common", "country_code",
				request.nationalityCode());
		boolean validResidence = codeDefinitionService.isActiveCode("common", "country_code",
				request.residencyCountryCode());
		boolean validPostalCode = codeDefinitionService.isActiveCode("customer-contact", "postal_code3",
				request.postalCode());
		if (!validNationality || !validResidence || !validPostalCode)
			throw new BusinessException(CustomerErrorCode.INVALID_CONTACT_CODE);
	}
	private byte[] encrypt(String value) {
		try {
			byte[] iv = new byte[12];
			new SecureRandom().nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
			byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
			byte[] result = Arrays.copyOf(iv, iv.length + encrypted.length);
			System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
			return result;
		} catch (Exception e) {
			throw new IllegalStateException("個資加密失敗", e);
		}
	}
	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	private static String normalize(String value) {
		return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
	}
	private static String normalizeContact(String value) {
		return value.replaceAll("[^0-9+]", "");
	}
	private static String mask(String value) {
		return value.length() < 4 ? "****" : "******" + value.substring(value.length() - 4);
	}
	private static String maskPhone(String value) {
		return value.length() < 4 ? "****" : "******" + value.substring(value.length() - 4);
	}
	private static String maskEmail(String value) {
		int at = value.indexOf('@');
		return at <= 1 ? "***" : value.charAt(0) + "***" + value.substring(at);
	}
	private static boolean validTaiwanId(String value) {
		if (!value.matches("[A-Z][12][0-9]{8}"))
			return false;
		String letters = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
		int code = letters.indexOf(value.charAt(0)) + 10;
		if (code < 10)
			return false;
		int sum = code / 10 + (code % 10) * 9;
		for (int i = 1; i < 9; i++)
			sum += (value.charAt(i) - '0') * (9 - i);
		sum += value.charAt(9) - '0';
		return sum % 10 == 0;
	}
}

package tw.com.insurance.api.review.service.impl;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.RemittanceSlipRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequest;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDecisionRequest;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDetail;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewPageResult;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSubmissionResult;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import tw.com.insurance.api.customer.service.CustomerService;
import tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationRequest;
import tw.com.insurance.api.newcontract.service.NewContractService;
import tw.com.insurance.api.review.domain.ReviewErrorCode;
import tw.com.insurance.api.review.domain.ReviewDecisionPolicy;
import tw.com.insurance.api.review.domain.ReviewOperationType;
import tw.com.insurance.api.review.persistence.ReviewMapper;
import tw.com.insurance.api.review.service.ReviewService;

/** 以單一交易協調待審鎖、覆核決行、正式異動與成功稽核。 */
@Service
public class ReviewServiceImpl implements ReviewService {
	private final ReviewMapper mapper;
	private final CustomerService customerService;
	private final NewContractService newContractService;
	private final ObjectMapper objectMapper;
	private final byte[] encryptionKey;

	public ReviewServiceImpl(ReviewMapper mapper, CustomerService customerService,
			NewContractService newContractService, ObjectMapper objectMapper,
			@Value("${app.pii-encryption-key}") String keyText) {
		this.mapper = mapper;
		this.customerService = customerService;
		this.newContractService = newContractService;
		this.objectMapper = objectMapper;
		this.encryptionKey = sha256(keyText);
	}

	/** 建立加密覆核案件與唯一待審鎖，不直接異動正式業務資料。 */
	@Override
	@Transactional
	public ReviewSubmissionResult submit(ReviewOperationType operationType, String businessKey, Object payload,
			String makerId) {
		String reviewId = UUID.randomUUID().toString();
		try {
			mapper.insertPendingLock(operationType.functionCode(), businessKey, reviewId);
			mapper.insertCase(reviewId, operationType.name(), operationType.functionCode(), businessKey,
					encrypt(writeBytes(payload)), makerId);
			mapper.insertAudit(UUID.randomUUID().toString(), reviewId, "SUBMIT", makerId, reviewId, "SUCCESS");
		} catch (DuplicateKeyException exception) {
			throw new BusinessException(ReviewErrorCode.DUPLICATE_PENDING);
		}
		return new ReviewSubmissionResult(reviewId, operationType.name(), operationType.description(), businessKey,
				"PENDING", LocalDateTime.now());
	}

	/** 依覆核狀態分頁查詢待辦，排序固定為最早送審優先。 */
	@Override
	@Transactional(readOnly = true)
	public ReviewPageResult findPage(String status, int page, int pageSize) {
		int safePage = Math.max(page, 1);
		int safePageSize = Math.min(Math.max(pageSize, 1), 100);
		long total = mapper.countByStatus(status);
		List<ReviewSummary> items = mapper.findPage(status, (safePage - 1) * safePageSize, safePageSize).stream()
				.map(this::toSummary).toList();
		return new ReviewPageResult(items, total, safePage, safePageSize,
				(int) Math.ceil((double) total / safePageSize));
	}

	/** 取得覆核明細並只在授權後解密 payload。 */
	@Override
	@Transactional(readOnly = true)
	public ReviewDetail findById(String reviewId) {
		Map<String, Object> row = mapper.findById(reviewId);
		if (row == null) {
			throw new BusinessException(ReviewErrorCode.NOT_FOUND);
		}
		return toDetail(row);
	}

	/** 核准待審案件；正式異動與覆核狀態、稽核及解鎖同成同敗。 */
	@Override
	@Transactional
	public ReviewDetail approve(String reviewId, ReviewDecisionRequest request, String reviewerId, String requestId) {
		Map<String, Object> row = lockPending(reviewId, reviewerId);
		ReviewOperationType operationType = ReviewOperationType.valueOf(text(row, "operation_type"));
		JsonNode result = execute(operationType, decrypt((byte[]) row.get("payload_ciphertext")), requestId);
		if (mapper.approve(reviewId, reviewerId, request.comment(), writeJson(result),
				longNumber(row, "record_version")) != 1) {
			throw new BusinessException(ReviewErrorCode.ALREADY_DECIDED);
		}
		mapper.deletePendingLock(reviewId);
		mapper.insertAudit(UUID.randomUUID().toString(), reviewId, "APPROVE", reviewerId, requestId, "SUCCESS");
		return findById(reviewId);
	}

	/** 退回待審案件並釋放防重鎖，不觸碰正式業務資料。 */
	@Override
	@Transactional
	public ReviewDetail reject(String reviewId, ReviewDecisionRequest request, String reviewerId, String requestId) {
		Map<String, Object> row = lockPending(reviewId, reviewerId);
		if (mapper.reject(reviewId, reviewerId, request.comment(), longNumber(row, "record_version")) != 1) {
			throw new BusinessException(ReviewErrorCode.ALREADY_DECIDED);
		}
		mapper.deletePendingLock(reviewId);
		mapper.insertAudit(UUID.randomUUID().toString(), reviewId, "REJECT", reviewerId, requestId, "SUCCESS");
		return findById(reviewId);
	}

	/** 驗證案件仍待審且符合職務分離後回傳鎖定資料。 */
	private Map<String, Object> lockPending(String reviewId, String reviewerId) {
		Map<String, Object> row = mapper.findByIdForUpdate(reviewId);
		if (row == null) {
			throw new BusinessException(ReviewErrorCode.NOT_FOUND);
		}
		ReviewDecisionPolicy.validate(text(row, "review_status"), text(row, "maker_id"), reviewerId);
		return row;
	}

	/** 依固定 operation type 將核准案件派送至既有業務 use case。 */
	private JsonNode execute(ReviewOperationType operationType, byte[] payload, String requestId) {
		try {
			Object result = switch (operationType) {
				case CUSTOMER_CREATE ->
					customerService.create(objectMapper.readValue(payload, CreateCustomerRequest.class), requestId);
				case APPLICATION_CREATE -> newContractService
						.createApplication(objectMapper.readValue(payload, CreateApplicationRequest.class));
				case POLICY_NUMBER_RESERVE -> newContractService
						.reservePolicyNumber(objectMapper.readTree(payload).path("applicationNo").asText());
				case POLICY_REVERSAL ->
					newContractService.reverse(objectMapper.readValue(payload, PolicyReversalRequest.class), requestId);
				case UNDERWRITING_BATCH_ENQUEUE ->
					newContractService.enqueue(objectMapper.readValue(payload, UnderwritingBatchRequest.class));
				case INITIAL_PREMIUM_MATCH ->
					newContractService.matchPremium(objectMapper.readValue(payload, RemittanceSlipRequest.class));
			};
			return objectMapper.valueToTree(result);
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BusinessException(ReviewErrorCode.INVALID_PAYLOAD);
		}
	}

	/** 將 persistence row 轉為不含 payload 的清單摘要。 */
	private ReviewSummary toSummary(Map<String, Object> row) {
		ReviewOperationType type = ReviewOperationType.valueOf(text(row, "operation_type"));
		return new ReviewSummary(text(row, "review_id"), type.name(), type.description(), text(row, "business_key"),
				text(row, "review_status"), text(row, "maker_id"), localDateTime(row.get("submitted_at")),
				text(row, "reviewer_id"), localDateTime(row.get("reviewed_at")));
	}

	/** 將 persistence row 轉為覆核明細，payload 僅在此解密。 */
	private ReviewDetail toDetail(Map<String, Object> row) {
		ReviewOperationType type = ReviewOperationType.valueOf(text(row, "operation_type"));
		return new ReviewDetail(text(row, "review_id"), type.name(), type.description(), text(row, "business_key"),
				text(row, "review_status"), text(row, "maker_id"), localDateTime(row.get("submitted_at")),
				text(row, "reviewer_id"), text(row, "review_comment"), localDateTime(row.get("reviewed_at")),
				readTree(decrypt((byte[]) row.get("payload_ciphertext"))), readTreeNullable(row.get("result_content")));
	}

	private byte[] writeBytes(Object value) {
		try {
			return objectMapper.writeValueAsBytes(value);
		} catch (Exception exception) {
			throw new BusinessException(ReviewErrorCode.INVALID_PAYLOAD);
		}
	}
	private String writeJson(JsonNode value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception exception) {
			throw new BusinessException(ReviewErrorCode.INVALID_PAYLOAD);
		}
	}
	private JsonNode readTree(byte[] value) {
		try {
			return objectMapper.readTree(value);
		} catch (Exception exception) {
			throw new BusinessException(ReviewErrorCode.INVALID_PAYLOAD);
		}
	}
	private JsonNode readTreeNullable(Object value) {
		if (value == null)
			return null;
		try {
			return objectMapper.readTree(String.valueOf(value));
		} catch (Exception exception) {
			return objectMapper.valueToTree(value);
		}
	}
	private byte[] encrypt(byte[] value) {
		try {
			byte[] iv = new byte[12];
			new SecureRandom().nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"), new GCMParameterSpec(128, iv));
			byte[] encrypted = cipher.doFinal(value);
			byte[] result = Arrays.copyOf(iv, iv.length + encrypted.length);
			System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
			return result;
		} catch (Exception exception) {
			throw new IllegalStateException("覆核資料加密失敗", exception);
		}
	}
	private byte[] decrypt(byte[] value) {
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, "AES"),
					new GCMParameterSpec(128, Arrays.copyOfRange(value, 0, 12)));
			return cipher.doFinal(Arrays.copyOfRange(value, 12, value.length));
		} catch (Exception exception) {
			throw new IllegalStateException("覆核資料解密失敗", exception);
		}
	}
	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}
	private static String text(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : String.valueOf(value);
	}
	private static long longNumber(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).longValue();
	}
	private static LocalDateTime localDateTime(Object value) {
		if (value == null) {
			return null;
		}
		return value instanceof LocalDateTime localDateTime
				? localDateTime
				: ((java.sql.Timestamp) value).toLocalDateTime();
	}
}

package tw.com.insurance.api.review.controller;

import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDecisionRequest;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDetail;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewPageResult;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewOperationOption;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.review.service.ReviewService;

/** 提供覆核待辦、明細、核准與退回 API。 */
@Validated
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
	private final ReviewService service;
	public ReviewController(ReviewService service) {
		this.service = service;
	}

	/** 查詢指定狀態的覆核案件。 */
	@GetMapping
	public ResponseBodyDto<ReviewPageResult> findPage(@RequestParam(defaultValue = "P") String status,
			@RequestParam(defaultValue = "1") @Min(1) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
			@RequestParam(defaultValue = "reviewId,asc") String sort,
			@RequestParam(defaultValue = "") @Size(max = 200) String query,
			@RequestParam(defaultValue = "") @Size(max = 64) String operationType) {
		return ResponseBodyDto.success("覆核待辦查詢成功",
				service.findPage(status, page, pageSize, sort, query, operationType));
	}

	/** 取得覆核功能的英文代碼與繁中顯示名稱資料字典。 */
	@GetMapping("/operation-types")
	public ResponseBodyDto<List<ReviewOperationOption>> findOperationOptions() {
		return ResponseBodyDto.success("覆核功能對照查詢成功", service.findOperationOptions());
	}

	/** 取得單筆覆核內容。 */
	@GetMapping("/{reviewId}")
	public ResponseBodyDto<ReviewDetail> findById(@PathVariable @NotBlank String reviewId) {
		return ResponseBodyDto.success("覆核明細查詢成功", service.findById(reviewId));
	}

	/** 核准並套用正式業務異動。 */
	@PostMapping("/{reviewId}/approve")
	public ResponseBodyDto<ReviewDetail> approve(@PathVariable @NotBlank String reviewId,
			@Valid @RequestBody ReviewDecisionRequest request, @AuthenticationPrincipal Jwt jwt,
			@RequestHeader(value = "X-Request-ID", required = false) String requestId) {
		return ResponseBodyDto.success("覆核核准完成",
				service.approve(reviewId, request, jwt.getSubject(), effectiveRequestId(requestId)));
	}

	/** 退回案件且不異動正式業務資料。 */
	@PostMapping("/{reviewId}/reject")
	public ResponseBodyDto<ReviewDetail> reject(@PathVariable @NotBlank String reviewId,
			@Valid @RequestBody ReviewDecisionRequest request, @AuthenticationPrincipal Jwt jwt,
			@RequestHeader(value = "X-Request-ID", required = false) String requestId) {
		return ResponseBodyDto.success("覆核退回完成",
				service.reject(reviewId, request, jwt.getSubject(), effectiveRequestId(requestId)));
	}

	/** 確保每次決行都有可追蹤 request id。 */
	private static String effectiveRequestId(String requestId) {
		return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
	}
}

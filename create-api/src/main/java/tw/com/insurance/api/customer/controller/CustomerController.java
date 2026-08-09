package tw.com.insurance.api.customer.controller;

import static tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSubmissionResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.review.domain.ReviewOperationType;
import tw.com.insurance.api.review.service.ReviewService;
import tw.com.insurance.api.review.util.ReviewBusinessKey;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
	private final ReviewService service;
	public CustomerController(ReviewService service) {
		this.service = service;
	}
	/** 客戶資料只建立覆核案件，核准前不寫入客戶主檔。 */
	@PostMapping
	ResponseBodyDto<ReviewSubmissionResult> create(@Valid @RequestBody CreateCustomerRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String businessKey = ReviewBusinessKey.sensitive(request.identityTypeCode(), request.identityNo());
		return ResponseBodyDto.success("客戶建立已送覆核",
				service.submit(ReviewOperationType.CUSTOMER_CREATE, businessKey, request, jwt.getSubject()));
	}
}

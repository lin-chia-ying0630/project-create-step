package tw.com.insurance.api.customer.controller;

import static tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerPage;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSubmissionResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.review.domain.ReviewOperationType;
import tw.com.insurance.api.review.service.ReviewService;
import tw.com.insurance.api.customer.service.CustomerService;
import tw.com.insurance.api.review.util.ReviewBusinessKey;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
	private final ReviewService service;
	private final CustomerService customerService;
	public CustomerController(ReviewService service, CustomerService customerService) {
		this.service = service;
		this.customerService = customerService;
	}
	/** 客戶資料只建立覆核案件，核准前不寫入客戶主檔。 */
	@PostMapping
	ResponseBodyDto<ReviewSubmissionResult> create(@Valid @RequestBody CreateCustomerRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String businessKey = ReviewBusinessKey.sensitive(request.identityTypeCode(), request.identityNo());
		return ResponseBodyDto.success("客戶建立已送覆核",
				service.submit(ReviewOperationType.CUSTOMER_CREATE, businessKey, request, jwt.getSubject()));
	}
	/** 初次進入客戶建立畫面即列出十筆客戶摘要。 */
	@GetMapping
	ResponseBodyDto<CustomerPage> findPage(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "customerId,asc") String sort) {
		return ResponseBodyDto.success("客戶清單查詢成功", customerService.findPage(query, page, pageSize, sort));
	}
}

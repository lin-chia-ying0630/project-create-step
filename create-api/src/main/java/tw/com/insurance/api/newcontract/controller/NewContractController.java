package tw.com.insurance.api.newcontract.controller;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDuePreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.RemittanceSlipRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchExecutionSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingDecisionRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingOutcomeOption;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSubmissionResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.newcontract.service.NewContractService;
import tw.com.insurance.api.review.domain.ReviewOperationType;
import tw.com.insurance.api.review.service.ReviewService;

@Validated
@RestController
@RequestMapping("/api/v1/new-contract")
public class NewContractController {
	private final NewContractService service;
	private final ReviewService reviewService;
	public NewContractController(NewContractService service, ReviewService reviewService) {
		this.service = service;
		this.reviewService = reviewService;
	}

	/** 保單登打只送覆核，核准後才建立要保案件與首期應繳。 */
	@PostMapping("/applications")
	ResponseBodyDto<ReviewSubmissionResult> create(@Valid @RequestBody CreateApplicationRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseBodyDto.success("保單登打已送覆核", reviewService.submit(ReviewOperationType.APPLICATION_CREATE,
				request.applicationNo(), request, jwt.getSubject()));
	}

	/** 完整帳號或卡號只供即時格式驗證，回應只含 Token 與遮罩值。 */
	@PostMapping("/payment-instruments/validate")
	ResponseBodyDto<PaymentInstrumentValidationResult> validatePaymentInstrument(
			@Valid @RequestBody PaymentInstrumentValidationRequest request) {
		return ResponseBodyDto.success("付款資料格式驗證成功", service.validatePaymentInstrument(request));
	}

	/** 保單號碼編發屬資料異動，需經保單登打類別覆核。 */
	@PostMapping("/applications/{applicationNo}/policy-number")
	ResponseBodyDto<ReviewSubmissionResult> reservePolicyNumber(@PathVariable @NotBlank String applicationNo,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseBodyDto.success("保單號碼編發已送覆核", reviewService.submit(ReviewOperationType.POLICY_NUMBER_RESERVE,
				applicationNo, Map.of("applicationNo", applicationNo), jwt.getSubject()));
	}

	@GetMapping("/applications/query/{query}")
	ResponseBodyDto<List<ApplicationQueryResult>> queryApplication(@PathVariable @NotBlank String query) {
		return ResponseBodyDto.success("保單資料查詢成功", service.queryApplication(query));
	}

	/** 初次進入即以十筆分頁列出保單，並支援完整識別值查詢與白名單排序。 */
	@GetMapping("/applications/query")
	ResponseBodyDto<ApplicationQueryPage> queryApplications(@RequestParam(defaultValue = "") String query,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "applicationNo,asc") String sort) {
		return ResponseBodyDto.success("保單資料清單查詢成功", service.queryApplications(query, page, pageSize, sort));
	}

	@GetMapping("/applications/{applicationNo}/initial-premium")
	ResponseBodyDto<PremiumDuePreview> getPremiumDue(@PathVariable @NotBlank String applicationNo) {
		return ResponseBodyDto.success("查詢成功", service.getPremiumDue(applicationNo));
	}
	/** 新增送金單並送交覆核；核准後才寫入送金單及執行首期保費銷帳。 */
	@PostMapping({"/remittance-slips", "/initial-premium-payments/reconcile", "/remittance-slips/match"})
	ResponseBodyDto<ReviewSubmissionResult> match(@Valid @RequestBody RemittanceSlipRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseBodyDto.success("新增送金單已送覆核", reviewService.submit(ReviewOperationType.INITIAL_PREMIUM_MATCH,
				request.applicationNo(), request, jwt.getSubject()));
	}
	/** 指定執行日送覆核；核准後才將保單寫入該日的新契約批次承保作業排程。 */
	@PostMapping("/underwriting-batch/requests")
	ResponseBodyDto<ReviewSubmissionResult> enqueue(@Valid @RequestBody UnderwritingBatchRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		String businessKey = request.applicationNo() + ":" + request.executionDate();
		return ResponseBodyDto.success("新契約批次承保作業已送覆核", reviewService
				.submit(ReviewOperationType.UNDERWRITING_BATCH_ENQUEUE, businessKey, request, jwt.getSubject()));
	}
	@GetMapping("/underwriting-batch/executions")
	ResponseBodyDto<List<UnderwritingBatchExecutionSummary>> executions() {
		return ResponseBodyDto.success("查詢成功", service.latestExecutions());
	}
	/** 列出新契約受理檔中 NS 照會結束、等待核保審查的案件。 */
	@GetMapping("/underwriting-reviews")
	ResponseBodyDto<UnderwritingReviewPage> findUnderwritingReviewCandidates(
			@RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "applicationNo,asc") String sort) {
		return ResponseBodyDto.success("待核保審查清單查詢成功",
				service.findUnderwritingReviewCandidates(query, page, pageSize, sort));
	}
	/** 查詢人工核保審查目前結果與版本，不異動正式資料。 */
	@GetMapping("/underwriting-reviews/{query}")
	ResponseBodyDto<UnderwritingReviewPreview> previewUnderwritingReview(@PathVariable @NotBlank String query) {
		return ResponseBodyDto.success("核保審查案件查詢成功", service.previewUnderwritingReview(query));
	}
	/** 回傳完整核保結果選項；可承保與不承保結果使用同一份後端正式對照。 */
	@GetMapping("/underwriting-reviews/outcomes")
	ResponseBodyDto<List<UnderwritingOutcomeOption>> findUnderwritingOutcomes() {
		return ResponseBodyDto.success("核保結果選項查詢成功", service.findUnderwritingOutcomes());
	}
	/** 修改核保結果一律建立覆核案件，核准後才同步階段碼與契約狀態。 */
	@PostMapping("/underwriting-reviews/decisions")
	ResponseBodyDto<ReviewSubmissionResult> decideUnderwriting(@Valid @RequestBody UnderwritingDecisionRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseBodyDto.success("核保結果修改已送覆核", reviewService.submit(ReviewOperationType.UNDERWRITING_DECISION,
				request.applicationNo(), request, jwt.getSubject()));
	}
	@GetMapping("/policy-reversals/{policyNo}/preview")
	ResponseBodyDto<PolicyReversalPreview> preview(@PathVariable @NotBlank String policyNo) {
		return ResponseBodyDto.success("查詢成功", service.previewReversal(policyNo));
	}
	/** 初次進入承保撤回即列出契約狀態 01 的候選保單。 */
	@GetMapping("/policy-reversals")
	ResponseBodyDto<PolicyReversalPage> findReversiblePolicies(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "policyNo,asc") String sort) {
		return ResponseBodyDto.success("可承保撤回保單查詢成功", service.findReversiblePolicies(page, pageSize, sort));
	}
	@PostMapping("/policy-reversals")
	ResponseBodyDto<ReviewSubmissionResult> reverse(@Valid @RequestBody PolicyReversalRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return ResponseBodyDto.success("承保撤回已送覆核", reviewService.submit(ReviewOperationType.POLICY_REVERSAL,
				request.policyNo(), request, jwt.getSubject()));
	}
}

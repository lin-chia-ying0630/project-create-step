package tw.com.insurance.api.newcontract.controller;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyNumberReservationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumDuePreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PremiumMatchResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.RemittanceSlipRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchExecutionSummary;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingBatchRequestResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.newcontract.service.NewContractService;

@Validated
@RestController
@RequestMapping("/api/v1/new-contract")
public class NewContractController {
	private final NewContractService service;
	public NewContractController(NewContractService service) {
		this.service = service;
	}

	@PostMapping("/applications")
	ResponseBodyDto<CreateApplicationResult> create(@Valid @RequestBody CreateApplicationRequest request) {
		return ResponseBodyDto.success("要保案件建立成功", service.createApplication(request));
	}

	@PostMapping("/applications/{applicationNo}/policy-number")
	ResponseBodyDto<PolicyNumberReservationResult> reservePolicyNumber(@PathVariable @NotBlank String applicationNo) {
		return ResponseBodyDto.success("保單號碼編發成功", service.reservePolicyNumber(applicationNo));
	}

	@GetMapping("/applications/query/{query}")
	ResponseBodyDto<List<ApplicationQueryResult>> queryApplication(@PathVariable @NotBlank String query) {
		return ResponseBodyDto.success("保單資料查詢成功", service.queryApplication(query));
	}

	@GetMapping("/applications/{applicationNo}/initial-premium")
	ResponseBodyDto<PremiumDuePreview> getPremiumDue(@PathVariable @NotBlank String applicationNo) {
		return ResponseBodyDto.success("查詢成功", service.getPremiumDue(applicationNo));
	}
	@PostMapping({"/initial-premium-payments/reconcile", "/remittance-slips/match"})
	ResponseBodyDto<PremiumMatchResult> match(@Valid @RequestBody RemittanceSlipRequest request) {
		return ResponseBodyDto.success("首期保險費收款與銷帳完成", service.matchPremium(request));
	}
	@PostMapping("/underwriting-batch/requests")
	ResponseBodyDto<UnderwritingBatchRequestResult> enqueue(@Valid @RequestBody UnderwritingBatchRequest request) {
		return ResponseBodyDto.success("已排入核保批次", service.enqueue(request));
	}
	@GetMapping("/underwriting-batch/executions")
	ResponseBodyDto<List<UnderwritingBatchExecutionSummary>> executions() {
		return ResponseBodyDto.success("查詢成功", service.latestExecutions());
	}
	@GetMapping("/policy-reversals/{policyNo}/preview")
	ResponseBodyDto<PolicyReversalPreview> preview(@PathVariable @NotBlank String policyNo) {
		return ResponseBodyDto.success("查詢成功", service.previewReversal(policyNo));
	}
	@PostMapping("/policy-reversals")
	ResponseBodyDto<PolicyReversalResult> reverse(@Valid @RequestBody PolicyReversalRequest request,
			@RequestHeader(value = "Idempotency-Key", required = false) String requestId) {
		String effectiveRequestId = requestId == null || requestId.isBlank()
				? java.util.UUID.randomUUID().toString()
				: requestId;
		return ResponseBodyDto.success("承保撤回完成", service.reverse(request, effectiveRequestId));
	}
}

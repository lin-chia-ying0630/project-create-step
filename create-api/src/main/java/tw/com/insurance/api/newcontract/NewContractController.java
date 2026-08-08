package tw.com.insurance.api.newcontract;

import static tw.com.insurance.api.newcontract.NewContractDtos.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tw.com.insurance.api.common.ResponseBodyDto;

@Validated
@RestController
@RequestMapping("/api/v1/new-contract")
public class NewContractController {
    private final NewContractService service;
    public NewContractController(NewContractService service){this.service=service;}

    @PostMapping("/applications")
    ResponseBodyDto<CreateApplicationResult> create(@Valid @RequestBody CreateApplicationRequest request){
        return ResponseBodyDto.success("要保案件建立成功",service.createApplication(request));
    }

    @GetMapping("/applications/{applicationNo}/initial-premium")
    ResponseBodyDto<PremiumDuePreview> getPremiumDue(@PathVariable @NotBlank String applicationNo){
        return ResponseBodyDto.success("查詢成功",service.getPremiumDue(applicationNo));
    }
    @PostMapping("/remittance-slips/match")
    ResponseBodyDto<PremiumMatchResult> match(@Valid @RequestBody RemittanceSlipRequest request){
        return ResponseBodyDto.success("繳費配對完成",service.matchPremium(request));
    }
    @PostMapping("/underwriting-batch/requests")
    ResponseBodyDto<UnderwritingBatchRequestResult> enqueue(@Valid @RequestBody UnderwritingBatchRequest request){
        return ResponseBodyDto.success("已排入核保批次",service.enqueue(request));
    }
    @GetMapping("/underwriting-batch/executions")
    ResponseBodyDto<List<UnderwritingBatchExecutionSummary>> executions(){
        return ResponseBodyDto.success("查詢成功",service.latestExecutions());
    }
    @GetMapping("/policy-reversals/{policyNo}/preview")
    ResponseBodyDto<PolicyReversalPreview> preview(@PathVariable @NotBlank String policyNo){
        return ResponseBodyDto.success("查詢成功",service.previewReversal(policyNo));
    }
    @PostMapping("/policy-reversals")
    ResponseBodyDto<PolicyReversalResult> reverse(@Valid @RequestBody PolicyReversalRequest request,
        @RequestHeader(value="Idempotency-Key",required=false) String requestId){
        String effectiveRequestId=requestId==null||requestId.isBlank()?java.util.UUID.randomUUID().toString():requestId;
        return ResponseBodyDto.success("承保撤回完成",service.reverse(request,effectiveRequestId));
    }
}

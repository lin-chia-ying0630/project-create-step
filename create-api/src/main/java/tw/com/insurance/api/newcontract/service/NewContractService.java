package tw.com.insurance.api.newcontract.service;

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
import java.util.List;

public interface NewContractService {
	CreateApplicationResult createApplication(CreateApplicationRequest request);
	PolicyNumberReservationResult reservePolicyNumber(String applicationNo);
	List<ApplicationQueryResult> queryApplication(String query);
	PremiumDuePreview getPremiumDue(String applicationNo);
	PremiumMatchResult matchPremium(RemittanceSlipRequest request);
	UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request);
	List<UnderwritingBatchExecutionSummary> latestExecutions();
	PolicyReversalPreview previewReversal(String policyNo);
	PolicyReversalResult reverse(PolicyReversalRequest request, String requestId);
}

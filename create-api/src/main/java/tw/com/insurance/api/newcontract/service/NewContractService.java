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
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingDecisionRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingDecisionResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPreview;
import java.util.List;

public interface NewContractService {
	CreateApplicationResult createApplication(CreateApplicationRequest request);
	PolicyNumberReservationResult reservePolicyNumber(String applicationNo);
	List<ApplicationQueryResult> queryApplication(String query);
	PremiumDuePreview getPremiumDue(String applicationNo);
	/** 覆核核准後建立送金單，比對應收與實收並以同一交易保存銷帳結果。 */
	PremiumMatchResult matchPremium(RemittanceSlipRequest request);
	/** 將指定保單與執行日建立為待執行核保排程，不在 API request 內直接執行核保。 */
	UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request);
	List<UnderwritingBatchExecutionSummary> latestExecutions();
	UnderwritingReviewPreview previewUnderwritingReview(String query);
	/** 覆核核准後以樂觀鎖同步核保案件、要保案件及 append-only 決行稽核。 */
	UnderwritingDecisionResult decideUnderwriting(UnderwritingDecisionRequest request, String operatorId);
	PolicyReversalPreview previewReversal(String policyNo);
	PolicyReversalResult reverse(PolicyReversalRequest request, String requestId);
}

package tw.com.insurance.api.newcontract.service;

import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.ApplicationQueryPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.CreateApplicationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationRequest;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyNumberReservationResult;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPreview;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.PolicyReversalPage;
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
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingReviewPage;
import static tw.com.insurance.api.newcontract.dto.NewContractDtos.UnderwritingOutcomeOption;
import java.util.List;

public interface NewContractService {
	/** 驗證付款工具後只回傳代碼與遮罩值，完整號碼不進入要保書覆核資料。 */
	PaymentInstrumentValidationResult validatePaymentInstrument(PaymentInstrumentValidationRequest request);
	CreateApplicationResult createApplication(CreateApplicationRequest request);
	PolicyNumberReservationResult reservePolicyNumber(String applicationNo);
	List<ApplicationQueryResult> queryApplication(String query);
	ApplicationQueryPage queryApplications(String query, int page, int pageSize, String sort);
	PremiumDuePreview getPremiumDue(String applicationNo);
	/** 覆核核准後建立送金單，比對應收與實收並以同一交易保存銷帳結果。 */
	PremiumMatchResult matchPremium(RemittanceSlipRequest request);
	/** 將指定保單與執行日建立為待執行核保排程，不在 API request 內直接執行核保。 */
	UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request);
	List<UnderwritingBatchExecutionSummary> latestExecutions();
	/** 分頁列出新契約受理檔中階段為 NS、需要人工核保審查的案件。 */
	UnderwritingReviewPage findUnderwritingReviewCandidates(String query, int page, int pageSize, String sort);
	UnderwritingReviewPreview previewUnderwritingReview(String query);
	/** 取得核保審查完整結果選項及其後續狀態映射。 */
	List<UnderwritingOutcomeOption> findUnderwritingOutcomes();
	/** 覆核核准後以樂觀鎖同步核保案件、要保案件及 append-only 決行稽核。 */
	UnderwritingDecisionResult decideUnderwriting(UnderwritingDecisionRequest request, String operatorId);
	PolicyReversalPreview previewReversal(String policyNo);
	PolicyReversalPage findReversiblePolicies(int page, int pageSize, String sort);
	PolicyReversalResult reverse(PolicyReversalRequest request, String requestId, String reviewerId);
}

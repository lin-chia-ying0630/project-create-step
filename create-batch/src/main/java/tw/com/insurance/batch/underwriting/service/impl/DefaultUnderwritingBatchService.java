package tw.com.insurance.batch.underwriting.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.batch.underwriting.domain.ApplicationCandidate;
import tw.com.insurance.batch.underwriting.domain.UnderwritingStage;
import tw.com.insurance.batch.underwriting.domain.ValidationIssue;
import tw.com.insurance.batch.underwriting.persistence.UnderwritingBatchMapper;
import tw.com.insurance.batch.underwriting.service.UnderwritingBatchService;
import tw.com.insurance.batch.underwriting.validation.BasicPolicyValidator;

@Service
public class DefaultUnderwritingBatchService implements UnderwritingBatchService {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultUnderwritingBatchService.class);
	private final UnderwritingBatchMapper mapper;
	private final BasicPolicyValidator validator = new BasicPolicyValidator();

	/** 以持久層 gateway 組成無共享可變狀態的批次服務。 */
	public DefaultUnderwritingBatchService(UnderwritingBatchMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * 依執行日領取待執行保單，完成基本資料與首期保費核保檢核並保存結果。
	 *
	 * @param businessDate
	 *            排程啟動時的臺北執行日
	 * @param triggerType
	 *            固定排程或人工觸發代碼
	 */
	@Override
	@Transactional
	public void execute(LocalDate businessDate, String triggerType) {
		String executionId = UUID.randomUUID().toString();
		mapper.insertExecution(executionId, businessDate, triggerType);
		mapper.claimPendingRequests(executionId, businessDate);
		List<Map<String, Object>> candidates = mapper.findClaimedCandidates(executionId);
		int approvedCount = 0;
		int inquiryCount = 0;
		for (Map<String, Object> row : candidates) {
			if (processCandidate(row, executionId))
				approvedCount++;
			else
				inquiryCount++;
		}
		mapper.completeExecution(executionId, candidates.size(), approvedCount, inquiryCount, 0,
				inquiryCount == 0 ? "S" : "R");
		LOG.info("核保批次完成 executionId={}, executionDate={}, totalCount={}", executionId, businessDate,
				candidates.size());
	}

	/** 執行單一保單核保檢核；通過為承保，未通過則轉照會。 */
	private boolean processCandidate(Map<String, Object> row, String executionId) {
		ApplicationCandidate candidate = toCandidate(row);
		List<ValidationIssue> issues = validator.validate(candidate);
		if (!"PW".equals(text(row, "application_status")))
			issues = append(issues, new ValidationIssue("APPLICATION_NOT_READY_FOR_ISSUANCE", "要保案件不是待發單狀態"));
		if (!"MATCHED".equals(text(row, "initial_premium_match_status")))
			issues = append(issues, new ValidationIssue("INITIAL_PREMIUM_NOT_MATCHED", "首期保險費尚未完成銷帳"));
		if (text(row, "policy_no") == null)
			issues = append(issues, new ValidationIssue("POLICY_NUMBER_MISSING", "案件未取得固定保單號碼"));

		boolean approved = issues.isEmpty();
		String caseNo = text(row, "underwriting_case_no");
		if (caseNo == null)
			caseNo = "UW-" + UUID.randomUUID().toString().substring(0, 20).toUpperCase();
		String requestStatus = approved ? "S" : "R";
		String resultCode = approved ? "SA" : issues.get(0).ruleCode();
		String policyNo = text(row, "policy_no");
		mapper.upsertUnderwritingCase(caseNo, candidate.applicationNo(),
				approved ? UnderwritingStage.COMPLETED.code() : UnderwritingStage.REFERRED.code(),
				approved ? "SA" : null, approved ? null : resultCode, approved ? policyNo : null);
		if (approved) {
			mapper.insertPolicyContract(UUID.randomUUID().toString(), candidate.applicationNo(), caseNo, policyNo);
			mapper.insertPolicyParties(policyNo, candidate.applicationNo());
			mapper.insertPolicyCoverages(policyNo, candidate.applicationNo());
			mapper.insertPolicyBeneficiaries(policyNo, candidate.applicationNo());
		}
		mapper.updateApplicationValidation(candidate.applicationNo(), approved ? "PASS" : "FAIL", executionId,
				approved ? "PS" : "PR");
		mapper.completeRequest(text(row, "batch_request_id"), requestStatus, resultCode);
		mapper.insertAudit(UUID.randomUUID().toString(), executionId, candidate.applicationNo(), caseNo, resultCode);
		return approved;
	}

	/** 將查詢快照轉成不依賴 MyBatis 的核保候選資料。 */
	private ApplicationCandidate toCandidate(Map<String, Object> row) {
		return new ApplicationCandidate(text(row, "application_no"), text(row, "applicant_customer_id"),
				text(row, "insured_customer_id"), text(row, "product_code"), text(row, "product_version"),
				text(row, "currency_code"), decimal(row, "sum_assured_amount"), decimal(row, "premium_amount"),
				localDate(row.get("application_date")), localDate(row.get("requested_effective_date")));
	}

	/** 保留既有不可變錯誤清單並追加一筆排程資格問題。 */
	private List<ValidationIssue> append(List<ValidationIssue> issues, ValidationIssue issue) {
		var result = new java.util.ArrayList<>(issues);
		result.add(issue);
		return List.copyOf(result);
	}

	/** 讀取查詢列文字欄位。 */
	private String text(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : String.valueOf(value);
	}

	/** 讀取查詢列金額欄位。 */
	private BigDecimal decimal(Map<String, Object> row, String key) {
		return (BigDecimal) row.get(key);
	}

	/** 將 JDBC 日期轉成核保領域日期。 */
	private LocalDate localDate(Object value) {
		return value instanceof LocalDate date ? date : ((java.sql.Date) value).toLocalDate();
	}
}

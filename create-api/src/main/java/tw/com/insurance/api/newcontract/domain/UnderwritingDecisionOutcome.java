package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 人工核保結果及其新契約階段、契約狀態的唯一固定對照。 */
public enum UnderwritingDecisionOutcome {
	STANDARD_ACCEPTED("SA", "標準承保", NewContractApplicationStatus.UNDERWRITING_COMPLETED, "01", "有效", true),
	RATED_ACCEPTED("RA", "加費承保", NewContractApplicationStatus.UNDERWRITING_COMPLETED, "01", "有效", true),
	EXCLUSION_ACCEPTED("EA", "除外承保", NewContractApplicationStatus.UNDERWRITING_COMPLETED, "01", "有效", true),
	CONDITIONAL_ACCEPTED("CA", "條件承保", NewContractApplicationStatus.UNDERWRITING_COMPLETED, "01", "有效", true),
	PARTIALLY_ACCEPTED("PA", "部分承保", NewContractApplicationStatus.UNDERWRITING_COMPLETED, "01", "有效", true),
	DECLINED("DC", "拒絕承保", NewContractApplicationStatus.UNDERWRITING_DECLINED, "13", "拒保", false),
	POSTPONED("PO", "延期承保", NewContractApplicationStatus.UNDERWRITING_POSTPONED, "14", "延期", false),
	CANCELLED("CN", "取消申請", NewContractApplicationStatus.UNDERWRITING_CANCELLED, "15", "取消", false);

	private final String decisionCode;
	private final String decisionDescription;
	private final NewContractApplicationStatus newContractStage;
	private final String contractStatusCode;
	private final String contractStatusDescription;
	private final boolean insurable;

	UnderwritingDecisionOutcome(String decisionCode, String decisionDescription,
			NewContractApplicationStatus newContractStage, String contractStatusCode,
			String contractStatusDescription, boolean insurable) {
		this.decisionCode = decisionCode;
		this.decisionDescription = decisionDescription;
		this.newContractStage = newContractStage;
		this.contractStatusCode = contractStatusCode;
		this.contractStatusDescription = contractStatusDescription;
		this.insurable = insurable;
	}

	/** 以畫面傳入的核保結果代碼取得固定對照；未知代碼由業務例外處理。 */
	public static UnderwritingDecisionOutcome fromDecisionCode(String code) {
		return Arrays.stream(values()).filter(value -> value.decisionCode.equals(code)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("未知的核保結果代碼: " + code));
	}

	public String decisionCode() {
		return decisionCode;
	}
	public String decisionDescription() {
		return decisionDescription;
	}
	public String newContractStageCode() {
		return newContractStage.newContractStageCode();
	}
	public String newContractStageNameEn() {
		return newContractStage.newContractStageNameEn();
	}
	public String newContractStageDescriptionZhTw() {
		return newContractStage.newContractStageDescriptionZhTw();
	}
	public String contractStatusCode() {
		return contractStatusCode;
	}
	public String contractStatusDescription() {
		return contractStatusDescription;
	}
	public boolean insurable() {
		return insurable;
	}
}

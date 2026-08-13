package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 人工核保審查可決行的負向核保結果，以及其核保階段與契約狀態唯一對照。 */
public enum UnderwritingDecisionOutcome {
	STANDARD_ACCEPTED("SA", "標準承保", "AS", "承保完成", "01", "有效", true), RATED_ACCEPTED("RA", "加費承保", "AS", "承保完成", "01",
			"有效", true), EXCLUSION_ACCEPTED("EA", "除外承保", "AS", "承保完成", "01", "有效", true), CONDITIONAL_ACCEPTED("CA",
					"條件承保", "AS", "承保完成", "01", "有效", true), PARTIALLY_ACCEPTED("PA", "部分承保", "AS", "承保完成", "01", "有效",
							true), DECLINED("DC", "拒絕承保", "RS", "拒保完成", "13", "拒保", false), POSTPONED("PO", "延期承保",
									"DS", "延期完成", "14", "延期",
									false), CANCELLED("CN", "取消申請", "CS", "取消完成", "15", "取消", false);

	private final String decisionCode;
	private final String decisionDescription;
	private final String stageCode;
	private final String stageDescription;
	private final String contractStatusCode;
	private final String contractStatusDescription;
	private final boolean insurable;

	UnderwritingDecisionOutcome(String decisionCode, String decisionDescription, String stageCode,
			String stageDescription, String contractStatusCode, String contractStatusDescription, boolean insurable) {
		this.decisionCode = decisionCode;
		this.decisionDescription = decisionDescription;
		this.stageCode = stageCode;
		this.stageDescription = stageDescription;
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
	public String stageCode() {
		return stageCode;
	}
	public String stageDescription() {
		return stageDescription;
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

package tw.com.insurance.api.review.domain;

/** 覆核支援的固定業務異動種類。 */
public enum ReviewOperationType {
	CUSTOMER_CREATE("CUS_CREATE", "客戶建立"), APPLICATION_CREATE("APP_CREATE", "保單登打"), POLICY_NUMBER_RESERVE(
			"APP_POLICY_NO", "保單號碼編發"), POLICY_REVERSAL("POL_REVERSAL", "承保撤回"), UNDERWRITING_BATCH_ENQUEUE(
					"UW_BATCH", "新契約批次承保作業"), UNDERWRITING_DECISION("UW_DECISION",
							"核保審查結果"), INITIAL_PREMIUM_MATCH("PRM_MATCH", "首期保費資料");

	private final String functionCode;
	private final String description;
	ReviewOperationType(String functionCode, String description) {
		this.functionCode = functionCode;
		this.description = description;
	}
	public String functionCode() {
		return functionCode;
	}
	public String description() {
		return description;
	}
}

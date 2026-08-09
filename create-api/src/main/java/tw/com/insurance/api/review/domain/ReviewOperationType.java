package tw.com.insurance.api.review.domain;

/** 覆核支援的固定業務異動種類。 */
public enum ReviewOperationType {
	CUSTOMER_CREATE("CUS_CREATE", "覆核客戶建立"), APPLICATION_CREATE("APP_CREATE", "覆核保單登打"), POLICY_NUMBER_RESERVE(
			"APP_POLICY_NO", "覆核保單號碼編發"), POLICY_REVERSAL("POL_REVERSAL", "覆核承保撤回"), UNDERWRITING_BATCH_ENQUEUE(
					"UW_BATCH", "覆核新契約批次承保作業"), UNDERWRITING_DECISION("UW_DECISION", "覆核核保審查結果"),
	INITIAL_PREMIUM_MATCH("PRM_MATCH", "覆核新增送金單與首期保費銷帳");

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

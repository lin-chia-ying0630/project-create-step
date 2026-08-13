package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 新契約要保案件的固定狀態與繁中顯示唯一來源。 */
public enum NewContractApplicationStatus {
	APPLICATION_ACCEPTED("AP", "要保受理", "AP", "要保受理", null, "受理"), WAITING_POLICY_ISSUANCE("PW", "待發單／等待", "PW",
			"待發單／等待", null,
			"受理"), UNDERWRITING_PROCESSING("NP", "核保處理中／受理", "NP", "核保處理中／受理", null, "受理"), UNDERWRITING_WAITING("NW",
					"核保等待／警示", "NW", "核保等待／警示", null,
					"受理"), UNDERWRITING_REFERRED("NR", "核保照會／退回", "NR", "核保照會／退回", null, "受理"), INQUIRY_WAITING("UW",
							"等待照會回覆", "UW", "等待照會回覆", null, "受理"), INQUIRY_COMPLETED("US", "照會完成", "US", "照會完成", null,
									"受理"), UNDERWRITING_INQUIRY_COMPLETED("NS", "照會結束／待核保審查", "NS", "照會結束／待核保審查", null,
											"受理"), UNDERWRITING_COMPLETED("AS", "承保完成／結案", "AS", "承保完成／結案", "01",
													"有效"), UNDERWRITING_DECLINED("RS", "拒保完成", "RS", "拒保完成", "13",
															"拒保"), UNDERWRITING_POSTPONED("DS", "延期完成", "DS", "延期完成",
																	"14", "延期"), UNDERWRITING_CANCELLED("CS", "取消完成",
																			"CS", "取消完成", "15",
																			"取消"), POLICY_ISSUED("PS", "保單製發完成", "PS",
																					"保單製發完成", "01", "有效");

	private final String code;
	private final String description;
	private final String stageCode;
	private final String stageDescription;
	private final String contractStatusCode;
	private final String contractStatusDescription;

	NewContractApplicationStatus(String code, String description, String stageCode, String stageDescription,
			String contractStatusCode, String contractStatusDescription) {
		this.code = code;
		this.description = description;
		this.stageCode = stageCode;
		this.stageDescription = stageDescription;
		this.contractStatusCode = contractStatusCode;
		this.contractStatusDescription = contractStatusDescription;
	}

	public static NewContractApplicationStatus fromCode(String code) {
		return Arrays.stream(values()).filter(status -> status.code.equals(code)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("未知的新契約案件狀態代碼: " + code));
	}

	public String code() {
		return code;
	}
	public String description() {
		return description;
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
}

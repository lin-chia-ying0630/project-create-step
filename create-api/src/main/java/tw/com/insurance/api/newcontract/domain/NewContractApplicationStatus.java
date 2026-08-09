package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 新契約要保案件的固定狀態與繁中顯示唯一來源。 */
public enum NewContractApplicationStatus {
	APPLICATION_ACCEPTED("AP", "要保受理", "AP", "要保受理", "NOT_UNDERWRITTEN", "未承保"),
	WAITING_POLICY_ISSUANCE("PW", "待發單／等待", "PW", "待發單／等待", "NOT_UNDERWRITTEN", "未承保"),
	UNDERWRITING_PROCESSING("NP", "核保處理中／受理", "NP", "核保處理中／受理", "NOT_UNDERWRITTEN", "未承保"),
	UNDERWRITING_WAITING("NW", "核保等待／警示", "NW", "核保等待／警示", "NOT_UNDERWRITTEN", "未承保"),
	UNDERWRITING_REFERRED("NR", "核保照會／退回", "NR", "核保照會／退回", "NOT_UNDERWRITTEN", "未承保"),
	INQUIRY_WAITING("UW", "等待照會回覆", "UW", "等待照會回覆", "NOT_UNDERWRITTEN", "未承保"),
	INQUIRY_COMPLETED("US", "照會完成", "US", "照會完成", "NOT_UNDERWRITTEN", "未承保"),
	UNDERWRITING_COMPLETED("NS", "核保完成／結案", "NS", "核保完成／結案", "UNDERWRITTEN", "已承保"),
	UNDERWRITING_CANCELLED("NC", "核保取消", "NC", "核保取消", "NOT_UNDERWRITTEN", "未承保"),
	POLICY_ISSUED("PS", "保單製發完成", "PS", "保單製發完成", "UNDERWRITTEN", "已承保");

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

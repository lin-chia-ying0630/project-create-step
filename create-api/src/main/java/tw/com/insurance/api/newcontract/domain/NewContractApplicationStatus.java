package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 新契約要保案件的固定狀態與繁中顯示唯一來源。 */
public enum NewContractApplicationStatus {
	SUBMITTED("SUBMITTED", "已送件，待核保", "APPLICATION_RECEIVED", "要保案件受理", "NOT_UNDERWRITTEN", "未承保"),
	VALIDATED("VALIDATED", "檢核通過", "PRE_UNDERWRITING_CHECK", "核保前檢核", "NOT_UNDERWRITTEN",
			"未承保"),
	UNDERWRITING("UNDERWRITING", "核保中", "UNDERWRITING", "核保審查", "NOT_UNDERWRITTEN", "未承保"), INQUIRY(
					"INQUIRY", "照會中", "UNDERWRITING", "核保審查", "NOT_UNDERWRITTEN",
					"未承保"),
	UNDERWRITTEN("UNDERWRITTEN", "已承保", "POLICY_ISSUANCE", "承保完成", "UNDERWRITTEN", "已承保");

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

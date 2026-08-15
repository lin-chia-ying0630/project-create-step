package tw.com.insurance.api.newcontract.domain;

import java.util.Arrays;

/** 新契約階段碼、完整英文名稱、繁中說明及契約狀態的唯一程式來源。 */
public enum NewContractApplicationStatus {
	APPLICATION_ACCEPTED("AP", "Application Accepted", "要保受理", null, "受理"),
	WAITING_POLICY_ISSUANCE("PW", "Waiting for Policy Issuance", "待發單／等待", null, "受理"),
	UNDERWRITING_PROCESSING("NP", "Underwriting Processing", "核保處理中／受理", null, "受理"),
	UNDERWRITING_WAITING("NW", "Underwriting Waiting", "核保等待／警示", null, "受理"),
	UNDERWRITING_REFERRED("NR", "Underwriting Referred", "核保照會／退回", null, "受理"),
	INQUIRY_WAITING("UW", "Waiting for Inquiry Response", "等待照會回覆", null, "受理"),
	INQUIRY_COMPLETED("US", "Inquiry Completed", "照會完成", null, "受理"),
	UNDERWRITING_INQUIRY_COMPLETED("NS", "Pending Underwriting Review", "照會結束／待核保審查", null, "受理"),
	UNDERWRITING_COMPLETED("AS", "Underwriting Accepted", "承保完成／結案", "01", "有效"),
	UNDERWRITING_DECLINED("RS", "Underwriting Declined", "拒保完成", "13", "拒保"),
	UNDERWRITING_POSTPONED("DS", "Underwriting Postponed", "延期完成", "14", "延期"),
	UNDERWRITING_CANCELLED("CS", "Underwriting Cancelled", "取消完成", "15", "取消"),
	POLICY_ISSUED("PS", "Policy Issued", "保單製發完成", "01", "有效");

	private final String newContractStageCode;
	private final String newContractStageNameEn;
	private final String newContractStageDescriptionZhTw;
	private final String contractStatusCode;
	private final String contractStatusDescription;

	NewContractApplicationStatus(String newContractStageCode, String newContractStageNameEn,
			String newContractStageDescriptionZhTw, String contractStatusCode, String contractStatusDescription) {
		this.newContractStageCode = newContractStageCode;
		this.newContractStageNameEn = newContractStageNameEn;
		this.newContractStageDescriptionZhTw = newContractStageDescriptionZhTw;
		this.contractStatusCode = contractStatusCode;
		this.contractStatusDescription = contractStatusDescription;
	}

	/** 以正式新契約階段碼取得固定定義，未知代碼不得靜默轉換。 */
	public static NewContractApplicationStatus fromCode(String code) {
		return Arrays.stream(values()).filter(status -> status.newContractStageCode.equals(code)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("未知的新契約階段碼: " + code));
	}

	public String newContractStageCode() {
		return newContractStageCode;
	}
	public String newContractStageNameEn() {
		return newContractStageNameEn;
	}
	public String newContractStageDescriptionZhTw() {
		return newContractStageDescriptionZhTw;
	}
	public String contractStatusCode() {
		return contractStatusCode;
	}
	public String contractStatusDescription() {
		return contractStatusDescription;
	}
}

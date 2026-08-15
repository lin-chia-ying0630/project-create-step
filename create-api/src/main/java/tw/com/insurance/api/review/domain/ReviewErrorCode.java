package tw.com.insurance.api.review.domain;

import tw.com.insurance.api.common.error.ErrorCode;

/** 覆核工作流固定錯誤契約。 */
public enum ReviewErrorCode implements ErrorCode {
	NOT_FOUND("REV-4041", "查無覆核案件"), DUPLICATE_PENDING("REV-4091", "相同業務資料已有待覆核案件"), ALREADY_DECIDED("REV-4092",
			"覆核案件已處理，請重新整理"), MAKER_CANNOT_REVIEW("REV-4221", "建立人不得覆核自己的案件"), INVALID_PAYLOAD("REV-4001",
					"覆核案件內容無法解析"), INVALID_STATUS("REV-4002", "覆核狀態僅允許 P、A 或 R"),
	INVALID_OPERATION_TYPE("REV-4003", "覆核功能分類不存在");

	private final String code;
	private final String message;
	ReviewErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}
	public String code() {
		return code;
	}
	public String message() {
		return message;
	}
}

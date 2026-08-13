package tw.com.insurance.api.review.domain;

import java.util.Arrays;

/** 覆核案件的固定單碼狀態；P 待覆核、A 核准、R 退回。 */
public enum ReviewStatus {
	PENDING("P", "待覆核／處理中"), APPROVED("A", "覆核核准"), REJECTED("R", "覆核退回");

	private final String code;
	private final String description;

	ReviewStatus(String code, String description) {
		this.code = code;
		this.description = description;
	}

	/** 將 API 或資料庫單碼嚴格轉為覆核狀態，未知代碼不採預設值。 */
	public static ReviewStatus fromCode(String code) {
		return Arrays.stream(values()).filter(status -> status.code.equals(code)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("未知覆核狀態代碼: " + code));
	}

	public String code() {
		return code;
	}

	public String description() {
		return description;
	}
}

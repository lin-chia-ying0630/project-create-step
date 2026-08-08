package tw.com.insurance.batch.underwriting.domain;

import java.util.Arrays;

/** 首期保險費應收與實收配對的固定代碼及繁中說明。 */
public enum PremiumMatchStatus {
	MATCHED("MATCHED", "配對成功"), UNDERPAID("UNDERPAID", "短收"), OVERPAID("OVERPAID",
			"溢收"), CURRENCY_MISMATCH("CURRENCY_MISMATCH", "幣別不符"), NOT_RECEIVED("NOT_RECEIVED", "尚未收款");

	private final String code;
	private final String description;

	PremiumMatchStatus(String code, String description) {
		this.code = code;
		this.description = description;
	}

	/** 依正式代碼取得配對狀態，未知代碼視為資料契約錯誤。 */
	public static PremiumMatchStatus fromCode(String code) {
		return Arrays.stream(values()).filter(status -> status.code.equals(code)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("未知的首期保費配對狀態: " + code));
	}

	public String code() {
		return code;
	}
	public String description() {
		return description;
	}
}

package tw.com.insurance.api.common.error;

public enum CommonErrorCode implements ErrorCode {
	INVALID_REQUEST("REQ-4001", "輸入資料格式錯誤"), SYSTEM_FAILURE("SYS-9001", "系統處理失敗，請稍後再試");

	private final String code;
	private final String message;
	CommonErrorCode(String code, String message) {
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

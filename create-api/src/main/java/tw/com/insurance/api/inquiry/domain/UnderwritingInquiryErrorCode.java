package tw.com.insurance.api.inquiry.domain;

import tw.com.insurance.api.common.error.ErrorCode;

public enum UnderwritingInquiryErrorCode implements ErrorCode {
	NOT_FOUND("INQ-4041", "查無核保照會單"), POLICY_NUMBER_NOT_RESERVED("INQ-4091", "照會案件尚未取得固定保單號碼，不得產生照會單");

	private final String code;
	private final String message;
	UnderwritingInquiryErrorCode(String code, String message) {
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

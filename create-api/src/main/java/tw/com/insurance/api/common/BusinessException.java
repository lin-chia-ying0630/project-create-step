package tw.com.insurance.api.common;

import tw.com.insurance.api.common.error.ErrorCode;

public class BusinessException extends RuntimeException {
	private final String errorCode;

	public BusinessException(ErrorCode error) {
		super(error.message());
		this.errorCode = error.code();
	}

	public String errorCode() {
		return errorCode;
	}
}

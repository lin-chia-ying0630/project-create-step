package tw.com.insurance.api.customer.domain;

import tw.com.insurance.api.common.error.ErrorCode;

public enum CustomerErrorCode implements ErrorCode {
	INVALID_CUSTOMER_TYPE("CUS-4000", "客戶類型僅可為自然人或公司行號"), INVALID_NATIONAL_ID("CUS-4001",
			"身分證字號格式或檢核碼錯誤"), PERSON_REQUIRED_FIELDS("CUS-4002", "自然人須填寫性別與出生日期"), ORGANIZATION_REQUIRED_FIELDS(
					"CUS-4003", "公司行號須填寫負責人、行業別及組織類型"), INVALID_BUSINESS_NO("CUS-4004",
							"公司行號須使用 8 碼統一編號"), INVALID_KYC_CODE("CUS-4005", "職業、資金來源或投保目的代碼無效"), INVALID_CONTACT_CODE(
									"CUS-4006", "國家或郵遞區號代碼無效"), DUPLICATE_IDENTITY("CUS-4091", "此身分識別資料已建立客戶");

	private final String code;
	private final String message;
	CustomerErrorCode(String code, String message) {
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

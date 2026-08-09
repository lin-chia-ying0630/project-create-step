package tw.com.insurance.api.newcontract.domain;

import tw.com.insurance.api.common.error.ErrorCode;

public enum NewContractErrorCode implements ErrorCode {
	PREMIUM_DUE_NOT_FOUND("NCT-4041", "查無待收首期保險費案件，可能尚未產生應收或已完成銷帳"), APPLICATION_NOT_FOUND("NCT-4042", "查無要保案件"),
	QUERY_NOT_FOUND("NCT-4043", "查無客戶 ID、要保書號碼或保單號碼"),
	INVALID_EFFECTIVE_DATE("NCT-4001", "預定生效日不得早於要保日期"),
	INVALID_PARTY("NCT-4002", "要保人或被保險人不是有效客戶"),
	INVALID_BASE_COVERAGE("NCT-4003", "每份要保書必須且只能有一筆主約"),
	INVALID_COVERAGE_TYPE("NCT-4004", "保障項目類型僅可為 BASE 或 RIDER"),
	INVALID_BENEFICIARY("NCT-4005", "受益人必須擇一指定客戶或指定方式"),
	HEALTH_DETAIL_REQUIRED("NCT-4006", "健康告知回答是時，必須填寫補充說明"),
	INVALID_BENEFICIARY_ALLOCATION("NCT-4007", "同一順位受益分配比例合計須為 100%"),
	INVALID_CURRENCY("NCT-4008", "幣別代碼無效"),
	INVALID_PRODUCT("NCT-4009", "商品不存在、尚未完成上架或已停售"),
	INVALID_PAYMENT_INSTRUMENT("PAY-4003", "銀行帳號或信用卡號格式無效"),
	PAYMENT_INSTRUMENT_NOT_VALIDATED("PAY-4221", "首期保費付款工具尚未完成驗證"),
	INVESTMENT_SUITABILITY_REQUIRED("NCT-4221", "投資型商品必須完成風險屬性與適合度評估"),
	PRODUCT_LIMIT_VIOLATION("NCT-4222", "保險金額或保險費不符合商品定義的投保限制"),
	INVALID_PAYMENT_CHANNEL("PAY-4001", "繳費管道代碼無效"),
	INVALID_PAYER_ROLE("PAY-4002", "繳款人身分代碼無效"),
	INVALID_BATCH_EXECUTION_DATE("BAT-4001", "新契約批次承保作業執行日不得早於臺北當日"),
	APPLICATION_NOT_READY_FOR_BATCH("BAT-4221", "要保案件尚未進入 PW 待發單狀態，不可排入批次"),
	DUPLICATE_APPLICATION("NCT-4091", "要保書號碼已存在"),
	PAYMENT_ALREADY_MATCHED("PAY-4091", "繳費憑證、收款交易序號或應收紀錄已完成銷帳"),
	DUPLICATE_BATCH_REQUEST("BAT-4091", "此案件已排入指定營業日批次"),
	POLICY_NOT_FOUND("POL-4041", "查無可撤回的正式保單"),
	POLICY_REVERSAL_BLOCKED("POL-4091", "此保單目前不符合承保撤回條件"),
	CONCURRENT_MODIFICATION("POL-4092", "資料已被其他人異動，請重新查詢"),
	UNDERWRITING_CASE_NOT_FOUND("UWR-4041", "查無可進行核保審查的案件"),
	INVALID_UNDERWRITING_DECISION("UWR-4001", "核保結果代碼無效，請重新取得目前可用選項"),
	UNDERWRITING_CONCURRENT_MODIFICATION("UWR-4091", "核保案件已被其他人異動，請重新查詢");
	private final String code;
	private final String message;
	NewContractErrorCode(String code, String message) {
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

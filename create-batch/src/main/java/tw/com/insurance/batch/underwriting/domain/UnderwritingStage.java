package tw.com.insurance.batch.underwriting.domain;

/** 臺灣壽險新契約批次採用的兩碼核保階段；此為本系統內碼。 */
public enum UnderwritingStage {
	PROCESSING("NP", "核保處理中／受理"), COMPLETED("AS", "承保完成／結案"),
	REFERRED("NR", "核保照會／退回"), CANCELLED("NC", "核保取消"), WAITING("NW", "核保等待／警示");

	private final String code;
	private final String description;

	UnderwritingStage(String code, String description) {
		this.code = code;
		this.description = description;
	}

	/** 回傳資料庫與 API 使用的兩碼正式值。 */
	public String code() {
		return code;
	}

	/** 回傳畫面與文件使用的繁中名稱。 */
	public String description() {
		return description;
	}
}

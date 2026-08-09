package tw.com.insurance.api.newcontract.codedefinition.dto;

/** 提供前端下拉選單與對照查詢使用的動態代碼及來源分類。 */
public record CodeDefinitionDto(String code, String description, String descriptionEn, String classificationCode,
		String classificationDescription, String breakdownCode, String breakdownDescription, String natureOfWork,
		String sourceVersion) {
}

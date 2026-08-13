package tw.com.insurance.api.newcontract.codedefinition.dto;

/** 提供畫面選擇可查詢之代碼定義群組與欄位。 */
public record CodeDefinitionTableDto(String codeGroup, String codeGroupDescription, String codeField,
		String codeFieldDescription) {
}

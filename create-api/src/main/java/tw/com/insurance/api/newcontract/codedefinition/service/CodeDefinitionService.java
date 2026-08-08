package tw.com.insurance.api.newcontract.codedefinition.service;

import java.util.List;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;

/** 提供動態業務代碼查詢與後端驗證。 */
public interface CodeDefinitionService {

	/** 取得指定代碼欄位目前可使用的選項。 */
	List<CodeDefinitionDto> findActiveOptions(String codeGroup, String codeField);

	/** 驗證指定代碼是否存在且已啟用。 */
	boolean isActiveCode(String codeGroup, String codeField, String code);
}

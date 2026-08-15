package tw.com.insurance.api.newcontract.codedefinition.service;

import java.util.List;
import tw.com.insurance.api.common.dto.PageResult;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionTableDto;

/** 提供動態業務代碼查詢與後端驗證。 */
public interface CodeDefinitionService {

	/** 取得目前可供畫面選擇的代碼群組與欄位。 */
	List<CodeDefinitionTableDto> findActiveTables();

	/** 取得指定代碼欄位目前可使用的選項。 */
	List<CodeDefinitionDto> findActiveOptions(String codeGroup, String codeField);

	/** 分頁取得指定代碼欄位目前可使用的選項。 */
	PageResult<CodeDefinitionDto> findActiveOptionPage(String codeGroup, String codeField, int page, int pageSize,
			String query);

	/** 驗證指定代碼是否存在且已啟用。 */
	boolean isActiveCode(String codeGroup, String codeField, String code);
}

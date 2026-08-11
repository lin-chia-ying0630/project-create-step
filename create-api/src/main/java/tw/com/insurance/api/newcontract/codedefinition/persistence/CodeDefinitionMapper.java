package tw.com.insurance.api.newcontract.codedefinition.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionTableDto;

/** 定義動態代碼設定表的查詢契約；SQL 統一放在 Mapper XML。 */
@Mapper
public interface CodeDefinitionMapper {

	/** 取得目前至少有一筆有效代碼的群組與欄位清單。 */
	List<CodeDefinitionTableDto> findActiveTables();

	/** 依代碼群組與欄位取得啟用中的選項。 */
	List<CodeDefinitionDto> findActiveOptions(@Param("codeGroup") String codeGroup,
			@Param("codeField") String codeField);

	/** 檢查指定代碼是否為目前啟用的正式代碼。 */
	boolean existsActiveCode(@Param("codeGroup") String codeGroup, @Param("codeField") String codeField,
			@Param("code") String code);
}

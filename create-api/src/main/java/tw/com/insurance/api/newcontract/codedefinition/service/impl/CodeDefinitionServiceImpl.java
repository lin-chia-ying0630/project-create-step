package tw.com.insurance.api.newcontract.codedefinition.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.persistence.CodeDefinitionMapper;
import tw.com.insurance.api.newcontract.codedefinition.service.CodeDefinitionService;

/** 由資料庫代碼設定表提供唯一的代碼與繁中說明來源。 */
@Service
public class CodeDefinitionServiceImpl implements CodeDefinitionService {
	private final CodeDefinitionMapper mapper;

	public CodeDefinitionServiceImpl(CodeDefinitionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<CodeDefinitionDto> findActiveOptions(String codeGroup, String codeField) {
		return mapper.findActiveOptions(codeGroup, codeField);
	}

	@Override
	public boolean isActiveCode(String codeGroup, String codeField, String code) {
		return code != null && !code.isBlank() && mapper.existsActiveCode(codeGroup, codeField, code);
	}
}

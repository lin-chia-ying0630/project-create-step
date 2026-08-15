package tw.com.insurance.api.newcontract.codedefinition.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.common.dto.PageResult;
import tw.com.insurance.api.common.util.PageSortRequest;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionTableDto;
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
	public List<CodeDefinitionTableDto> findActiveTables() {
		return mapper.findActiveTables();
	}

	@Override
	public List<CodeDefinitionDto> findActiveOptions(String codeGroup, String codeField) {
		return mapper.findActiveOptions(codeGroup, codeField);
	}

	@Override
	public PageResult<CodeDefinitionDto> findActiveOptionPage(String codeGroup, String codeField, int page,
			int pageSize, String query) {
		PageSortRequest pageQuery = PageSortRequest.of(page, pageSize, "code,asc", java.util.Set.of("code"), "code");
		String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
		long totalItems = mapper.countActiveOptions(codeGroup, codeField, normalizedQuery);
		List<CodeDefinitionDto> items = mapper.findActiveOptionPage(codeGroup, codeField, pageQuery.offset(),
				pageQuery.pageSize(), normalizedQuery);
		return new PageResult<>(items, totalItems, pageQuery.page(), pageQuery.pageSize(),
				pageQuery.totalPages(totalItems));
	}

	@Override
	public boolean isActiveCode(String codeGroup, String codeField, String code) {
		return code != null && !code.isBlank() && mapper.existsActiveCode(codeGroup, codeField, code);
	}
}

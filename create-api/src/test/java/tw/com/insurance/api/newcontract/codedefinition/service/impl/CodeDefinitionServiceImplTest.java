package tw.com.insurance.api.newcontract.codedefinition.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.persistence.CodeDefinitionMapper;

/** 驗證代碼定義查詢頁只向資料庫取得指定分頁。 */
class CodeDefinitionServiceImplTest {
	private final CodeDefinitionMapper mapper = mock(CodeDefinitionMapper.class);
	private final CodeDefinitionServiceImpl service = new CodeDefinitionServiceImpl(mapper);

	/** 職業代碼筆數再多也只應回傳要求的單頁資料與正確頁數。 */
	@Test
	void shouldReturnOnlyRequestedCodeDefinitionPage() {
		CodeDefinitionDto option = new CodeDefinitionDto("01010101", "測試職業", null, "01", "測試大分類",
				"0101", "測試中分類", "測試工作", "TEST-2026");
		when(mapper.countActiveOptions("customer-kyc", "occupation_code", "測試")).thenReturn(1324L);
		when(mapper.findActiveOptionPage("customer-kyc", "occupation_code", 10, 10, "測試"))
				.thenReturn(List.of(option));

		var result = service.findActiveOptionPage("customer-kyc", "occupation_code", 2, 10, " 測試 ");

		assertThat(result.items()).containsExactly(option);
		assertThat(result.totalItems()).isEqualTo(1324);
		assertThat(result.page()).isEqualTo(2);
		assertThat(result.totalPages()).isEqualTo(133);
		verify(mapper).findActiveOptionPage("customer-kyc", "occupation_code", 10, 10, "測試");
	}
}

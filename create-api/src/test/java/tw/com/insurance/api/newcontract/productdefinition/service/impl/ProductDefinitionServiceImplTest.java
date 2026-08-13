package tw.com.insurance.api.newcontract.productdefinition.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.persistence.ProductDefinitionMapper;

/** 驗證商品定義查詢以資料庫有效商品作為唯一來源。 */
class ProductDefinitionServiceImplTest {
	private final ProductDefinitionMapper mapper = mock(ProductDefinitionMapper.class);
	private final ProductDefinitionServiceImpl service = new ProductDefinitionServiceImpl(mapper);

	/** 已完成上架的商品應完整回傳類型與投保限制。 */
	@Test
	void shouldReturnActiveProductDefinition() {
		ProductDefinitionDto product = product("LIFE-DEMO", false);
		when(mapper.findActiveProduct("LIFE-DEMO", "1.0")).thenReturn(product);

		assertThat(service.requireActiveProduct("LIFE-DEMO", "1.0")).isEqualTo(product);
	}

	/** 查無有效商品時不得接受前端自行輸入的商品代碼。 */
	@Test
	void shouldRejectUnknownOrInactiveProduct() {
		when(mapper.findActiveProduct("UNKNOWN", "1.0")).thenReturn(null);

		assertThatThrownBy(() -> service.requireActiveProduct("UNKNOWN", "1.0")).isInstanceOf(BusinessException.class);
	}

	/** 建立不含真實商品或客戶資料的測試商品。 */
	private ProductDefinitionDto product(String code, boolean investmentProduct) {
		LocalDateTime auditTime = LocalDateTime.of(2026, 1, 1, 9, 0);
		return new ProductDefinitionDto(code, "1.0", "測試商品", investmentProduct ? "I" : "L",
				investmentProduct ? "投資型保險" : "傳統型壽險", "BASE", "TWD", investmentProduct ? "R3" : null, 0, 70,
				new BigDecimal("100000"), new BigDecimal("10000000"), new BigDecimal("1000"), 1, 99, 1, 30,
				LocalDate.of(2026, 1, 1), null, investmentProduct, "test-maker", auditTime, "test-maker", auditTime,
				"test-checker", auditTime);
	}
}

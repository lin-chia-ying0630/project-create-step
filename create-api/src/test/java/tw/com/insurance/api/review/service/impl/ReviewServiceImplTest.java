package tw.com.insurance.api.review.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import tw.com.insurance.api.customer.service.CustomerService;
import tw.com.insurance.api.newcontract.service.NewContractService;
import tw.com.insurance.api.review.persistence.ReviewMapper;

/** 驗證覆核工作台分類由後端參與計數與分頁。 */
class ReviewServiceImplTest {
	private final ReviewMapper mapper = mock(ReviewMapper.class);
	private final ReviewServiceImpl service = new ReviewServiceImpl(mapper, mock(CustomerService.class),
			mock(NewContractService.class), new ObjectMapper(), "test-only-encryption-key");

	/** 切換至客戶建立分類時，count 與 page 查詢必須使用相同分類條件。 */
	@Test
	void shouldApplyOperationTypeToCountAndPageQueries() {
		when(mapper.countByStatus("P", null, "CUSTOMER_CREATE")).thenReturn(0L);
		when(mapper.findPage("P", 0, 10, "reviewId", "asc", null, "CUSTOMER_CREATE"))
				.thenReturn(List.of());

		var result = service.findPage("P", 1, 10, "reviewId,asc", "", "CUSTOMER_CREATE");

		assertThat(result.items()).isEmpty();
		assertThat(result.totalItems()).isZero();
		verify(mapper).countByStatus("P", null, "CUSTOMER_CREATE");
		verify(mapper).findPage("P", 0, 10, "reviewId", "asc", null, "CUSTOMER_CREATE");
	}
}

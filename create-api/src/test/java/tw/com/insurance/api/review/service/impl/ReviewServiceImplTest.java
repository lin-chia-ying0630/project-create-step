package tw.com.insurance.api.review.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tw.com.insurance.api.customer.service.CustomerService;
import tw.com.insurance.api.newcontract.service.NewContractService;
import tw.com.insurance.api.review.persistence.ReviewMapper;

/** 驗證覆核工作台分類由後端參與計數與分頁。 */
class ReviewServiceImplTest {
	private final ReviewMapper mapper = mock(ReviewMapper.class);
	private final ReviewServiceImpl service = new ReviewServiceImpl(mapper, mock(CustomerService.class),
			mock(NewContractService.class), new ObjectMapper(), "test-only-encryption-key");

	/** 分類沒有待覆核案件時，只執行 count，不再發出必定為空的分頁查詢。 */
	@Test
	void shouldApplyOperationTypeToCountAndPageQueries() {
		when(mapper.countByStatus("P", null, "CUSTOMER_CREATE")).thenReturn(0L);

		var result = service.findPage("P", 1, 10, "reviewId,asc", "", "CUSTOMER_CREATE");

		assertThat(result.items()).isEmpty();
		assertThat(result.totalItems()).isZero();
		verify(mapper).countByStatus("P", null, "CUSTOMER_CREATE");
		verify(mapper, never()).findPage("P", 0, 10, "reviewId", "asc", null, "CUSTOMER_CREATE");
	}
}

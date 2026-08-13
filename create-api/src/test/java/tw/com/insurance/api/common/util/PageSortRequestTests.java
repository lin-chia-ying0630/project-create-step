package tw.com.insurance.api.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PageSortRequestTests {

	/** 驗證頁碼、每頁筆數、排序欄位與方向都會套用安全邊界。 */
	@Test
	void shouldNormalizePaginationAndRejectUnknownSortField() {
		PageSortRequest result = PageSortRequest.of(0, 999, "unsafe,desc", Set.of("reviewId"), "reviewId");

		assertThat(result.page()).isEqualTo(1);
		assertThat(result.pageSize()).isEqualTo(100);
		assertThat(result.offset()).isZero();
		assertThat(result.sortField()).isEqualTo("reviewId");
		assertThat(result.sortDirection()).isEqualTo("desc");
	}

	/** 驗證合法欄位、預設升冪與總頁數計算可供不同功能共用。 */
	@Test
	void shouldRetainAllowedFieldAndCalculateTotalPages() {
		PageSortRequest result = PageSortRequest.of(2, 20, "businessKey", Set.of("businessKey"), "reviewId");

		assertThat(result.offset()).isEqualTo(20);
		assertThat(result.sortField()).isEqualTo("businessKey");
		assertThat(result.sortDirection()).isEqualTo("asc");
		assertThat(result.totalPages(41)).isEqualTo(3);
	}
}

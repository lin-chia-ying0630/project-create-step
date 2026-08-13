package tw.com.insurance.api.review.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReviewStatusTests {
	@Test
	void fromCode_givenSupportedSingleCharacterCode_returnsFormalStatus() {
		assertThat(ReviewStatus.fromCode("P")).isEqualTo(ReviewStatus.PENDING);
		assertThat(ReviewStatus.fromCode("A")).isEqualTo(ReviewStatus.APPROVED);
		assertThat(ReviewStatus.fromCode("R")).isEqualTo(ReviewStatus.REJECTED);
	}

	@Test
	void fromCode_givenLegacyLongCode_rejectsUnknownStatus() {
		assertThatThrownBy(() -> ReviewStatus.fromCode("PENDING")).isInstanceOf(IllegalArgumentException.class);
	}
}

package tw.com.insurance.api.review.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import tw.com.insurance.api.common.BusinessException;

class ReviewDecisionPolicyTests {
	@Test
	void validate_givenDifferentReviewerAndPendingStatus_allowsDecision() {
		assertThatCode(() -> ReviewDecisionPolicy.validate("P", "maker-01", "reviewer-02")).doesNotThrowAnyException();
	}

	@Test
	void validate_givenSameMakerAndReviewer_rejectsDecision() {
		assertThatThrownBy(() -> ReviewDecisionPolicy.validate("P", "maker-01", "maker-01"))
				.isInstanceOf(BusinessException.class).hasMessage("建立人不得覆核自己的案件");
	}

	@Test
	void validate_givenCompletedCase_rejectsSecondDecision() {
		assertThatThrownBy(() -> ReviewDecisionPolicy.validate("A", "maker-01", "reviewer-02"))
				.isInstanceOf(BusinessException.class).hasMessage("覆核案件已處理，請重新整理");
	}
}

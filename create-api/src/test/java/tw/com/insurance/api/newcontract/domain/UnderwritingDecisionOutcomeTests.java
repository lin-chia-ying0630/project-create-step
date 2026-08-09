package tw.com.insurance.api.newcontract.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UnderwritingDecisionOutcomeTests {
	@Test
	void acceptedOutcomesContinueAsActiveContract() {
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("SA").insurable()).isTrue();
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("RA").contractStatusCode()).isEqualTo("01");
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("EA").stageCode()).isEqualTo("AS");
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("CA").insurable()).isTrue();
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("PA").insurable()).isTrue();
	}

	@Test
	void nonAcceptedOutcomesStopBeforeIssuance() {
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("DC").contractStatusCode()).isEqualTo("13");
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("PO").contractStatusCode()).isEqualTo("14");
		assertThat(UnderwritingDecisionOutcome.fromDecisionCode("CN").contractStatusCode()).isEqualTo("15");
	}

	@Test
	void unknownOutcomeIsRejected() {
		assertThatThrownBy(() -> UnderwritingDecisionOutcome.fromDecisionCode("XX"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}

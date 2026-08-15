package tw.com.insurance.api.newcontract.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NewContractApplicationStatusTests {
	@Test
	void fromCode_givenEveryStage_returnsCompleteCanonicalNames() {
		for (NewContractApplicationStatus stage : NewContractApplicationStatus.values()) {
			assertThat(NewContractApplicationStatus.fromCode(stage.newContractStageCode())).isSameAs(stage);
			assertThat(stage.newContractStageNameEn()).isNotBlank();
			assertThat(stage.newContractStageDescriptionZhTw()).isNotBlank();
		}
	}

	@Test
	void fromCode_givenUnknownStage_throwsIllegalArgumentException() {
		assertThatThrownBy(() -> NewContractApplicationStatus.fromCode("XX"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("未知的新契約階段碼");
	}
}

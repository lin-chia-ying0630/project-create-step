package tw.com.insurance.api.newcontract.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.newcontract.codedefinition.service.CodeDefinitionService;
import tw.com.insurance.api.newcontract.dto.NewContractDtos.PaymentInstrumentValidationRequest;
import tw.com.insurance.api.newcontract.persistence.NewContractMapper;
import tw.com.insurance.api.newcontract.productdefinition.service.ProductDefinitionService;

class NewContractServiceImplPaymentValidationTest {
	private NewContractServiceImpl service;

	/** 每一測試使用隔離的 Mapper 與代碼服務，避免付款格式驗證觸碰資料庫。 */
	@BeforeEach
	void setUp() {
		service = new NewContractServiceImpl(mock(NewContractMapper.class), mock(CodeDefinitionService.class),
				mock(ProductDefinitionService.class), "test-only-payment-token-key");
	}

	/** 合法信用卡應通過 Luhn 與有效年月檢查，且只回傳遮罩號碼及 Token。 */
	@Test
	void shouldValidateCreditCardWithoutReturningFullNumber() {
		var result = service.validatePaymentInstrument(
				new PaymentInstrumentValidationRequest("C", "4111-1111-1111-1111", null, null, "12", "2099"));

		assertThat(result.validationStatus()).isEqualTo("S");
		assertThat(result.maskedNumber()).endsWith("1111").doesNotContain("4111111111111111");
		assertThat(result.paymentToken()).startsWith("PAY-");
	}

	/** 錯誤的信用卡檢查碼必須拒絕，不可產生可送件的付款 Token。 */
	@Test
	void shouldRejectInvalidCreditCardChecksum() {
		var request = new PaymentInstrumentValidationRequest("C", "4111111111111112", null, null, "12", "2099");

		assertThatThrownBy(() -> service.validatePaymentInstrument(request)).isInstanceOf(BusinessException.class);
	}

	/** 銀行帳號需為 6 至 20 位數字，並同時提供三碼銀行代碼。 */
	@Test
	void shouldValidateBankAccountWithBankCode() {
		var result = service.validatePaymentInstrument(
				new PaymentInstrumentValidationRequest("B", "123456789012", "012", "0001", null, null));

		assertThat(result.validationStatus()).isEqualTo("S");
		assertThat(result.institutionCode()).isEqualTo("012");
		assertThat(result.maskedNumber()).endsWith("9012");
	}
}

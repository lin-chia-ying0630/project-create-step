package tw.com.insurance.batch.underwriting.validation;

import java.math.BigDecimal;
import tw.com.insurance.batch.underwriting.domain.PremiumMatchStatus;

public final class InitialPremiumMatcher {
	/** 比較應收與實收金額、幣別及收款狀態，產生固定配對結果。 */
	public PremiumMatchStatus match(BigDecimal expected, String expectedCurrency, BigDecimal actual,
			String actualCurrency, boolean received) {
		if (!received || actual == null)
			return PremiumMatchStatus.NOT_RECEIVED;
		if (expectedCurrency == null || !expectedCurrency.equals(actualCurrency))
			return PremiumMatchStatus.CURRENCY_MISMATCH;
		int comparison = actual.compareTo(expected);
		if (comparison < 0)
			return PremiumMatchStatus.UNDERPAID;
		if (comparison > 0)
			return PremiumMatchStatus.OVERPAID;
		return PremiumMatchStatus.MATCHED;
	}
}

package tw.com.insurance.batch.underwriting;

import java.math.BigDecimal;

public final class InitialPremiumMatcher {
    public PremiumMatchStatus match(BigDecimal expected, String expectedCurrency,
                                    BigDecimal actual, String actualCurrency, boolean received) {
        if (!received || actual == null) return PremiumMatchStatus.NOT_RECEIVED;
        if (expectedCurrency == null || !expectedCurrency.equals(actualCurrency)) return PremiumMatchStatus.CURRENCY_MISMATCH;
        int comparison = actual.compareTo(expected);
        if (comparison < 0) return PremiumMatchStatus.UNDERPAID;
        if (comparison > 0) return PremiumMatchStatus.OVERPAID;
        return PremiumMatchStatus.MATCHED;
    }
}

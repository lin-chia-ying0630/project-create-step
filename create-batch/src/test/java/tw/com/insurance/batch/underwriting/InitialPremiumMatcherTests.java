package tw.com.insurance.batch.underwriting;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class InitialPremiumMatcherTests {
    private final InitialPremiumMatcher matcher = new InitialPremiumMatcher();

    @Test void 應繳與實繳完全一致才配對成功() {
        assertThat(matcher.match(new BigDecimal("12000.0000"), "TWD", new BigDecimal("12000.0000"), "TWD", true))
                .isEqualTo(PremiumMatchStatus.MATCHED);
    }

    @Test void 少繳多繳幣別錯誤及未入帳都不得承保() {
        assertThat(matcher.match(new BigDecimal("12000"), "TWD", new BigDecimal("11999"), "TWD", true)).isEqualTo(PremiumMatchStatus.UNDERPAID);
        assertThat(matcher.match(new BigDecimal("12000"), "TWD", new BigDecimal("12001"), "TWD", true)).isEqualTo(PremiumMatchStatus.OVERPAID);
        assertThat(matcher.match(new BigDecimal("12000"), "TWD", new BigDecimal("12000"), "USD", true)).isEqualTo(PremiumMatchStatus.CURRENCY_MISMATCH);
        assertThat(matcher.match(new BigDecimal("12000"), "TWD", null, null, false)).isEqualTo(PremiumMatchStatus.NOT_RECEIVED);
    }
}

package tw.com.insurance.batch.underwriting;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class BasicPolicyValidatorTests {
    private final BasicPolicyValidator validator = new BasicPolicyValidator();

    @Test void 基本資料正確時通過檢核() {
        var candidate = new ApplicationCandidate("APP-TEST-001", "CUSTOMER-A", "CUSTOMER-B", "LIFE", "1", "TWD",
                new BigDecimal("1000000.0000"), new BigDecimal("12000.0000"), LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 9));
        assertThat(validator.validate(candidate)).isEmpty();
    }

    @Test void 日期與金額錯誤時產生照會依據() {
        var candidate = new ApplicationCandidate("APP-TEST-002", "CUSTOMER-A", "CUSTOMER-B", "LIFE", "1", "TWD",
                BigDecimal.ZERO, new BigDecimal("-1"), LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 7));
        assertThat(validator.validate(candidate)).extracting(ValidationIssue::ruleCode)
                .containsExactly("BASIC_AMOUNT_RANGE", "BASIC_DATE_ORDER");
    }
}

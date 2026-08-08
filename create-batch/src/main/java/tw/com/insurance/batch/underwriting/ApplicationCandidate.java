package tw.com.insurance.batch.underwriting;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApplicationCandidate(
        String applicationNo,
        String applicantCustomerId,
        String insuredCustomerId,
        String productCode,
        String productVersion,
        String currencyCode,
        BigDecimal sumAssuredAmount,
        BigDecimal premiumAmount,
        LocalDate applicationDate,
        LocalDate requestedEffectiveDate) {
}

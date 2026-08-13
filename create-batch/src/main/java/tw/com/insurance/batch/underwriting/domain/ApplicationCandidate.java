package tw.com.insurance.batch.underwriting.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 新契約批次承保作業執行基本檢核所需的要保案件快照。 */
public record ApplicationCandidate(String applicationNo, String applicantCustomerId, String insuredCustomerId,
		String productCode, String productVersion, String currencyCode, BigDecimal sumAssuredAmount,
		BigDecimal premiumAmount, LocalDate applicationDate, LocalDate requestedEffectiveDate) {
}

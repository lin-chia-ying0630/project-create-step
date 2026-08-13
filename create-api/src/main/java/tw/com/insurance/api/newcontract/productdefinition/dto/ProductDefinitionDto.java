package tw.com.insurance.api.newcontract.productdefinition.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 保單登打使用的有效商品定義；商品類型與限制均由後端提供。 */
public record ProductDefinitionDto(String productCode, String productVersion, String productName,
		String productTypeCode, String productTypeDescription, String coverageItemType, String currencyCode,
		String productRiskLevelCode,
		Integer minimumEntryAge, Integer maximumEntryAge, BigDecimal minimumSumAssured,
		BigDecimal maximumSumAssured, BigDecimal minimumPremium, Integer minimumCoverageTermYears,
		Integer maximumCoverageTermYears, Integer minimumPaymentTermYears, Integer maximumPaymentTermYears,
		LocalDate effectiveFrom, LocalDate effectiveTo,
		boolean investmentProduct, String createdBy, LocalDateTime createdAt, String updatedBy,
		LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt) {
}

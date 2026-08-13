package tw.com.insurance.api.newcontract.productdefinition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 商品新增或修改送覆核內容；核准前不異動正式商品定義。 */
public record ProductDefinitionChangeRequest(@NotBlank @Size(max = 32) String productCode,
		@NotBlank @Size(max = 32) String productVersion, @NotBlank @Size(max = 200) String productName,
		@Size(max = 200) String productNameEn, @NotBlank @Size(max = 1) String productTypeCode,
		@NotBlank @Size(max = 10) String coverageItemType, @NotBlank @Size(max = 3) String currencyCode,
		@Size(max = 2) String productRiskLevelCode, Integer minimumEntryAge, Integer maximumEntryAge,
		@DecimalMin("0") BigDecimal minimumSumAssured, @DecimalMin("0") BigDecimal maximumSumAssured,
		@DecimalMin("0") BigDecimal minimumPremium, Integer minimumCoverageTermYears, Integer maximumCoverageTermYears,
		Integer minimumPaymentTermYears, Integer maximumPaymentTermYears, @NotNull LocalDate effectiveFrom,
		LocalDate effectiveTo, @NotBlank @Size(max = 1) String productStatus,
		@NotEmpty List<@NotBlank String> paymentModeCodes, List<@Valid ProductReference> compatibleRiders) {
	/** 可搭配附約商品版本鍵。 */
	public record ProductReference(@NotBlank @Size(max = 32) String productCode,
			@NotBlank @Size(max = 32) String productVersion) {
	}
}

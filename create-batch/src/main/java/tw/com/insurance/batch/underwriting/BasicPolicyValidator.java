package tw.com.insurance.batch.underwriting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class BasicPolicyValidator {
    public List<ValidationIssue> validate(ApplicationCandidate candidate) {
        var issues = new ArrayList<ValidationIssue>();
        if (isBlank(candidate.applicationNo()) || isBlank(candidate.applicantCustomerId())
                || isBlank(candidate.insuredCustomerId()) || isBlank(candidate.productCode())
                || isBlank(candidate.productVersion()) || isBlank(candidate.currencyCode())) {
            issues.add(new ValidationIssue("BASIC_REQUIRED_FIELDS", "基本必填欄位不完整"));
        }
        if (candidate.sumAssuredAmount() == null || candidate.sumAssuredAmount().compareTo(BigDecimal.ZERO) <= 0
                || candidate.premiumAmount() == null || candidate.premiumAmount().compareTo(BigDecimal.ZERO) < 0) {
            issues.add(new ValidationIssue("BASIC_AMOUNT_RANGE", "保額必須大於零且保費不得小於零"));
        }
        if (candidate.applicationDate() == null || candidate.requestedEffectiveDate() == null
                || candidate.requestedEffectiveDate().isBefore(candidate.applicationDate())) {
            issues.add(new ValidationIssue("BASIC_DATE_ORDER", "預定生效日不得早於申請日"));
        }
        return List.copyOf(issues);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

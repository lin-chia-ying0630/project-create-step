package tw.com.insurance.batch.underwriting;

public record ValidationIssue(String ruleCode, String message) {
}

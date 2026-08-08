package tw.com.insurance.batch.underwriting.domain;

/** 一筆未通過核保基本檢核的規則代碼與繁中原因。 */
public record ValidationIssue(String ruleCode, String message) {
}

package tw.com.insurance.api.inquiry.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

public final class UnderwritingInquiryDtos {
	private UnderwritingInquiryDtos() {
	}
	public record InquiryItem(String ruleCode, String ruleName, String itemMessage, String responseText,
			LocalDateTime respondedAt) {
	}
	public record InquiryDetail(String inquiryNo, String applicationNo, String policyNo, String underwritingCaseNo,
			long applicationRevision, String applicantCustomerReference, String applicantNameMasked,
			String insuredCustomerReference, String insuredNameMasked, String productCode, LocalDate applicationDate,
			LocalDate requestedEffectiveDate, String currencyCode, BigDecimal sumAssuredAmount,
			BigDecimal premiumAmount, String newContractStage, String newContractStageDescription,
			String contractStatus, String contractStatusDescription, String underwritingStatus,
			String underwritingStatusDescription, String decisionCode, String decisionDescription, String inquiryStatus,
			String inquiryStatusDescription, LocalDateTime issuedAt, LocalDateTime resolvedAt,
			List<InquiryItem> items) {
	}
	public record InquiryPdfDocument(String inquiryNo, String fileName, String contentType, String base64Content) {
	}
}

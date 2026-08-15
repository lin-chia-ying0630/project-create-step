package tw.com.insurance.api.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
			BigDecimal premiumAmount, String newContractStageCode, String newContractStageNameEn,
			String newContractStageDescriptionZhTw,
			String contractStatus, String contractStatusDescription, String underwritingStatus,
			String underwritingStatusDescription, String decisionCode, String decisionDescription, String inquiryStatus,
			String inquiryStatusDescription, LocalDateTime issuedAt, LocalDateTime resolvedAt,
			List<InquiryItem> items) {
		@Deprecated
		@JsonProperty("newContractStage")
		public String newContractStage() {
			return newContractStageCode;
		}
		@Deprecated
		@JsonProperty("newContractStageDescription")
		public String newContractStageDescription() {
			return newContractStageDescriptionZhTw;
		}
	}
	public record InquiryPdfDocument(String inquiryNo, String fileName, String contentType, String base64Content) {
	}
	public record InquirySummary(String inquiryNo, String applicationNo, String policyNo, String inquiryStatus,
			String inquiryStatusDescription, LocalDateTime issuedAt, String createdBy, LocalDateTime createdAt,
			String updatedBy, LocalDateTime updatedAt, String reviewerId, LocalDateTime reviewedAt) {
	}
	public record InquiryPage(List<InquirySummary> items, long totalItems, int page, int pageSize, int totalPages) {
	}
}

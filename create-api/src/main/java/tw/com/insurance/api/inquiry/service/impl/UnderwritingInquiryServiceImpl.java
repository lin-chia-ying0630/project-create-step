package tw.com.insurance.api.inquiry.service.impl;

import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryDetail;
import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryItem;
import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryPdfDocument;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.inquiry.persistence.UnderwritingInquiryMapper;
import tw.com.insurance.api.inquiry.service.UnderwritingInquiryService;
import tw.com.insurance.api.inquiry.domain.UnderwritingInquiryErrorCode;
import tw.com.insurance.api.newcontract.domain.NewContractApplicationStatus;
import tw.com.insurance.api.inquiry.util.UnderwritingInquiryPdfGenerator;

@Service
public class UnderwritingInquiryServiceImpl implements UnderwritingInquiryService {
	private final UnderwritingInquiryMapper mapper;
	public UnderwritingInquiryServiceImpl(UnderwritingInquiryMapper mapper) {
		this.mapper = mapper;
	}
	public InquiryDetail find(String query) {
		Map<String, Object> row = mapper.findInquiry(query);
		if (row == null)
			throw new BusinessException(UnderwritingInquiryErrorCode.NOT_FOUND);
		String inquiryNo = text(row, "inquiry_no"), uwStatus = text(row, "underwriting_status"),
				inquiryStatus = text(row, "inquiry_status");
		var applicationStatus = NewContractApplicationStatus.fromCode(text(row, "application_status"));
		String policyNo = nullableText(row, "policy_no");
		if (policyNo == null || policyNo.isBlank())
			throw new BusinessException(UnderwritingInquiryErrorCode.POLICY_NUMBER_NOT_RESERVED);
		List<InquiryItem> items = mapper
				.findItems(inquiryNo).stream().map(i -> new InquiryItem(text(i, "rule_code"), text(i, "rule_name"),
						text(i, "item_message"), nullableText(i, "response_text"), dateTime(i.get("responded_at"))))
				.toList();
		return new InquiryDetail(inquiryNo, text(row, "application_no"), policyNo, text(row, "underwriting_case_no"),
				number(row, "application_revision"), maskReference(text(row, "applicant_customer_id")),
				maskName(text(row, "applicant_name")), maskReference(text(row, "insured_customer_id")),
				maskName(text(row, "insured_name")), text(row, "product_code"), date(row.get("application_date")),
				date(row.get("requested_effective_date")), text(row, "currency_code"),
				decimal(row, "sum_assured_amount"), decimal(row, "premium_amount"), applicationStatus.stageCode(),
				applicationStatus.stageDescription(), applicationStatus.contractStatusCode(),
				applicationStatus.contractStatusDescription(), uwStatus, uwDescription(uwStatus),
				nullableText(row, "underwriting_decision_code"),
				decisionDescription(nullableText(row, "underwriting_decision_code")), inquiryStatus,
				inquiryDescription(inquiryStatus), dateTime(row.get("issued_at")), dateTime(row.get("resolved_at")),
				items);
	}
	public InquiryPdfDocument createPdf(String query) {
		InquiryDetail detail = find(query);
		byte[] content = UnderwritingInquiryPdfGenerator.generate(detail);
		return new InquiryPdfDocument(detail.inquiryNo(), "核保照會單-" + detail.inquiryNo() + ".pdf", "application/pdf",
				Base64.getEncoder().encodeToString(content));
	}
	private static String uwDescription(String code) {
		return switch (code) {
			case "UW" -> "等待照會回覆";
			case "US" -> "照會完成";
			case "AS" -> "承保完成";
			case "NS" -> "拒保完成";
			case "DECLINED" -> "拒保";
			default -> code;
		};
	}
	private static String inquiryDescription(String code) {
		return switch (code) {
			case "UP" -> "照會受理";
			case "UW" -> "等待照會回覆";
			case "US" -> "照會完成";
			case "UN" -> "照會取消";
			case "UD" -> "照會撤回";
			default -> code;
		};
	}
	private static String decisionDescription(String code) {
		if (code == null)
			return "待補件後重新核保";
		return switch (code) {
			case "PENDING_DOCUMENT" -> "文件不完整，待補件";
			case "DECLINED" -> "不予承保";
			default -> code;
		};
	}
	private static String text(Map<String, Object> row, String key) {
		return String.valueOf(row.get(key));
	}
	private static String nullableText(Map<String, Object> row, String key) {
		Object value = row.get(key);
		return value == null ? null : String.valueOf(value);
	}
	private static long number(Map<String, Object> row, String key) {
		return ((Number) row.get(key)).longValue();
	}
	private static LocalDateTime dateTime(Object value) {
		return value == null ? null : value instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) value;
	}
	private static LocalDate date(Object value) {
		return value instanceof Date d ? d.toLocalDate() : (LocalDate) value;
	}
	private static BigDecimal decimal(Map<String, Object> row, String key) {
		return (BigDecimal) row.get(key);
	}
	private static String maskReference(String value) {
		if (value == null || value.length() < 9)
			return "****";
		return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
	}
	private static String maskName(String value) {
		if (value == null || value.isBlank())
			return "***";
		if (value.length() == 1)
			return "*";
		if (value.length() == 2)
			return value.substring(0, 1) + "*";
		return value.substring(0, 1) + "*".repeat(Math.min(4, value.length() - 2))
				+ value.substring(value.length() - 1);
	}
}

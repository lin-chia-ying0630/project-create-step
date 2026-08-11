package tw.com.insurance.api.inquiry.service;

import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryDetail;
import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryPdfDocument;

public interface UnderwritingInquiryService {
	InquiryDetail find(String query);
	InquiryPdfDocument createPdf(String query);
}

package tw.com.insurance.api.inquiry.controller;

import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryDetail;
import static tw.com.insurance.api.inquiry.dto.UnderwritingInquiryDtos.InquiryPdfDocument;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.inquiry.service.UnderwritingInquiryService;

@Validated
@RestController
@RequestMapping("/api/v1/new-contract/underwriting-inquiries")
public class UnderwritingInquiryController {
	private final UnderwritingInquiryService service;
	public UnderwritingInquiryController(UnderwritingInquiryService service) {
		this.service = service;
	}
	@GetMapping("/{query}")
	ResponseBodyDto<InquiryDetail> find(@PathVariable @NotBlank String query) {
		return ResponseBodyDto.success("核保照會單查詢成功", service.find(query));
	}
	@GetMapping("/{query}/pdf")
	ResponseBodyDto<InquiryPdfDocument> pdf(@PathVariable @NotBlank String query) {
		return ResponseBodyDto.success("核保照會單 PDF 產生成功", service.createPdf(query));
	}
}

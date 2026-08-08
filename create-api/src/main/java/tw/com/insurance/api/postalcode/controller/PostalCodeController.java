package tw.com.insurance.api.postalcode.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.postalcode.dto.PostalCodeAreaDto;
import tw.com.insurance.api.postalcode.service.PostalCodeService;

@Validated
@RestController
public class PostalCodeController {
	private final PostalCodeService service;

	public PostalCodeController(PostalCodeService service) {
		this.service = service;
	}

	@GetMapping({"/api/postal-codes/{postalCode}", "/api/v1/postal-codes/{postalCode}"})
	ResponseBodyDto<PostalCodeAreaDto> findArea(
			@PathVariable @NotBlank @Pattern(regexp = "[0-9]{3}([0-9]{3})?", message = "postalCode 必須為 3 或 6 碼數字") String postalCode) {
		return ResponseBodyDto.success("郵遞區號查詢成功", service.findArea(postalCode));
	}
}

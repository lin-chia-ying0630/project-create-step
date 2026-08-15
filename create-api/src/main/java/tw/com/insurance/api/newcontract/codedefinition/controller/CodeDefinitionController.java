package tw.com.insurance.api.newcontract.codedefinition.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.common.dto.PageResult;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionDto;
import tw.com.insurance.api.newcontract.codedefinition.dto.CodeDefinitionTableDto;
import tw.com.insurance.api.newcontract.codedefinition.service.CodeDefinitionService;

/** 提供前端取得資料庫維護的動態代碼與繁體中文說明。 */
@Validated
@RestController
@RequestMapping("/api/v1/new-contract/code-definitions")
public class CodeDefinitionController {
	private final CodeDefinitionService service;

	public CodeDefinitionController(CodeDefinitionService service) {
		this.service = service;
	}

	/** 查詢目前可供使用者選擇的代碼表。 */
	@GetMapping("/tables")
	ResponseBodyDto<List<CodeDefinitionTableDto>> findActiveTables() {
		return ResponseBodyDto.success("代碼表清單查詢成功", service.findActiveTables());
	}

	/** 依群組及欄位查詢目前啟用的代碼選項。 */
	@GetMapping("/{codeGroup}/{codeField}")
	ResponseBodyDto<List<CodeDefinitionDto>> findActiveOptions(
			@PathVariable @NotBlank @Pattern(regexp = "[a-z0-9-]{1,64}") String codeGroup,
			@PathVariable @NotBlank @Pattern(regexp = "[a-z0-9_]{1,64}") String codeField) {
		return ResponseBodyDto.success("代碼對照查詢成功", service.findActiveOptions(codeGroup, codeField));
	}

	/** 依群組及欄位分頁查詢目前啟用的代碼，避免查詢畫面一次載入完整代碼表。 */
	@GetMapping("/{codeGroup}/{codeField}/page")
	ResponseBodyDto<PageResult<CodeDefinitionDto>> findActiveOptionPage(
			@PathVariable @NotBlank @Pattern(regexp = "[a-z0-9-]{1,64}") String codeGroup,
			@PathVariable @NotBlank @Pattern(regexp = "[a-z0-9_]{1,64}") String codeField,
			@RequestParam(defaultValue = "1") @Min(1) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
			@RequestParam(defaultValue = "") @Size(max = 100) String query) {
		return ResponseBodyDto.success("代碼對照分頁查詢成功",
				service.findActiveOptionPage(codeGroup, codeField, page, pageSize, query));
	}
}

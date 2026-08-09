package tw.com.insurance.api.newcontract.productdefinition.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.service.ProductDefinitionService;

/** 提供保單登打取得目前有效的保險商品定義。 */
@RestController
@RequestMapping("/api/v1/new-contract/product-definitions")
public class ProductDefinitionController {
	private final ProductDefinitionService service;

	public ProductDefinitionController(ProductDefinitionService service) {
		this.service = service;
	}

	/** 查詢已完成上架且目前生效的商品下拉選項。 */
	@GetMapping("/active")
	ResponseBodyDto<List<ProductDefinitionDto>> findActiveProducts() {
		return ResponseBodyDto.success("保險商品定義查詢成功", service.findActiveProducts());
	}
}

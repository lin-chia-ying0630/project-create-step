package tw.com.insurance.api.newcontract.productdefinition.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.newcontract.domain.NewContractErrorCode;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.persistence.ProductDefinitionMapper;
import tw.com.insurance.api.newcontract.productdefinition.service.ProductDefinitionService;

/** 以商品定義檔作為商品類型與投保限制的唯一裁決來源。 */
@Service
public class ProductDefinitionServiceImpl implements ProductDefinitionService {
	private final ProductDefinitionMapper mapper;

	public ProductDefinitionServiceImpl(ProductDefinitionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<ProductDefinitionDto> findActiveProducts() {
		return mapper.findActiveProducts();
	}

	@Override
	public ProductDefinitionDto requireActiveProduct(String productCode, String productVersion) {
		ProductDefinitionDto product = mapper.findActiveProduct(productCode, productVersion);
		if (product == null)
			throw new BusinessException(NewContractErrorCode.INVALID_PRODUCT);
		return product;
	}
}

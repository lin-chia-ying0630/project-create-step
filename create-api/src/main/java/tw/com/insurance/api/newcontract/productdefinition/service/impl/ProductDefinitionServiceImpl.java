package tw.com.insurance.api.newcontract.productdefinition.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.common.BusinessException;
import tw.com.insurance.api.newcontract.domain.NewContractErrorCode;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionChangeRequest;
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

	@Override
	public boolean supportsPaymentMode(String productCode, String productVersion, String paymentModeCode) {
		return mapper.supportsPaymentMode(productCode, productVersion, paymentModeCode);
	}

	@Override
	public boolean supportsRider(String baseProductCode, String baseProductVersion, String riderProductCode,
			String riderProductVersion) {
		return mapper.supportsRider(baseProductCode, baseProductVersion, riderProductCode, riderProductVersion);
	}

	@Override
	@org.springframework.transaction.annotation.Transactional
	public ProductDefinitionDto applyChange(ProductDefinitionChangeRequest request, String reviewerId) {
		validateChange(request);
		mapper.upsertProduct(request, reviewerId);
		mapper.deletePaymentModes(request.productCode(), request.productVersion());
		request.paymentModeCodes().stream().distinct().forEach(
				code -> mapper.insertPaymentMode(request.productCode(), request.productVersion(), code, reviewerId));
		mapper.deleteRiderRules(request.productCode(), request.productVersion());
		if ("BASE".equals(request.coverageItemType()))
			request.compatibleRiders().forEach(rider -> mapper.insertRiderRule(request.productCode(),
					request.productVersion(), rider.productCode(), rider.productVersion(), reviewerId));
		return mapper.findProduct(request.productCode(), request.productVersion());
	}

	/** 驗證商品固定代碼與上下限，避免不完整內容寫入正式定義。 */
	private void validateChange(ProductDefinitionChangeRequest request) {
		if (!List.of("L", "I").contains(request.productTypeCode())
				|| !List.of("BASE", "RIDER").contains(request.coverageItemType())
				|| !List.of("P", "S", "C", "W").contains(request.productStatus())
				|| ("I".equals(request.productTypeCode()) && request.productRiskLevelCode() == null)
				|| (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())))
			throw new BusinessException(NewContractErrorCode.INVALID_PRODUCT);
	}
}

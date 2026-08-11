package tw.com.insurance.api.newcontract.productdefinition.service;

import java.util.List;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionChangeRequest;

/** 提供保單登打與後端驗證共用的有效商品定義。 */
public interface ProductDefinitionService {
	/** 取得目前可登打的全部商品。 */
	List<ProductDefinitionDto> findActiveProducts();

	/** 取得指定商品；商品不存在、未上架或已停售時拒絕。 */
	ProductDefinitionDto requireActiveProduct(String productCode, String productVersion);

	/** 確認商品支援要保書繳別。 */
	boolean supportsPaymentMode(String productCode, String productVersion, String paymentModeCode);

	/** 確認附約可搭配目前主約版本。 */
	boolean supportsRider(String baseProductCode, String baseProductVersion, String riderProductCode,
			String riderProductVersion);

	/** 覆核核准後套用正式商品版本及其搭配規則。 */
	ProductDefinitionDto applyChange(ProductDefinitionChangeRequest request, String reviewerId);
}

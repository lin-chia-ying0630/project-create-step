package tw.com.insurance.api.newcontract.productdefinition.service;

import java.util.List;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;

/** 提供保單登打與後端驗證共用的有效商品定義。 */
public interface ProductDefinitionService {
	/** 取得目前可登打的全部商品。 */
	List<ProductDefinitionDto> findActiveProducts();

	/** 取得指定商品；商品不存在、未上架或已停售時拒絕。 */
	ProductDefinitionDto requireActiveProduct(String productCode, String productVersion);
}

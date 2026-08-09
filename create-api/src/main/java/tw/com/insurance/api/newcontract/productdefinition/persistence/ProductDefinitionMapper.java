package tw.com.insurance.api.newcontract.productdefinition.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;

/** 定義保險商品定義檔查詢契約；SQL 統一放在 Mapper XML。 */
@Mapper
public interface ProductDefinitionMapper {
	/** 查詢目前已完成上架且在有效期間內的商品。 */
	List<ProductDefinitionDto> findActiveProducts();

	/** 依商品代碼及版本取得目前有效的商品定義。 */
	ProductDefinitionDto findActiveProduct(@Param("productCode") String productCode,
			@Param("productVersion") String productVersion);
}

package tw.com.insurance.api.newcontract.productdefinition.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionDto;
import tw.com.insurance.api.newcontract.productdefinition.dto.ProductDefinitionChangeRequest;

/** 定義保險商品定義檔查詢契約；SQL 統一放在 Mapper XML。 */
@Mapper
public interface ProductDefinitionMapper {
	/** 查詢目前已完成上架且在有效期間內的商品。 */
	List<ProductDefinitionDto> findActiveProducts();

	/** 依商品代碼及版本取得目前有效的商品定義。 */
	ProductDefinitionDto findActiveProduct(@Param("productCode") String productCode,
			@Param("productVersion") String productVersion);
	ProductDefinitionDto findProduct(@Param("productCode") String productCode,
			@Param("productVersion") String productVersion);

	/** 驗證商品是否允許指定繳別。 */
	boolean supportsPaymentMode(@Param("productCode") String productCode,
			@Param("productVersion") String productVersion, @Param("paymentModeCode") String paymentModeCode);

	/** 驗證主約與附約版本是否允許搭配。 */
	boolean supportsRider(@Param("baseProductCode") String baseProductCode,
			@Param("baseProductVersion") String baseProductVersion, @Param("riderProductCode") String riderProductCode,
			@Param("riderProductVersion") String riderProductVersion);

	/** 新增或更新覆核核准的正式商品版本。 */
	int upsertProduct(@Param("request") ProductDefinitionChangeRequest request, @Param("reviewerId") String reviewerId);

	/** 重建商品繳別前先清除舊明細。 */
	int deletePaymentModes(@Param("productCode") String productCode, @Param("productVersion") String productVersion);

	/** 新增一筆商品可使用繳別。 */
	int insertPaymentMode(@Param("productCode") String productCode, @Param("productVersion") String productVersion,
			@Param("paymentModeCode") String paymentModeCode, @Param("reviewerId") String reviewerId);

	/** 重建主約附約搭配規則前先清除舊明細。 */
	int deleteRiderRules(@Param("productCode") String productCode, @Param("productVersion") String productVersion);

	/** 新增一筆主約與附約搭配規則。 */
	int insertRiderRule(@Param("baseProductCode") String baseProductCode,
			@Param("baseProductVersion") String baseProductVersion, @Param("riderProductCode") String riderProductCode,
			@Param("riderProductVersion") String riderProductVersion, @Param("reviewerId") String reviewerId);
}

import { request } from '../../../shared/api/apiClient'
import type { ProductDefinitionOption } from '../types/productDefinition'

export const productDefinitionApi = {
  /** 取得目前完成上架且有效的商品，前端不自行維護商品類型。 */
  findActiveProducts(): Promise<ProductDefinitionOption[]> {
    return request('/api/v1/new-contract/product-definitions/active')
  },
}

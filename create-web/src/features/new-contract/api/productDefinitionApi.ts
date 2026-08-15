import { request } from '../../../shared/api/apiClient'
import type { ProductDefinitionOption } from '../types/productDefinition'

let activeProductCache: ProductDefinitionOption[] | null = null
let activeProductRequest: Promise<ProductDefinitionOption[]> | null = null

/** 清除有效商品快取，供商品覆核生效或使用者登出後重新取得權威資料。 */
export function clearActiveProductCache() {
  activeProductCache = null
  activeProductRequest = null
}

export const productDefinitionApi = {
  /** 取得目前完成上架且有效的商品，前端不自行維護商品類型。 */
  findActiveProducts(): Promise<ProductDefinitionOption[]> {
    if (activeProductCache) return Promise.resolve(activeProductCache)
    if (activeProductRequest) return activeProductRequest
    activeProductRequest = request<ProductDefinitionOption[]>(
      '/api/v1/new-contract/product-definitions/active',
    )
      .then((result) => {
        activeProductCache = result
        return result
      })
      .finally(() => {
        activeProductRequest = null
      })
    return activeProductRequest
  },
}

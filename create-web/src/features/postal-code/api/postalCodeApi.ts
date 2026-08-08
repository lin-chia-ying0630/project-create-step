import { request } from '../../../shared/api/apiClient'
import type { PostalCodeArea } from '../types/postalCode'

export const postalCodeApi = {
  find(postalCode: string): Promise<PostalCodeArea> {
    return request(`/api/postal-codes/${encodeURIComponent(postalCode)}`)
  },
}

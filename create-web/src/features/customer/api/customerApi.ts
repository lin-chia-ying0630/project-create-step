import { request } from '../../../shared/api/apiClient'
import type { CreateCustomerRequest, CustomerResult } from '../types/customer'
export const customerApi = {
  create(command: CreateCustomerRequest): Promise<CustomerResult> {
    return request('/api/v1/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': crypto.randomUUID() },
      body: JSON.stringify(command),
    })
  },
}

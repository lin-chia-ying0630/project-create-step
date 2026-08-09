import { request } from '../../../shared/api/apiClient'
import type { ReviewSubmissionResult } from '../../review/types/review'
import type { CreateCustomerRequest } from '../types/customer'
export const customerApi = {
  /** 送出客戶建立覆核案件，核准前不建立客戶主檔。 */
  create(command: CreateCustomerRequest): Promise<ReviewSubmissionResult> {
    return request('/api/v1/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': crypto.randomUUID() },
      body: JSON.stringify(command),
    })
  },
}

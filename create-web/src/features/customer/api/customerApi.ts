import { get, request } from '../../../shared/api/apiClient'
import type { ReviewSubmissionResult } from '../../review/types/review'
import type { CreateCustomerRequest, CustomerPage } from '../types/customer'
export const customerApi = {
  /** 取得不含敏感身分與聯絡資料的客戶摘要清單。 */
  findPage(page = 1, pageSize = 10, sort = 'customerId,asc'): Promise<CustomerPage> {
    const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize), sort })
    return get(`/api/v1/customers?${params.toString()}`)
  },
  /** 送出客戶建立覆核案件，核准前不建立客戶主檔。 */
  create(command: CreateCustomerRequest): Promise<ReviewSubmissionResult> {
    return request('/api/v1/customers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': crypto.randomUUID() },
      body: JSON.stringify(command),
    })
  },
}

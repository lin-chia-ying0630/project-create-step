import { request } from '../../../shared/api/apiClient'
import type {
  ApplicationQueryPage,
  ApplicationQueryResult,
  CreateApplicationRequest,
  PaymentInstrumentValidationRequest,
  PaymentInstrumentValidationResult,
} from '../types/applicationEntry'
import type { ReviewSubmissionResult } from '../../review/types/review'
export const applicationEntryApi = {
  validatePaymentInstrument(
    command: PaymentInstrumentValidationRequest,
  ): Promise<PaymentInstrumentValidationResult> {
    return request('/api/v1/new-contract/payment-instruments/validate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    })
  },
  create(command: CreateApplicationRequest): Promise<ReviewSubmissionResult> {
    return request('/api/v1/new-contract/applications', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    })
  },
  reservePolicyNumber(applicationNo: string): Promise<ReviewSubmissionResult> {
    return request(
      `/api/v1/new-contract/applications/${encodeURIComponent(applicationNo)}/policy-number`,
      { method: 'POST' },
    )
  },
  query(query: string): Promise<ApplicationQueryResult[]> {
    return request(`/api/v1/new-contract/applications/query/${encodeURIComponent(query)}`)
  },
  queryPage(
    query = '',
    page = 1,
    pageSize = 10,
    sort = 'applicationNo,asc',
  ): Promise<ApplicationQueryPage> {
    const params = new URLSearchParams({
      query,
      page: String(page),
      pageSize: String(pageSize),
      sort,
    })
    return request(`/api/v1/new-contract/applications/query?${params.toString()}`)
  },
}

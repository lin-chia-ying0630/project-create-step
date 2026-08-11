import { request } from '../../../shared/api/apiClient'
import type {
  ApplicationQueryResult,
  CreateApplicationRequest,
  CreateApplicationResult,
  PolicyNumberReservationResult,
} from '../types/applicationEntry'
export const applicationEntryApi = {
  create(command: CreateApplicationRequest): Promise<CreateApplicationResult> {
    return request('/api/v1/new-contract/applications', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    })
  },
  reservePolicyNumber(applicationNo: string): Promise<PolicyNumberReservationResult> {
    return request(
      `/api/v1/new-contract/applications/${encodeURIComponent(applicationNo)}/policy-number`,
      { method: 'POST' },
    )
  },
  query(query: string): Promise<ApplicationQueryResult[]> {
    return request(`/api/v1/new-contract/applications/query/${encodeURIComponent(query)}`)
  },
}

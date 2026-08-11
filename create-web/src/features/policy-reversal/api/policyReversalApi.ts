import { get, request } from '../../../shared/api/apiClient'
import type {
  PolicyReversalPage,
  PolicyReversalPreview,
  PolicyReversalRequest,
} from '../types/policyReversal'
import type { ReviewSubmissionResult } from '../../review/types/review'

export const policyReversalApi = {
  list(page = 1, pageSize = 10, sort = 'policyNo,asc'): Promise<PolicyReversalPage> {
    const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize), sort })
    return get(`/api/v1/new-contract/policy-reversals?${params.toString()}`)
  },
  preview(policyNo: string): Promise<PolicyReversalPreview> {
    return get(`/api/v1/new-contract/policy-reversals/${encodeURIComponent(policyNo)}/preview`)
  },
  execute(command: PolicyReversalRequest, idempotencyKey: string): Promise<ReviewSubmissionResult> {
    return request('/api/v1/new-contract/policy-reversals', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': idempotencyKey },
      body: JSON.stringify(command),
    })
  },
}

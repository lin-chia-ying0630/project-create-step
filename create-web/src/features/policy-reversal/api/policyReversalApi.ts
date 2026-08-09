import { get, request } from '../../../shared/api/apiClient'
import type { PolicyReversalPreview, PolicyReversalRequest } from '../types/policyReversal'
import type { ReviewSubmissionResult } from '../../review/types/review'

export const policyReversalApi = {
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

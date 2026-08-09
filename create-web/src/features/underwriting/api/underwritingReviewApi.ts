import { get, request } from '../../../shared/api/apiClient'
import type { ReviewSubmissionResult } from '../../review/types/review'
import type {
  UnderwritingDecisionRequest,
  UnderwritingOutcomeOption,
  UnderwritingReviewPreview,
} from '../types/underwritingReview'

export const underwritingReviewApi = {
  find(query: string): Promise<UnderwritingReviewPreview> {
    return get(`/api/v1/new-contract/underwriting-reviews/${encodeURIComponent(query)}`)
  },
  outcomes(): Promise<UnderwritingOutcomeOption[]> {
    return get('/api/v1/new-contract/underwriting-reviews/outcomes')
  },
  submit(command: UnderwritingDecisionRequest): Promise<ReviewSubmissionResult> {
    return request('/api/v1/new-contract/underwriting-reviews/decisions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    })
  },
}

import { get, request } from '../../../shared/api/apiClient'
import type { ReviewSubmissionResult } from '../../review/types/review'
import type {
  UnderwritingDecisionRequest,
  UnderwritingOutcomeOption,
  UnderwritingReviewPage,
  UnderwritingReviewPreview,
} from '../types/underwritingReview'

export const underwritingReviewApi = {
  list(
    query = '',
    page = 1,
    pageSize = 10,
    sort = 'applicationNo,asc',
  ): Promise<UnderwritingReviewPage> {
    const params = new URLSearchParams({
      query,
      page: String(page),
      pageSize: String(pageSize),
      sort,
    })
    return get(`/api/v1/new-contract/underwriting-reviews?${params.toString()}`)
  },
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

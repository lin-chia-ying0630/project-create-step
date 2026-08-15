import { get, request } from '../../../shared/api/apiClient'
import type { ReviewSubmissionResult } from '../../review/types/review'
import type {
  UnderwritingDecisionRequest,
  UnderwritingOutcomeOption,
  UnderwritingReviewPage,
  UnderwritingReviewPreview,
} from '../types/underwritingReview'

let outcomeCache: UnderwritingOutcomeOption[] | null = null
let outcomeRequest: Promise<UnderwritingOutcomeOption[]> | null = null

/** 清除核保結果快取，供核保結果定義異動或使用者登出後重新載入。 */
export function clearUnderwritingOutcomeCache() {
  outcomeCache = null
  outcomeRequest = null
}

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
    if (outcomeCache) return Promise.resolve(outcomeCache)
    if (outcomeRequest) return outcomeRequest
    outcomeRequest = get<UnderwritingOutcomeOption[]>(
      '/api/v1/new-contract/underwriting-reviews/outcomes',
    )
      .then((result) => {
        outcomeCache = result
        return result
      })
      .finally(() => {
        outcomeRequest = null
      })
    return outcomeRequest
  },
  submit(command: UnderwritingDecisionRequest): Promise<ReviewSubmissionResult> {
    return request('/api/v1/new-contract/underwriting-reviews/decisions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(command),
    })
  },
}

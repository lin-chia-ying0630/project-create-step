import { get, request } from '../../../shared/api/apiClient'
import type { ReviewDetail, ReviewOperationOption, ReviewPageResult } from '../types/review'

export const reviewApi = {
  /** 取得後端唯一維護的覆核功能英文代碼與繁中名稱。 */
  findOperationOptions(): Promise<ReviewOperationOption[]> {
    return get('/api/v1/reviews/operation-types')
  },
  /** 取得覆核待辦清單。 */
  findPending(
    page = 1,
    pageSize = 10,
    sort = 'reviewId,asc',
    query = '',
    operationType = '',
    signal?: AbortSignal,
  ): Promise<ReviewPageResult> {
    const params = new URLSearchParams({
      status: 'P',
      page: String(page),
      pageSize: String(pageSize),
      sort,
    })
    if (query) params.set('query', query)
    if (operationType) params.set('operationType', operationType)
    return get(`/api/v1/reviews?${params.toString()}`, { signal })
  },
  /** 取得單筆覆核案件與解密後明細。 */
  findById(reviewId: string): Promise<ReviewDetail> {
    return get(`/api/v1/reviews/${encodeURIComponent(reviewId)}`)
  },
  /** 核准案件並在後端交易中套用正式異動。 */
  approve(reviewId: string, comment: string): Promise<ReviewDetail> {
    return request(`/api/v1/reviews/${encodeURIComponent(reviewId)}/approve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Request-ID': crypto.randomUUID() },
      body: JSON.stringify({ comment: comment || null }),
    })
  },
  /** 退回案件且不修改正式資料。 */
  reject(reviewId: string, comment: string): Promise<ReviewDetail> {
    return request(`/api/v1/reviews/${encodeURIComponent(reviewId)}/reject`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Request-ID': crypto.randomUUID() },
      body: JSON.stringify({ comment: comment || null }),
    })
  },
}

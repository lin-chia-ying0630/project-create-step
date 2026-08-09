import { get, request } from '../../../shared/api/apiClient'
import type { ReviewDetail, ReviewPageResult } from '../types/review'

export const reviewApi = {
  /** 取得覆核待辦清單。 */
  findPending(page = 1, pageSize = 20): Promise<ReviewPageResult> {
    return get(`/api/v1/reviews?status=PENDING&page=${page}&pageSize=${pageSize}`)
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

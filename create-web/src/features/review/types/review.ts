/** 覆核 feature 的 transport contract。 */
export interface ReviewSubmissionResult {
  reviewId: string
  operationType: string
  operationDescription: string
  businessKey: string
  reviewStatus: string
  submittedAt: string
}

export interface ReviewSummary extends ReviewSubmissionResult {
  makerId: string
  reviewerId: string | null
  reviewedAt: string | null
}

export interface ReviewDetail extends ReviewSummary {
  reviewComment: string | null
  payload: Record<string, unknown>
  result: Record<string, unknown> | null
}

export interface ReviewPageResult {
  items: ReviewSummary[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

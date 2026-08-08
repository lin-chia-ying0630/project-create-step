export interface UnderwritingBatchRequest {
  applicationNo: string
  requestedBusinessDate: string
}

export interface UnderwritingBatchRequestResult {
  batchRequestId: string
  applicationNo: string
  requestStatus: string
  scheduledAt: string
}

export interface UnderwritingBatchExecutionSummary {
  batchExecutionId: string
  businessDate: string
  executionStatus: string
  startedAt: string
  completedAt: string | null
  totalCount: number
  approvedCount: number
  inquiryCount: number
  failedCount: number
}

/** 新契約批次承保作業 feature 的 transport contract。 */
export interface UnderwritingBatchRequest {
  applicationNo: string
  executionDate: string
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

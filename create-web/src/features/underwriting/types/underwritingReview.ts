export interface UnderwritingReviewPreview {
  applicationNo: string
  policyNo: string | null
  underwritingCaseNo: string
  currentStageCode: string
  currentDecisionCode: string | null
  currentContractStatusCode: string | null
  recordVersion: number
}

export interface UnderwritingDecisionRequest {
  applicationNo: string
  decisionCode: 'DC' | 'PO' | 'CN'
  reasonCode: string
  reasonDescription: string
  expectedVersion: number
}

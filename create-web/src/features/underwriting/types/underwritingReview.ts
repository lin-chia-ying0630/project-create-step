export interface UnderwritingReviewPreview {
  applicationNo: string
  policyNo: string | null
  underwritingCaseNo: string
  productCode: string
  applicationDate: string
  requestedEffectiveDate: string
  currencyCode: string
  sumAssuredAmount: string
  premiumAmount: string
  currentStageCode: string
  currentStageDescription: string
  currentDecisionCode: string | null
  currentContractStatusCode: string | null
  currentContractStatusDescription: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  reviewerId: string | null
  reviewedAt: string | null
  recordVersion: number
}

export interface UnderwritingReviewSummary {
  applicationNo: string
  policyNo: string | null
  underwritingCaseNo: string
  productCode: string
  applicationDate: string
  requestedEffectiveDate: string
  currentStageCode: string
  currentStageDescription: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  reviewerId: string | null
  reviewedAt: string | null
}

export interface UnderwritingReviewPage {
  items: UnderwritingReviewSummary[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

export interface UnderwritingOutcomeOption {
  decisionCode: string
  decisionDescription: string
  stageCode: string
  stageDescription: string
  contractStatusCode: string
  contractStatusDescription: string
  insurable: boolean
}

export interface UnderwritingDecisionRequest {
  applicationNo: string
  decisionCode: string
  reasonCode: string
  reasonDescription: string
  expectedVersion: number
}

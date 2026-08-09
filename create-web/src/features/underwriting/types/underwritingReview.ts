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
  recordVersion: number
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

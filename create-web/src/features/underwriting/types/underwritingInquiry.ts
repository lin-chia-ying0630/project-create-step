/** 核保照會 feature 的 transport contract。 */
export interface InquiryItem {
  ruleCode: string
  ruleName: string
  itemMessage: string
  responseText: string | null
  respondedAt: string | null
}
export interface InquiryDetail {
  inquiryNo: string
  applicationNo: string
  policyNo: string
  underwritingCaseNo: string
  applicationRevision: number
  applicantCustomerReference: string
  applicantNameMasked: string
  insuredCustomerReference: string
  insuredNameMasked: string
  productCode: string
  applicationDate: string
  requestedEffectiveDate: string
  currencyCode: string
  sumAssuredAmount: string
  premiumAmount: string
  newContractStage: string
  newContractStageDescription: string
  contractStatus: string
  contractStatusDescription: string
  underwritingStatus: string
  underwritingStatusDescription: string
  decisionCode: string | null
  decisionDescription: string
  inquiryStatus: string
  inquiryStatusDescription: string
  issuedAt: string
  resolvedAt: string | null
  items: InquiryItem[]
}
export interface InquiryPdfDocument {
  inquiryNo: string
  fileName: string
  contentType: string
  base64Content: string
}
export interface InquirySummary {
  inquiryNo: string
  applicationNo: string
  policyNo: string | null
  inquiryStatus: string
  inquiryStatusDescription: string
  issuedAt: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  reviewerId: string | null
  reviewedAt: string | null
}
export interface InquiryPage {
  items: InquirySummary[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

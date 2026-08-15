/** 承保撤回 feature 的 transport contract。 */
export interface PolicyReversalPreview {
  policyNo: string
  applicationNo: string
  underwritingCaseNo: string
  policyStatus: string
  newContractStageCode: string
  newContractStageNameEn: string
  newContractStageDescriptionZhTw: string
  underwritingStatus: string
  effectiveDate: string
  policyVersion: number
  applicationVersion: number
  underwritingVersion: number
  deleteCounts: Record<string, number>
  blockers: string[]
  confirmToken: string
}

export interface PolicyReversalRequest {
  policyNo: string
  reasonCode: string
  reasonDescription: string
  expectedPolicyVersion: number
  expectedApplicationVersion: number
  expectedUnderwritingVersion: number
  confirmToken: string
}

export interface PolicyReversalResult {
  reversalAuditId: string
  policyNo: string
  applicationNo: string
  newContractStageCode: string
  newContractStageNameEn: string
  newContractStageDescriptionZhTw: string
  underwritingStatus: string
}
export interface PolicyReversalSummary {
  policyNo: string
  applicationNo: string
  productCode: string
  contractStatusCode: string
  effectiveDate: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  reviewerId: string | null
  reviewedAt: string | null
}
export interface PolicyReversalPage {
  items: PolicyReversalSummary[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

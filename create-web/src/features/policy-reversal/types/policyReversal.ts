/** 承保撤回 feature 的 transport contract。 */
export interface PolicyReversalPreview {
  policyNo: string
  applicationNo: string
  underwritingCaseNo: string
  policyStatus: string
  applicationStatus: string
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
  applicationStatus: string
  underwritingStatus: string
}

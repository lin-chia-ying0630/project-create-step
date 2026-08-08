/** 首期保費 feature 的 transport contract。 */
export interface PremiumDuePreview {
  applicationNo: string
  premiumDueId: string
  currencyCode: string
  calculatedPremiumAmount: string
  calculationRuleVersion: string
  dueStatus: string
  dueStatusDescription: string
}
export interface RemittanceSlipRequest {
  applicationNo: string
  paymentReceiptNo: string
  paymentChannelCode: string
  collectionReference: string
  currencyCode: string
  receivedAmount: string
  receivedAt: string
  payerRoleCode: string | null
}
export interface PremiumMatchResult {
  premiumMatchId: string
  matchStatus: string
  matchStatusDescription: string
  expectedAmount: string
  actualAmount: string
  differenceAmount: string
  mayUnderwrite: boolean
}

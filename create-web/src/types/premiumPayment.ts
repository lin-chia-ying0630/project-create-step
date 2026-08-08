export interface PremiumDuePreview {
  applicationNo: string
  premiumDueId: string
  currencyCode: string
  calculatedPremiumAmount: string
  calculationRuleVersion: string
  dueStatus: string
}
export interface RemittanceSlipRequest {
  applicationNo: string; remittanceSlipNo: string; paymentMethodCode: string; paymentReference: string
  currencyCode: string; actualPaidAmount: string; paidAt: string; payerRelationshipCode: string | null
}
export interface PremiumMatchResult {
  premiumMatchId: string; matchStatus: string; expectedAmount: string; actualAmount: string
  differenceAmount: string; mayUnderwrite: boolean
}

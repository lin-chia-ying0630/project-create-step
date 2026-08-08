export interface CreateApplicationRequest {
  applicationNo: string; applicationDate: string; channelCode: string; productCode: string
  productVersion: string; currencyCode: string; sumAssuredAmount: string; premiumAmount: string
  paymentModeCode: string; requestedEffectiveDate: string
}
export interface CreateApplicationResult {
  applicationId: string; applicationNo: string; applicationStatus: string; premiumDueId: string
  calculatedPremiumAmount: string; currencyCode: string
}

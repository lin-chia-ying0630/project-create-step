export interface CreateCustomerRequest {
  identityTypeCode:string; identityNo:string; customerName:string; genderCode:string; birthDate:string
  nationalityCode:string; residencyCountryCode:string; mobilePhone:string; email:string
  postalCode:string; contactAddress:string; occupationCode:string; sourceOfFundsCode:string
  insurancePurposeCode:string; consentVersion:string
}
export interface CustomerResult {
  customerId:string; identityTypeCode:string; maskedIdentityNo:string; customerName:string
  genderCode:string; birthDate:string; maskedMobilePhone:string; maskedEmail:string
  recordStatus:string; recordVersion:number
}

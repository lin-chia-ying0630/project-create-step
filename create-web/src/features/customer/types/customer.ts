/** 客戶建檔 feature 的 transport contract。 */
export interface CodeDefinitionOption {
  code: string
  description: string
}

export interface CreateCustomerRequest {
  customerTypeCode: 'PERSON' | 'ORGANIZATION'
  identityTypeCode: string
  identityNo: string
  customerName: string
  genderCode: string | null
  birthDate: string | null
  establishmentDate: string | null
  responsiblePersonName: string | null
  industryCode: string | null
  organizationTypeCode: string | null
  nationalityCode: string
  residencyCountryCode: string
  mobilePhone: string
  email: string
  postalCode: string
  contactAddress: string
  occupationCode: string
  sourceOfFundsCode: string
  insurancePurposeCode: string
  consentVersion: string
}
export interface CustomerResult {
  customerId: string
  customerTypeCode: string
  identityTypeCode: string
  maskedIdentityNo: string
  customerName: string
  genderCode: string | null
  birthDate: string | null
  maskedMobilePhone: string
  maskedEmail: string
  recordStatus: string
  recordVersion: number
}

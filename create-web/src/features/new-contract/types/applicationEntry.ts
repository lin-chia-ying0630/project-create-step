/** 新契約 feature 的 transport contract。 */
export interface CreateApplicationRequest {
  applicationNo: string
  applicationDate: string
  channelCode: string
  branchCode: string
  insuranceAgentCode: string
  applicantCustomerId: string
  insuredCustomerId: string
  applicantRelationshipToInsuredCode: string
  currencyCode: string
  paymentModeCode: string
  requestedEffectiveDate: string
  electronicPolicy: boolean
  fundsSourceCode: string
  insurancePurposeCode: string
  coverages: CoverageInput[]
  beneficiaries: BeneficiaryInput[]
  healthDisclosures: HealthDisclosureInput[]
  truthfulDisclosureConfirmed: boolean
  personalDataConsentConfirmed: boolean
  termsReviewedConfirmed: boolean
  applicantSignatureConfirmed: boolean
  insuredSignatureConfirmed: boolean
  signatureMethod: string
}
export interface CoverageInput {
  coverageItemType: 'BASE' | 'RIDER'
  productCode: string
  productVersion: string
  sumAssuredAmount: string
  premiumAmount: string
  coverageTermYears: number | null
  premiumPaymentTermYears: number | null
}
export interface BeneficiaryInput {
  beneficiaryTypeCode: string
  beneficiaryCustomerId: string | null
  beneficiaryDesignationCode: string | null
  priorityNo: number
  allocationPercentage: string | null
  relationshipToInsuredCode: string | null
}
export interface HealthDisclosureInput {
  questionCode: string
  answerCode: 'YES' | 'NO'
  supplementalDetail: string | null
}
export interface CreateApplicationResult {
  applicationId: string
  applicationNo: string
  applicationStatus: string
  premiumDueId: string
  calculatedPremiumAmount: string
  currencyCode: string
}
export interface PolicyNumberReservationResult {
  applicationNo: string
  policyNo: string
  policyNumberStatus: 'ASSIGNED'
  reservedAt: string
}
export interface CoverageDetail {
  coverageItemSeq: number
  coverageItemType: string
  productCode: string
  productVersion: string
  currencyCode: string
  sumAssuredAmount: string
  premiumAmount: string
  coverageTermYears: number | null
  premiumPaymentTermYears: number | null
  requestedEffectiveDate: string
}
export interface BeneficiaryDetail {
  beneficiaryTypeCode: string
  beneficiarySeq: number
  beneficiaryCustomerReference: string | null
  beneficiaryDesignationCode: string | null
  priorityNo: number
  allocationPercentage: string | null
  relationshipToInsuredCode: string | null
}
export interface HealthDisclosureDetail {
  questionSetCode: string
  questionSetVersion: string
  questionCode: string
  answerCode: string
  supplementalDetail: string | null
  answeredAt: string
  confirmedAt: string | null
}
export interface DeclarationDetail {
  declarationTypeCode: string
  declarationVersion: string
  confirmedByPartyRole: string
  confirmationMethod: string
  confirmedAt: string
}
export interface SignatureDetail {
  signerPartyRole: string
  signerCustomerReference: string
  signatureMethod: string
  signedAt: string
  verifiedAt: string | null
}
export interface CustomerContactDetail {
  partyRoleCode: string
  contactTypeCode: string
  contactValueMasked: string
  primaryContact: boolean
  verificationStatus: string
  effectiveFrom: string
  effectiveTo: string | null
}
export interface CustomerAddressDetail {
  partyRoleCode: string
  addressTypeCode: string
  postalCode: string
  addressMasked: string
  effectiveFrom: string
  effectiveTo: string | null
}
export interface PremiumDueDetail {
  premiumDueId: string
  currencyCode: string
  calculatedPremiumAmount: string
  calculationRuleVersion: string
  dueStatus: string
  calculatedAt: string
}
export interface ApplicationQueryResult {
  applicationNo: string
  policyNo: string | null
  policyNumberStatus: 'NOT_ASSIGNED' | 'ASSIGNED'
  applicationStatus: string
  applicationStatusDescription: string
  applicationDate: string
  newContractStage: string
  newContractStageDescription: string
  contractStatus: string
  contractStatusDescription: string
  requestedEffectiveDate: string
  channelCode: string
  branchCode: string | null
  insuranceAgentCode: string | null
  productCode: string
  productVersion: string
  paymentModeCode: string
  currencyCode: string
  sumAssuredAmount: string
  premiumAmount: string
  applicantCustomerId: string
  applicantName: string
  insuredCustomerId: string
  insuredName: string
  coverages: CoverageDetail[]
  beneficiaries: BeneficiaryDetail[]
  healthDisclosures: HealthDisclosureDetail[]
  declarations: DeclarationDetail[]
  signatures: SignatureDetail[]
  customerContacts: CustomerContactDetail[]
  customerAddresses: CustomerAddressDetail[]
  premiumDues: PremiumDueDetail[]
}
export interface ApplicationQuerySummary {
  applicationNo: string
  policyNo: string | null
  productCode: string
  applicationStatus: string
  applicationStatusDescription: string
  applicationDate: string
  requestedEffectiveDate: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  reviewerId: string | null
  reviewedAt: string | null
}
export interface ApplicationQueryPage {
  items: ApplicationQuerySummary[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

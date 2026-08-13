export interface ProductDefinitionOption {
  productCode: string
  productVersion: string
  productName: string
  productTypeCode: 'L' | 'I'
  productTypeDescription: string
  coverageItemType: 'BASE' | 'RIDER'
  currencyCode: string
  productRiskLevelCode: string | null
  minimumEntryAge: number | null
  maximumEntryAge: number | null
  minimumSumAssured: string | null
  maximumSumAssured: string | null
  minimumPremium: string | null
  minimumCoverageTermYears: number | null
  maximumCoverageTermYears: number | null
  minimumPaymentTermYears: number | null
  maximumPaymentTermYears: number | null
  effectiveFrom: string
  effectiveTo: string | null
  investmentProduct: boolean
}

export interface ProductDefinitionOption {
  productCode: string
  productVersion: string
  productName: string
  productTypeCode: 'L' | 'I'
  productTypeDescription: string
  coverageItemType: 'BASE' | 'RIDER'
  currencyCode: string
  minimumEntryAge: number | null
  maximumEntryAge: number | null
  minimumSumAssured: string | null
  maximumSumAssured: string | null
  minimumPremium: string | null
  effectiveFrom: string
  effectiveTo: string | null
  investmentProduct: boolean
}

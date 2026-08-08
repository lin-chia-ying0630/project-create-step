/** 郵遞區號 feature 的 transport contract。 */
export interface PostalCodeArea {
  postalCode: string
  zipCode3: string
  city: string
  district: string
  addressPrefix: string
  halfWidthAddressPrefix: string | null
}

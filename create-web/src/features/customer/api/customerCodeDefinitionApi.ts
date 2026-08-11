import { get } from '../../../shared/api/apiClient'
import type { CodeDefinitionOption } from '../types/customer'

const basePath = '/api/v1/new-contract/code-definitions/customer-kyc'

/** 取得新契約客戶 KYC 欄位的資料庫代碼選項。 */
function findOptions(codeField: string): Promise<CodeDefinitionOption[]> {
  return get<CodeDefinitionOption[]>(`${basePath}/${codeField}`)
}

export const customerCodeDefinitionApi = {
  /** 取得職業代碼與繁體中文說明。 */
  findOccupations(): Promise<CodeDefinitionOption[]> {
    return findOptions('occupation_code')
  },
  /** 取得資金來源代碼與繁體中文說明。 */
  findSourcesOfFunds(): Promise<CodeDefinitionOption[]> {
    return findOptions('source_of_funds_code')
  },
  /** 取得投保目的代碼與繁體中文說明。 */
  findInsurancePurposes(): Promise<CodeDefinitionOption[]> {
    return findOptions('insurance_purpose_code')
  },
}

import { get } from './apiClient'
import type { CodeDefinitionOption, CodeDefinitionTableOption } from '../types/codeDefinition'

const basePath = '/api/v1/new-contract/code-definitions'

export const codeDefinitionApi = {
  /** 取得目前有效、可供畫面下拉選擇的代碼表。 */
  findActiveTables(): Promise<CodeDefinitionTableOption[]> {
    return get<CodeDefinitionTableOption[]>(`${basePath}/tables`)
  },

  /** 依資料庫代碼群組與欄位查詢目前生效的代碼與中文。 */
  findActiveOptions(codeGroup: string, codeField: string): Promise<CodeDefinitionOption[]> {
    return get<CodeDefinitionOption[]>(
      `${basePath}/${encodeURIComponent(codeGroup)}/${encodeURIComponent(codeField)}`,
    )
  },
}

import { get } from '../../../shared/api/apiClient'
import type { CodeDefinitionOption } from '../types/codeDefinition'

const basePath = '/api/v1/new-contract/code-definitions'

/** 依資料庫代碼群組與欄位查詢目前生效的對照內容。 */
function findActiveOptions(codeGroup: string, codeField: string): Promise<CodeDefinitionOption[]> {
  return get<CodeDefinitionOption[]>(
    `${basePath}/${encodeURIComponent(codeGroup)}/${encodeURIComponent(codeField)}`,
  )
}

export const codeDefinitionApi = { findActiveOptions }

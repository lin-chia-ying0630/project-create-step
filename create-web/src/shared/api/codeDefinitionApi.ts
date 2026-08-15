import { get } from './apiClient'
import type { CodeDefinitionOption, CodeDefinitionTableOption } from '../types/codeDefinition'
import type { PageResult } from '../types/common'

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

  /** 依資料庫代碼群組與欄位只取得查詢畫面需要的一頁資料。 */
  findActiveOptionPage(
    codeGroup: string,
    codeField: string,
    page = 1,
    pageSize = 10,
    query = '',
    signal?: AbortSignal,
  ): Promise<PageResult<CodeDefinitionOption>> {
    const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
    if (query) params.set('query', query)
    return get<PageResult<CodeDefinitionOption>>(
      `${basePath}/${encodeURIComponent(codeGroup)}/${encodeURIComponent(codeField)}/page?${params.toString()}`,
      { signal },
    )
  },
}

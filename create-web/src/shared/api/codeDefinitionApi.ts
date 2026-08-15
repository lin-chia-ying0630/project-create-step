import { get } from './apiClient'
import type { CodeDefinitionOption, CodeDefinitionTableOption } from '../types/codeDefinition'
import type { PageResult } from '../types/common'

const basePath = '/api/v1/new-contract/code-definitions'
const MAX_PAGE_CACHE_ENTRIES = 100

const optionCache = new Map<string, CodeDefinitionOption[]>()
const pageCache = new Map<string, PageResult<CodeDefinitionOption>>()
const requestCache = new Map<string, Promise<unknown>>()
let tableCache: CodeDefinitionTableOption[] | null = null

/** 合併同一時間的相同請求，成功後才寫入共用快取，錯誤不保留。 */
function loadOnce<T>(key: string, loader: () => Promise<T>): Promise<T> {
  const pending = requestCache.get(key) as Promise<T> | undefined
  if (pending) return pending
  const request = loader().finally(() => requestCache.delete(key))
  requestCache.set(key, request)
  return request
}

/** 限制搜尋分頁快取大小，避免長時間輸入不同關鍵字造成記憶體持續增加。 */
function cachePage(key: string, value: PageResult<CodeDefinitionOption>) {
  if (pageCache.size >= MAX_PAGE_CACHE_ENTRIES) {
    const oldestKey = pageCache.keys().next().value
    if (oldestKey !== undefined) pageCache.delete(oldestKey)
  }
  pageCache.set(key, value)
}

/** 登出或代碼維護完成後清除快取，下一次使用時重新取得後端權威資料。 */
export function clearCodeDefinitionCache() {
  tableCache = null
  optionCache.clear()
  pageCache.clear()
  requestCache.clear()
}

export const codeDefinitionApi = {
  /** 取得目前有效、可供畫面下拉選擇的代碼表。 */
  findActiveTables(): Promise<CodeDefinitionTableOption[]> {
    if (tableCache) return Promise.resolve(tableCache)
    return loadOnce('tables', async () => {
      const tables = await get<CodeDefinitionTableOption[]>(`${basePath}/tables`)
      tableCache = tables
      return tables
    })
  },

  /** 依資料庫代碼群組與欄位查詢目前生效的代碼與中文。 */
  findActiveOptions(codeGroup: string, codeField: string): Promise<CodeDefinitionOption[]> {
    const key = `${codeGroup}::${codeField}`
    const cached = optionCache.get(key)
    if (cached) return Promise.resolve(cached)
    return loadOnce(`options::${key}`, async () => {
      const options = await get<CodeDefinitionOption[]>(
        `${basePath}/${encodeURIComponent(codeGroup)}/${encodeURIComponent(codeField)}`,
      )
      optionCache.set(key, options)
      return options
    })
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
    const normalizedQuery = query.trim()
    const key = `${codeGroup}::${codeField}::${page}::${pageSize}::${normalizedQuery}`
    const cached = pageCache.get(key)
    if (cached) return Promise.resolve(cached)
    const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize) })
    if (normalizedQuery) params.set('query', normalizedQuery)
    const loader = async () => {
      const result = await get<PageResult<CodeDefinitionOption>>(
        `${basePath}/${encodeURIComponent(codeGroup)}/${encodeURIComponent(codeField)}/page?${params.toString()}`,
        { signal },
      )
      cachePage(key, result)
      return result
    }
    return signal ? loader() : loadOnce(`page::${key}`, loader)
  },
}

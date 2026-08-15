import { afterEach, describe, expect, it, vi } from 'vitest'
import { clearCodeDefinitionCache, codeDefinitionApi } from './codeDefinitionApi'

const pageResponse = {
  items: [],
  totalItems: 0,
  page: 1,
  pageSize: 10,
  totalPages: 0,
}

describe('codeDefinitionApi cache', () => {
  afterEach(() => {
    clearCodeDefinitionCache()
    vi.unstubAllGlobals()
  })

  it('相同代碼分頁查詢只向後端取得一次', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({
        success: true,
        message: '查詢成功',
        errorCode: null,
        errorMessage: null,
        data: pageResponse,
      }),
    )
    vi.stubGlobal('fetch', fetchMock)

    const first = codeDefinitionApi.findActiveOptionPage('customer-kyc', 'occupation_code')
    const second = codeDefinitionApi.findActiveOptionPage('customer-kyc', 'occupation_code')

    await expect(Promise.all([first, second])).resolves.toEqual([pageResponse, pageResponse])
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('清除快取後下一次查詢會重新取得後端資料', async () => {
    const fetchMock = vi.fn().mockImplementation(() =>
      Promise.resolve(
        Response.json({
          success: true,
          message: '查詢成功',
          errorCode: null,
          errorMessage: null,
          data: [],
        }),
      ),
    )
    vi.stubGlobal('fetch', fetchMock)

    await codeDefinitionApi.findActiveTables()
    clearCodeDefinitionCache()
    await codeDefinitionApi.findActiveTables()

    expect(fetchMock).toHaveBeenCalledTimes(2)
  })
})

import { afterEach, describe, expect, it, vi } from 'vitest'
import { clearActiveProductCache, productDefinitionApi } from './productDefinitionApi'

describe('productDefinitionApi cache', () => {
  afterEach(() => {
    clearActiveProductCache()
    vi.unstubAllGlobals()
  })

  it('相同有效商品請求只向後端取得一次', async () => {
    const products = [{ productCode: 'P001' }]
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({ success: true, data: products }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(
      Promise.all([
        productDefinitionApi.findActiveProducts(),
        productDefinitionApi.findActiveProducts(),
      ]),
    ).resolves.toEqual([products, products])
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
})

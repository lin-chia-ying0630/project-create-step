import { afterEach, describe, expect, it, vi } from 'vitest'
import { get } from './apiClient'

describe('apiClient', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('後端回傳非 JSON 錯誤頁時顯示繁中服務訊息', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        new Response('<html>Bad Gateway</html>', {
          status: 502,
          headers: { 'Content-Type': 'text/html' },
        }),
      ),
    )

    await expect(get('/api/example')).rejects.toMatchObject({
      errorCode: 'SYS-9001',
      message: '服務暫時無法使用，請稍後重試',
      status: 502,
    })
  })

  it('瀏覽器拒絕建立請求時不洩漏底層英文例外', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('The string did not match')))

    await expect(get('/api/example')).rejects.toMatchObject({
      errorCode: 'SYS-9001',
      message: '服務暫時無法使用，請稍後重試',
      status: 0,
    })
  })

  it('後端業務錯誤維持 ResponseBodyDto 的錯誤契約', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        Response.json(
          {
            success: false,
            message: null,
            errorCode: 'TEST-0001',
            errorMessage: '測試錯誤',
            data: null,
          },
          { status: 422 },
        ),
      ),
    )

    await expect(get('/api/example')).rejects.toMatchObject({
      errorCode: 'TEST-0001',
      message: '測試錯誤',
      status: 422,
    })
  })
})

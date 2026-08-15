import { describe, expect, it } from 'vitest'
import { resolvePortalUrl, shouldRedirectToPortal } from './portalUrl'

describe('resolvePortalUrl', () => {
  it('使用明確設定的本機統一入口網址', () => {
    expect(resolvePortalUrl(' http://localhost:5174/ ', 'https://example.code.run')).toBe(
      'http://localhost:5174/',
    )
  })

  it('正式環境未設定時回到同源 HTTPS，不附加本機開發埠', () => {
    expect(resolvePortalUrl(undefined, 'https://example.code.run')).toBe(
      'https://example.code.run/',
    )
  })

  it('正式環境誤設同主機 5174 時改回同源 HTTPS', () => {
    expect(
      resolvePortalUrl(
        'https://p01--create-web--kkj9gmg9xdcp.code.run:5174',
        'https://p01--create-web--kkj9gmg9xdcp.code.run',
      ),
    ).toBe('https://p01--create-web--kkj9gmg9xdcp.code.run/')
  })

  it('無效入口設定不會中斷導向，改回同源 HTTPS', () => {
    expect(resolvePortalUrl('not-a-url', 'https://example.code.run')).toBe(
      'https://example.code.run/',
    )
  })
})

describe('shouldRedirectToPortal', () => {
  it.each([401, 403])('HTTP %i 應導向統一登入入口', (status) => {
    expect(shouldRedirectToPortal(status)).toBe(true)
  })

  it.each([0, 400, 404, 500, 502, 503])('HTTP %i 不得觸發同源重新導向迴圈', (status) => {
    expect(shouldRedirectToPortal(status)).toBe(false)
  })
})

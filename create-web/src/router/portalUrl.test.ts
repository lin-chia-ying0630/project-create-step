import { describe, expect, it } from 'vitest'
import { resolvePortalUrl } from './portalUrl'

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
})

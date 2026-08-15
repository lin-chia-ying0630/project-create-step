/** 解析未登入時的統一入口網址；公開站不得因錯誤設定而導向同主機的本機開發埠。 */
export function resolvePortalUrl(
  configuredPortalUrl: string | undefined,
  currentOrigin: string,
): string {
  const configured = configuredPortalUrl?.trim()
  if (!configured) return `${currentOrigin}/`

  try {
    const configuredUrl = new URL(configured)
    const currentUrl = new URL(currentOrigin)
    const usesInvalidPublicDevelopmentPort =
      currentUrl.protocol === 'https:' &&
      configuredUrl.hostname === currentUrl.hostname &&
      configuredUrl.port === '5174'
    return usesInvalidPublicDevelopmentPort ? `${currentUrl.origin}/` : configuredUrl.toString()
  } catch {
    return `${currentOrigin}/`
  }
}

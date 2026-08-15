/** 解析未登入時的統一入口網址；正式環境未設定入口時維持同源，避免誤用本機開發埠。 */
export function resolvePortalUrl(
  configuredPortalUrl: string | undefined,
  currentOrigin: string,
): string {
  const configured = configuredPortalUrl?.trim()
  return configured || `${currentOrigin}/`
}

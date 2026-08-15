import { afterEach, describe, expect, it, vi } from 'vitest'
import { clearUnderwritingOutcomeCache, underwritingReviewApi } from './underwritingReviewApi'

describe('underwritingReviewApi outcome cache', () => {
  afterEach(() => {
    clearUnderwritingOutcomeCache()
    vi.unstubAllGlobals()
  })

  it('相同核保結果請求只向後端取得一次', async () => {
    const outcomes = [{ decisionCode: 'APPROVED' }]
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({ success: true, data: outcomes }),
    )
    vi.stubGlobal('fetch', fetchMock)

    await expect(
      Promise.all([underwritingReviewApi.outcomes(), underwritingReviewApi.outcomes()]),
    ).resolves.toEqual([outcomes, outcomes])
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })
})

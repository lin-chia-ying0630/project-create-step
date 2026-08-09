import { get, request } from '../../../shared/api/apiClient'
import type { PremiumDuePreview, RemittanceSlipRequest } from '../types/premiumPayment'
import type { ReviewSubmissionResult } from '../../review/types/review'
export const premiumPaymentApi = {
  getDue(applicationNo: string): Promise<PremiumDuePreview> {
    return get(
      `/api/v1/new-contract/applications/${encodeURIComponent(applicationNo)}/initial-premium`,
    )
  },
  /** 新增送金單並送交覆核，核准後由後端完成銷帳。 */
  createRemittanceSlip(
    command: RemittanceSlipRequest,
    key: string,
  ): Promise<ReviewSubmissionResult> {
    return request('/api/v1/new-contract/remittance-slips', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key },
      body: JSON.stringify(command),
    })
  },
}

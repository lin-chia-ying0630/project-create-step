import { get, request } from '../../../shared/api/apiClient'
import type {
  PremiumDuePreview,
  PremiumMatchResult,
  RemittanceSlipRequest,
} from '../types/premiumPayment'
export const premiumPaymentApi = {
  getDue(applicationNo: string): Promise<PremiumDuePreview> {
    return get(
      `/api/v1/new-contract/applications/${encodeURIComponent(applicationNo)}/initial-premium`,
    )
  },
  registerAndReconcile(command: RemittanceSlipRequest, key: string): Promise<PremiumMatchResult> {
    return request('/api/v1/new-contract/initial-premium-payments/reconcile', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key },
      body: JSON.stringify(command),
    })
  },
}

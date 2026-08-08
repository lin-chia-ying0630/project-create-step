import { get, request } from './apiClient'
import type { PremiumDuePreview, PremiumMatchResult, RemittanceSlipRequest } from '../types/premiumPayment'
export const premiumPaymentApi = {
  getDue(applicationNo: string): Promise<PremiumDuePreview> { return get(`/api/v1/new-contract/applications/${encodeURIComponent(applicationNo)}/initial-premium`) },
  submitAndMatch(command: RemittanceSlipRequest, key: string): Promise<PremiumMatchResult> {
    return request('/api/v1/new-contract/remittance-slips/match', { method:'POST', headers:{'Content-Type':'application/json','Idempotency-Key':key}, body:JSON.stringify(command) })
  }
}

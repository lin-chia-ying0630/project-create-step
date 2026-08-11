import { get } from '../../../shared/api/apiClient'
import type { InquiryDetail, InquiryPdfDocument } from '../types/underwritingInquiry'
export const underwritingInquiryApi = {
  find(query: string): Promise<InquiryDetail> {
    return get(`/api/v1/new-contract/underwriting-inquiries/${encodeURIComponent(query)}`)
  },
  pdf(query: string): Promise<InquiryPdfDocument> {
    return get(`/api/v1/new-contract/underwriting-inquiries/${encodeURIComponent(query)}/pdf`)
  },
}

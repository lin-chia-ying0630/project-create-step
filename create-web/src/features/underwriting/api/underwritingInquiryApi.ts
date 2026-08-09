import { get } from '../../../shared/api/apiClient'
import type { InquiryDetail, InquiryPage, InquiryPdfDocument } from '../types/underwritingInquiry'
export const underwritingInquiryApi = {
  list(page = 1, pageSize = 10, sort = 'inquiryNo,asc'): Promise<InquiryPage> {
    const params = new URLSearchParams({ page: String(page), pageSize: String(pageSize), sort })
    return get(`/api/v1/new-contract/underwriting-inquiries?${params.toString()}`)
  },
  find(query: string): Promise<InquiryDetail> {
    return get(`/api/v1/new-contract/underwriting-inquiries/${encodeURIComponent(query)}`)
  },
  pdf(query: string): Promise<InquiryPdfDocument> {
    return get(`/api/v1/new-contract/underwriting-inquiries/${encodeURIComponent(query)}/pdf`)
  },
}

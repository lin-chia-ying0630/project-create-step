import { request } from './apiClient'
import type { CreateApplicationRequest, CreateApplicationResult } from '../types/applicationEntry'
export const applicationEntryApi = {
  create(command: CreateApplicationRequest): Promise<CreateApplicationResult> {
    return request('/api/v1/new-contract/applications', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(command)
    })
  }
}

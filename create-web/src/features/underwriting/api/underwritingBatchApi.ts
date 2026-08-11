import { get, request } from '../../../shared/api/apiClient'
import type {
  UnderwritingBatchExecutionSummary,
  UnderwritingBatchRequest,
  UnderwritingBatchRequestResult,
} from '../types/underwritingBatch'

export const underwritingBatchApi = {
  enqueue(
    command: UnderwritingBatchRequest,
    idempotencyKey: string,
  ): Promise<UnderwritingBatchRequestResult> {
    return request('/api/v1/new-contract/underwriting-batch/requests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': idempotencyKey },
      body: JSON.stringify(command),
    })
  },
  latestExecutions(): Promise<UnderwritingBatchExecutionSummary[]> {
    return get('/api/v1/new-contract/underwriting-batch/executions?page=1&pageSize=20')
  },
}

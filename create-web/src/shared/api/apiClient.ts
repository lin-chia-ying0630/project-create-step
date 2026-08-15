import type { ResponseBodyDto } from '../types/common'

const SERVICE_UNAVAILABLE_MESSAGE = '服務暫時無法使用，請稍後重試'

export class ApiError extends Error {
  constructor(
    public readonly errorCode: string,
    message: string,
    public readonly status: number,
  ) {
    super(message)
  }
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? ''}${path}`, {
      ...init,
      credentials: 'same-origin',
    })
  } catch {
    throw new ApiError('SYS-9001', SERVICE_UNAVAILABLE_MESSAGE, 0)
  }

  let body: ResponseBodyDto<T>
  try {
    body = (await response.json()) as ResponseBodyDto<T>
  } catch {
    throw new ApiError('SYS-9001', SERVICE_UNAVAILABLE_MESSAGE, response.status)
  }

  if (!response.ok || !body.success || body.data === null) {
    throw new ApiError(
      body.errorCode ?? 'SYS-9001',
      body.errorMessage ?? '系統連線異常，請稍後再試',
      response.status,
    )
  }
  return body.data
}

export function get<T>(path: string, init?: RequestInit): Promise<T> {
  return request<T>(path, init)
}

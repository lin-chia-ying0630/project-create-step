import type { ResponseBodyDto } from '../types/common'

export class ApiError extends Error {
  constructor(public readonly errorCode: string, message: string, public readonly status: number) { super(message) }
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? ''}${path}`, init)
  const body = await response.json() as ResponseBodyDto<T>
  if (!response.ok || !body.success || body.data === null) {
    throw new ApiError(body.errorCode ?? 'SYS-9001', body.errorMessage ?? '系統連線異常，請稍後再試', response.status)
  }
  return body.data
}

export function get<T>(path: string): Promise<T> {
  return request<T>(path)
}

export interface ResponseBodyDto<T> {
  success: boolean
  message: string | null
  errorCode: string | null
  errorMessage: string | null
  data: T | null
}

export interface PageResult<T> {
  items: T[]
  totalItems: number
  page: number
  pageSize: number
  totalPages: number
}

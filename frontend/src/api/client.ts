export type ApiErrorBody = {
  code?: string
  message?: string
  fieldErrors?: Record<string, string>
  traceId?: string
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: ApiErrorBody,
  ) {
    super(body.message || `Request failed with status ${status}`)
  }
}

type RequestOptions = Omit<RequestInit, 'body'> & { body?: unknown }

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers)
  const method = (options.method || 'GET').toUpperCase()
  if (!['GET', 'HEAD'].includes(method)) {
    const csrf = await fetch('/api/v1/auth/csrf', { credentials: 'include' })
    if (!csrf.ok) throw new ApiError(csrf.status, { message: 'ไม่สามารถยืนยันความปลอดภัยของคำขอได้' })
    const token = await csrf.json() as { headerName: string; token: string }
    headers.set(token.headerName, token.token)
  }
  let body: BodyInit | undefined
  if (options.body instanceof FormData) {
    body = options.body
  } else if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
    body = JSON.stringify(options.body)
  }
  const response = await fetch(path, { ...options, method, headers, body, credentials: 'include' })
  if (!response.ok) {
    const error = await response.json().catch(() => ({})) as ApiErrorBody
    throw new ApiError(response.status, error)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  get: <T>(path: string, signal?: AbortSignal) => request<T>(path, { signal }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
  put: <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT', body }),
  patch: <T>(path: string, body: unknown) => request<T>(path, { method: 'PATCH', body }),
}

export async function download(path: string): Promise<Blob> {
  const response = await fetch(path, { credentials: 'include' })
  if (!response.ok) throw new ApiError(response.status, { message: 'ดาวน์โหลดไฟล์ไม่สำเร็จ' })
  return response.blob()
}

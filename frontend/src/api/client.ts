import type { ApiError } from '../types'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export class RequestError extends Error {
  status: number
  code: string
  constructor(error: ApiError) { super(error.message || 'The request could not be completed.'); this.status = error.status; this.code = error.error }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = sessionStorage.getItem('jobtrakt.token')
  const headers = new Headers(options.headers)
  if (!(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers })
  } catch {
    throw new Error('JobTrakt could not reach the API. Check that the backend is running.')
  }
  if (response.status === 401 && !path.startsWith('/auth/login') && !path.startsWith('/auth/register')) {
    window.dispatchEvent(new Event('jobtrakt:unauthorized'))
  }
  if (!response.ok) {
    let body: ApiError | undefined
    try { body = await response.json() as ApiError } catch { /* Empty error response. */ }
    throw new RequestError(body ?? { status: response.status, error: 'REQUEST_FAILED', message: `Request failed (${response.status}).`, path })
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export function jsonBody(body: unknown): string { return JSON.stringify(body) }

import type { ErrorResponse } from './contracts'
import { getToken } from './authToken'

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export { BASE_URL }

export async function http<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken()

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const error: ErrorResponse = await response.json()
      throw error
    }
    const text = await response.text()
    const err = new Error(text)
    Object.assign(err, { status: response.status, message: text })
    throw err
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return await response.text() as unknown as T
}

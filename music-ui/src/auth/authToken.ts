export interface TokenPayload {
  sub:    string
  userId: string
  role:   string
  scope:  string[]
  iss:    string
  exp:    number
  iat:    number
}

export function getTokenPayload(): TokenPayload | null {
  const token = getToken()
  if (!token) return null
  try {
    return JSON.parse(atob(token.split('.')[1])) as TokenPayload
  } catch {
    return null
  }
}

export function isAdmin(): boolean {
  return getTokenPayload()?.role === 'ADMIN'
}

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

export function getToken(): string | null {
  const token = localStorage.getItem('access_token')
  if (!token) return null
  if (isTokenExpired(token)) {
    localStorage.removeItem('access_token')
    return null
  }
  return token
}

export function saveToken(token: string): void {
  localStorage.setItem('access_token', token)
}

export function clearToken(): void {
  localStorage.removeItem('access_token')
}

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

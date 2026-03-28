import type { RegisterParams, LoginParams, AuthResponse } from './contracts'
import { http } from './httpClient'

export const authApi = {

  register(params: RegisterParams): Promise<string> {
    return http<string>('/api/v0/auth/register', {
      method: 'POST',
      body: JSON.stringify(params),
    })
  },

  login(params: LoginParams): Promise<AuthResponse> {
    return http<AuthResponse>('/api/v0/auth/login', {
      method: 'POST',
      body: JSON.stringify(params),
    })
  },

}

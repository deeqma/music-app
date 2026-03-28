import type { UserProfileDto } from './contracts'
import { http } from './httpClient'

export const userApi = {

  getProfile(): Promise<UserProfileDto> {
    return http<UserProfileDto>('/api/v0/user/profile')
  },

  deleteAccount(): Promise<void> {
    return http<void>('/api/v0/user/delete', { method: 'DELETE' })
  },

}

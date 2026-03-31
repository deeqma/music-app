import type { PlaylistSummaryDto, PlaylistDetailsDto, SongDto, SongFilterParams, CreatePlaylistParams } from './contracts'
import { http } from './httpClient'

export const playlistApi = {

  create(params: CreatePlaylistParams): Promise<PlaylistSummaryDto> {
    return http<PlaylistSummaryDto>('/api/v0/playlists', {
      method: 'POST',
      body: JSON.stringify(params),
    })
  },

  update(id: number, params: CreatePlaylistParams): Promise<PlaylistDetailsDto> {
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${id}`, {
      method: 'PUT',
      body: JSON.stringify(params),
    })
  },

  getAll(): Promise<PlaylistSummaryDto[]> {
    return http<PlaylistSummaryDto[]>('/api/v0/playlists')
  },

  getById(id: number, params: SongFilterParams = {}): Promise<PlaylistDetailsDto> {
    const query = new URLSearchParams()
    if (params.genre)                  query.set('genre',      params.genre)
    if (params.artistName)             query.set('artistName', params.artistName)
    if (params.album)                  query.set('album',      params.album)
    if (params.yearFrom)               query.set('yearFrom',   String(params.yearFrom))
    if (params.yearTo)                 query.set('yearTo',     String(params.yearTo))
    if (params.page !== undefined)     query.set('page',       String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize',   String(params.pageSize))
    const qs = query.toString()
    const qsSuffix = qs ? `?${qs}` : ''
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${id}${qsSuffix}`)
  },

  addSong(playlistId: number, songId: number): Promise<PlaylistDetailsDto> {
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${playlistId}/songs/${songId}`, {
      method: 'POST',
    })
  },

  removeSong(playlistId: number, songId: number): Promise<PlaylistDetailsDto> {
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${playlistId}/songs/${songId}`, {
      method: 'DELETE',
    })
  },

  searchSongs(id: number, query: string, shareToken?: string, page = 0, pageSize = 15): Promise<SongDto[]> {
    const params = new URLSearchParams({ query, page: String(page), pageSize: String(pageSize) })
    if (shareToken) params.set('shareToken', shareToken)
    return http<SongDto[]>(`/api/v0/playlists/${id}/search?${params.toString()}`)
  },

  generateShareToken(id: number): Promise<PlaylistDetailsDto> {
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${id}/share`, { method: 'POST' })
  },

  getByShareToken(token: string, params: SongFilterParams = {}): Promise<PlaylistDetailsDto> {
    const query = new URLSearchParams()
    if (params.genre)                  query.set('genre',      params.genre)
    if (params.artistName)             query.set('artistName', params.artistName)
    if (params.album)                  query.set('album',      params.album)
    if (params.yearFrom)               query.set('yearFrom',   String(params.yearFrom))
    if (params.yearTo)                 query.set('yearTo',     String(params.yearTo))
    if (params.page !== undefined)     query.set('page',       String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize',   String(params.pageSize))
    const qs = query.toString()
    const qsSuffix = qs ? `?${qs}` : ''
    return http<PlaylistDetailsDto>(`/api/v0/playlists/share/${token}${qsSuffix}`)
  },

  delete(id: number): Promise<void> {
    return http<void>(`/api/v0/playlists/${id}`, { method: 'DELETE' })
  },

  toggleVisibility(id: number, isPrivate: boolean): Promise<PlaylistDetailsDto> {
    return http<PlaylistDetailsDto>(`/api/v0/playlists/${id}/private?value=${isPrivate}`, {
      method: 'PATCH',
    })
  },

}

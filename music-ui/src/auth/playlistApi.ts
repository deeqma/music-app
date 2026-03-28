import type { PlaylistDto, SongDto, SongFilterParams, CreatePlaylistParams } from './contracts'
import { http } from './httpClient'

export const playlistApi = {

  create(params: CreatePlaylistParams): Promise<PlaylistDto> {
    return http<PlaylistDto>('/api/v0/playlists', {
      method: 'POST',
      body: JSON.stringify(params),
    })
  },

  getAll(): Promise<PlaylistDto[]> {
    return http<PlaylistDto[]>('/api/v0/playlists')
  },

  getById(id: number, params: SongFilterParams = {}): Promise<PlaylistDto> {
    const query = new URLSearchParams()
    if (params.genre)                  query.set('genre',      params.genre)
    if (params.artistName)             query.set('artistName', params.artistName)
    if (params.album)                  query.set('album',      params.album)
    if (params.yearFrom)               query.set('yearFrom',   String(params.yearFrom))
    if (params.yearTo)                 query.set('yearTo',     String(params.yearTo))
    if (params.page !== undefined)     query.set('page',       String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize',   String(params.pageSize))
    const qs = query.toString()
    return http<PlaylistDto>(`/api/v0/playlists/${id}${qs ? `?${qs}` : ''}`)
  },

  addSong(playlistId: number, songId: number): Promise<PlaylistDto> {
    return http<PlaylistDto>(`/api/v0/playlists/${playlistId}/songs/${songId}`, {
      method: 'POST',
    })
  },

  removeSong(playlistId: number, songId: number): Promise<PlaylistDto> {
    return http<PlaylistDto>(`/api/v0/playlists/${playlistId}/songs/${songId}`, {
      method: 'DELETE',
    })
  },

  searchSongs(id: number, query: string, shareToken?: string, page = 0, pageSize = 15): Promise<SongDto[]> {
    const params = new URLSearchParams({ query, page: String(page), pageSize: String(pageSize) })
    if (shareToken) params.set('shareToken', shareToken)
    return http<SongDto[]>(`/api/v0/playlists/${id}/search?${params.toString()}`)
  },

  generateShareToken(id: number): Promise<PlaylistDto> {
    return http<PlaylistDto>(`/api/v0/playlists/${id}/share`, { method: 'POST' })
  },

  toggleVisibility(id: number, isPrivate: boolean): Promise<PlaylistDto> {
    return http<PlaylistDto>(`/api/v0/playlists/${id}/private?value=${isPrivate}`, {
      method: 'PATCH',
    })
  },

}

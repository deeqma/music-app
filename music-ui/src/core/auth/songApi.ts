import type { SongDto, SongFilterParams, CreateSongParams } from './contracts'
import { http, BASE_URL } from './httpClient'
import { getToken } from './authToken'

export const songApi = {

  getAll(params: SongFilterParams = {}): Promise<SongDto[]> {
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
    return http<SongDto[]>(`/api/v1/songs${qsSuffix}`)
  },

  search(query: string, page = 0, pageSize = 15): Promise<SongDto[]> {
    return http<SongDto[]>(
      `/api/v1/songs/search?query=${encodeURIComponent(query)}&page=${page}&pageSize=${pageSize}`
    )
  },

  searchLiked(query: string, page = 0, pageSize = 15): Promise<SongDto[]> {
    return http<SongDto[]>(
      `/api/v1/songs/liked/search?query=${encodeURIComponent(query)}&page=${page}&pageSize=${pageSize}`
    )
  },

  getLiked(params: SongFilterParams = {}): Promise<SongDto[]> {
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
    return http<SongDto[]>(`/api/v1/songs/liked${qsSuffix}`)
  },

  toggleLike(id: number): Promise<string> {
    return http<string>(`/api/v1/songs/${id}/like`, { method: 'POST' })
  },

  upload(params: CreateSongParams): Promise<SongDto> {
    const token = getToken()
    const formData = new FormData()
    formData.append('file',        params.file)
    formData.append('songName',    params.songName)
    formData.append('artistName',  params.artistName)
    formData.append('releaseYear', String(params.releaseYear))
    if (params.album) formData.append('album', params.album)
    if (params.genre) formData.append('genre', params.genre)

    return fetch(`${BASE_URL}/api/v1/songs`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    }).then(async res => {
      if (!res.ok) throw await res.json()
      return res.json()
    })
  },

  update(id: number, data: Partial<CreateSongParams>): Promise<SongDto> {
    return http<SongDto>(`/api/v1/songs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  },

  delete(id: number): Promise<void> {
    return http<void>(`/api/v1/songs/${id}`, { method: 'DELETE' })
  },

  streamUrl(id: number): string {
    return `${BASE_URL}/api/v1/songs/${id}/stream`
  },

}

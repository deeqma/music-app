import type {
  SongDto,
  PlaylistDto,
  SongFilterParams,
  CreateSongParams,
  CreatePlaylistParams,
  ErrorResponse,
} from './contracts'

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Date.now() >= payload.exp * 1000
  } catch {
    return true
  }
}

function getToken(): string | null {
  const token = localStorage.getItem('access_token')
  if (!token) return null
  if (isTokenExpired(token)) {
    localStorage.removeItem('access_token')
    return null
  }
  return token
}

async function http<T>(path: string, options: RequestInit = {}): Promise<T> {
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
    const error: ErrorResponse = await response.json()
    throw error
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json()
}

// songs

export const songsApi = {

  getAll(params: SongFilterParams = {}): Promise<SongDto[]> {
    const query = new URLSearchParams()
    if (params.genre) query.set('genre', params.genre)
    if (params.artistName) query.set('artistName', params.artistName)
    if (params.album) query.set('album', params.album)
    if (params.yearFrom) query.set('yearFrom', String(params.yearFrom))
    if (params.yearTo) query.set('yearTo', String(params.yearTo))
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize', String(params.pageSize))
    const qs = query.toString()
    return http<SongDto[]>(`/api/v1/songs${qs ? `?${qs}` : ''}`)
  },

  search(query: string, page = 0, pageSize = 15): Promise<SongDto[]> {
    return http<SongDto[]>(
      `/api/v1/songs/search?query=${encodeURIComponent(query)}&page=${page}&pageSize=${pageSize}`
    )
  },

  getLiked(params: SongFilterParams = {}): Promise<SongDto[]> {
    const query = new URLSearchParams()
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize', String(params.pageSize))
    const qs = query.toString()
    return http<SongDto[]>(`/api/v1/songs/liked${qs ? `?${qs}` : ''}`)
  },

  toggleLike(id: number): Promise<string> {
    return http<string>(`/api/v1/songs/${id}/like`, { method: 'POST' })
  },

  upload(params: CreateSongParams): Promise<SongDto> {
    const token = getToken()
    const formData = new FormData()
    formData.append('file', params.file)
    formData.append('songName', params.songName)
    formData.append('artistName', params.artistName)
    formData.append('releaseYear', String(params.releaseYear))
    if (params.album) formData.append('album', params.album)
    if (params.genre) formData.append('genre', params.genre)

    return fetch(`${BASE_URL}/api/v1/songs`, {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    }).then(async (res) => {
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

// playlists

export const playlistsApi = {

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
    if (params.page !== undefined) query.set('page', String(params.page))
    if (params.pageSize !== undefined) query.set('pageSize', String(params.pageSize))
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
    const params = new URLSearchParams({
      query,
      page: String(page),
      pageSize: String(pageSize),
    })
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
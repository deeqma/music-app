// backend data structure, // https://github.com/deeqma/music-app/tree/master/src/main/java/io/github/deeqma/music/dto

export interface SongDto {
  id: number
  songName: string
  artistName: string
  album: string | null
  genre: string | null
  releaseYear: number
  filePath: string
  liked: boolean
  durationSeconds: number
}

// Lightweight returned by GET /api/v0/playlists
export interface PlaylistSummaryDto {
  playlistId:   number
  playlistName: string
  slug:         string
  owner:        boolean
}

// Full details returned by GET /api/v0/playlists/{id} and mutating endpoints
export interface PlaylistDetailsDto {
  playlistId:   number
  playlistName: string
  description:  string | null
  slug:         string
  visibility:   'PUBLIC' | 'PRIVATE'
  shareToken:   string | null
  totalSongs:          number
  totalDurationSeconds: number
  songDtos:            SongDto[]
}

export interface SongFilterParams {
  genre?: string
  artistName?: string
  album?: string
  yearFrom?: number
  yearTo?: number
  page?: number
  pageSize?: number
}

export interface CreateSongParams {
  file: File
  songName: string
  artistName: string
  album?: string
  genre?: string
  releaseYear: number
}

export interface CreatePlaylistParams {
  playlistName: string
  description?: string
  visibility?: 'PUBLIC' | 'PRIVATE'
}

export interface UserProfileDto {
  userId:          string
  username:        string
  totalLikedSongs: number
  totalPlaylists:  number
  playlistLimit:   number
}

export interface ErrorResponse {
  errorType: string | null
  status: number
  timestamp: string
  message: string
}

export type UserRole = 'USER' | 'MODERATOR' | 'ADMIN'

export interface RegisterParams {
  username: string
  password: string
  role?:    UserRole
}

export interface LoginParams {
  username: string
  password: string
}

// Access token response from backend after successful authentication
export interface AuthResponse {
  accessToken: string
}
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

export interface PlaylistDto {
  playlistId: number
  playlistName: string
  description: string | null
  slug: string
  visibility: 'PUBLIC' | 'PRIVATE'
  shareToken: string | null
  totalSongs: number
  songDtos: SongDto[]
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

export interface ErrorResponse {
  errorType: string | null
  status: number
  timestamp: string
  message: string
}
// TypeScript mirror of the Java DTOs.
export type PlaylistVisibility = 'PUBLIC' | 'PRIVATE'

export interface SongDto {
  id:              number
  songName:        string
  artistName:      string
  album:           string
  genre:           string
  releaseYear:     number
  filePath:        string
  liked:           boolean
  durationSeconds: number
}

export interface PlaylistDto {
  playlistId:   number
  playlistName: string
  description:  string
  slug:         string
  visibility:   PlaylistVisibility
  totalSongs:   number
  shareToken:   string
  songDtos:     SongDto[]
}

import { create } from 'zustand'
import { playlists as initial } from '../mock/playlistData'
import type { SongDto, PlaylistDto } from '../mock/types'

interface PlaylistsState {
  playlists: PlaylistDto[]
  addSongToPlaylist:      (playlistId: number, song: SongDto) => void
  removeSongFromPlaylist: (playlistId: number, songId: number) => void
}

export const usePlaylistsStore = create<PlaylistsState>(set => ({
  playlists: initial,

  addSongToPlaylist: (playlistId, song) =>
    set(state => ({
      playlists: state.playlists.map(p => {
        if (p.playlistId !== playlistId) return p
        if (p.songDtos.some(s => s.id === song.id)) return p  // already there
        const songDtos = [...p.songDtos, song]
        return { ...p, songDtos, totalSongs: songDtos.length }
      }),
    })),

  removeSongFromPlaylist: (playlistId, songId) =>
    set(state => ({
      playlists: state.playlists.map(p => {
        if (p.playlistId !== playlistId) return p
        const songDtos = p.songDtos.filter(s => s.id !== songId)
        return { ...p, songDtos, totalSongs: songDtos.length }
      }),
    })),
}))

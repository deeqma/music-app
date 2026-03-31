import { create } from 'zustand'
import type { PlaylistSummaryDto } from '../auth/contracts'

interface PlaylistsState {
  playlists:      PlaylistSummaryDto[]
  setPlaylists:   (playlists: PlaylistSummaryDto[]) => void
  addPlaylist:    (playlist: PlaylistSummaryDto) => void
  removePlaylist: (id: number) => void
}

export const usePlaylistsStore = create<PlaylistsState>(set => ({
  playlists: [],

  setPlaylists: playlists => set({ playlists }),

  addPlaylist: playlist =>
    set(state => ({ playlists: [...state.playlists, playlist] })),

  removePlaylist: id =>
    set(state => ({ playlists: state.playlists.filter(p => p.playlistId !== id) })),
}))

import { create } from 'zustand'
import type { SongDto } from '../auth/contracts'
import { songApi } from '../auth/songApi'
import { clearToken } from '../auth/authToken'

export type RepeatMode = 'off' | 'all' | 'one'

interface PlayerState {
  currentSong:  SongDto | null
  queue:        SongDto[]
  isPlaying:    boolean
  likedIds:     Set<number>
  shuffle:      boolean
  repeatMode:   RepeatMode
  sourceRoute:  string

  playSong:        (song: SongDto, queue: SongDto[], sourceRoute: string) => void
  setIsPlaying:    (v: boolean) => void
  toggleLike:      (id: number) => void
  mergeLikedSongs: (songs: SongDto[]) => void
  playNext:        () => void
  playPrev:        () => void
  toggleShuffle:   () => void
  toggleRepeat:    () => void
}

export const usePlayerStore = create<PlayerState>((set, get) => ({
  currentSong:  null,
  queue:        [],
  isPlaying:    false,
  likedIds:     new Set<number>(),
  shuffle:      false,
  repeatMode:   'off',
  sourceRoute:  '',

  playSong: (song, queue, sourceRoute) => set({ currentSong: song, queue, isPlaying: true, sourceRoute }),

  setIsPlaying: v => set({ isPlaying: v }),

  toggleLike: id => {
    // Optimistic update
    set(state => {
      const next = new Set(state.likedIds)
      if (next.has(id)) next.delete(id)
      else              next.add(id)
      return { likedIds: next }
    })
    // API call — rollback on failure
    songApi.toggleLike(id).catch((err: { errorType?: string; status?: number }) => {
      if (err.errorType === 'NOT_FOUND' || err.status === 404) {
        clearToken()
        window.location.replace('/login')
        return
      }
      set(state => {
        const next = new Set(state.likedIds)
        if (next.has(id)) next.delete(id)
        else              next.add(id)
        return { likedIds: next }
      })
    })
  },

  // Merge API-returned songs into likedIds (add liked, remove unliked)
  mergeLikedSongs: (songs: SongDto[]) =>
    set(state => {
      const next = new Set(state.likedIds)
      for (const s of songs) {
        if (s.liked) next.add(s.id)
        else         next.delete(s.id)
      }
      return { likedIds: next }
    }),

  playNext: () => {
    const { currentSong, queue, shuffle, repeatMode } = get()
    if (!currentSong || queue.length === 0) return
    const nextRepeat = repeatMode === 'one' ? 'all' : repeatMode
    if (shuffle) {
      const others = queue.filter(s => s.id !== currentSong.id)
      const pick   = others.length > 0
        ? others[Math.floor(Math.random() * others.length)]
        : queue[0]
      set({ currentSong: pick, isPlaying: true, repeatMode: nextRepeat })
    } else {
      const idx  = queue.findIndex(s => s.id === currentSong.id)
      const next = queue[(idx + 1) % queue.length]
      set({ currentSong: next, isPlaying: true, repeatMode: nextRepeat })
    }
  },

  playPrev: () => {
    const { currentSong, queue, repeatMode } = get()
    if (!currentSong || queue.length === 0) return
    const nextRepeat = repeatMode === 'one' ? 'all' : repeatMode
    const idx  = queue.findIndex(s => s.id === currentSong.id)
    const prev = queue[(idx - 1 + queue.length) % queue.length]
    set({ currentSong: prev, isPlaying: true, repeatMode: nextRepeat })
  },

  toggleShuffle: () => set(s => ({ shuffle: !s.shuffle })),

  toggleRepeat: () =>
    set(s => ({
      repeatMode: s.repeatMode === 'off' ? 'all'
                : s.repeatMode === 'all' ? 'one'
                : 'off',
    })),
}))

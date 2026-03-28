import { create } from 'zustand'
import type { SongDto } from '../mock/types'
import { songs as allSongs } from '../mock/songData'

export type RepeatMode = 'off' | 'all' | 'one'

interface PlayerState {
  currentSong:  SongDto | null
  queue:        SongDto[]
  isPlaying:    boolean
  likedIds:     Set<number>
  shuffle:      boolean
  repeatMode:   RepeatMode
  sourceRoute:  string

  playSong:      (song: SongDto, queue: SongDto[], sourceRoute: string) => void
  setIsPlaying:  (v: boolean) => void
  toggleLike:    (id: number) => void
  playNext:      () => void
  playPrev:      () => void
  toggleShuffle: () => void
  toggleRepeat:  () => void
}

export const usePlayerStore = create<PlayerState>((set, get) => ({
  currentSong:  null,
  queue:        allSongs,
  isPlaying:    false,
  likedIds:     new Set(allSongs.filter(s => s.liked).map(s => s.id)),
  shuffle:      false,
  repeatMode:   'off',
  sourceRoute:  '',

  playSong: (song, queue, sourceRoute) => set({ currentSong: song, queue, isPlaying: true, sourceRoute }),

  setIsPlaying: v => set({ isPlaying: v }),

  toggleLike: id =>
    set(state => {
      const next = new Set(state.likedIds)
      if (next.has(id)) next.delete(id)
      else              next.add(id)
      return { likedIds: next }
    }),

  playNext: () => {
    const { currentSong, queue, shuffle, repeatMode } = get()
    if (!currentSong || queue.length === 0) return
    // Manually skipping forward exits repeat-one → drop to repeat-all
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
    // Manually skipping backward exits repeat-one → drop to repeat-all
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

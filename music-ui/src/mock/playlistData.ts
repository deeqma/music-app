import type { PlaylistDto } from './types'
import { songById } from './songData'

export const playlists: PlaylistDto[] = [
  {
    playlistId:   1,
    playlistName: 'Rock Classics',
    description:  'The greatest rock anthems of all time.',
    slug:         'rock-classics',
    visibility:   'PUBLIC',
    shareToken:   'tok-rc-a1b2c3',
    get totalSongs() { return this.songDtos.length },
    songDtos: [
      songById[1],  // Shoot to Thrill
      songById[2],  // Back in Black
      songById[3],  // Sweet Child O' Mine
      songById[4],  // Bohemian Rhapsody
    ],
  },
  {
    playlistId:   2,
    playlistName: 'Blues',
    description:  'Deep blues from the legends who built the genre.',
    slug:         'blues',
    visibility:   'PUBLIC',
    shareToken:   'tok-bm-d4e5f6',
    get totalSongs() { return this.songDtos.length },
    songDtos: [
      songById[5],  // The Thrill Is Gone
      songById[6],  // Red House
      songById[7],  // Mannish Boy
      songById[8],  // Pride and Joy
    ],
  },
  {
    playlistId:   3,
    playlistName: 'Heavy Metal',
    description:  'For when the volume only goes to eleven.',
    slug:         'heavy-metal',
    visibility:   'PRIVATE',
    shareToken:   'tok-hm-g7h8i9',
    get totalSongs() { return this.songDtos.length },
    songDtos: [
      songById[9],   // Master of Puppets
      songById[10],  // Iron Man
      songById[11],  // Painkiller
      songById[12],  // Holy Wars
    ],
  },
  {
    playlistId:   4,
    playlistName: 'Funky',
    description:  'Grooves that make it impossible to sit still.',
    slug:         'funky',
    visibility:   'PUBLIC',
    shareToken:   'tok-fg-j1k2l3',
    get totalSongs() { return this.songDtos.length },
    songDtos: [
      songById[13],  // Superstition
      songById[14],  // Give It Away
      songById[15],  // Sex Machine
      songById[1],   // Shoot to Thrill — cross-genre pick
    ],
  },
]

// Convenience lookup by slug — matches the URL pattern /playlist/:name
export const playlistBySlug = Object.fromEntries(playlists.map(p => [p.slug, p]))

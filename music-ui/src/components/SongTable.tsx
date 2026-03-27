import { useState } from 'react'
import type { SongDto } from '../mock/types'
import PlayPauseButton from './PlayPauseButton'
import HeartButton from './HeartButton'
import SongDrawer from './SongDrawer'

interface SongTableProps {
  readonly songs: readonly SongDto[]
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

export default function SongTable({ songs }: SongTableProps) {
  const [playingId,  setPlayingId]  = useState<number | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const [likedIds, setLikedIds] = useState<Set<number>>(
    () => new Set(songs.filter(s => s.liked).map(s => s.id))
  )

  const togglePlay = (id: number) =>
    setPlayingId(prev => (prev === id ? null : id))

  const toggleLike = (id: number) =>
    setLikedIds(prev => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else              next.add(id)
      return next
    })

  const selectedSong = songs.find(s => s.id === selectedId) ?? null

  return (
    <>
      {selectedSong && (
        <SongDrawer song={selectedSong} onClose={() => setSelectedId(null)} />
      )}

      <table className="song-table">
        <thead>
          <tr>
            <th className="song-table__col-play" />
            <th className="song-table__col-song">SONG</th>
            <th className="song-table__col-genre">GENRE</th>
            <th className="song-table__col-liked">LIKED</th>
            <th className="song-table__col-duration">DURATION</th>
          </tr>
        </thead>

        <tbody>
          {songs.map(song => {
            const isPlaying  = playingId  === song.id
            const isLiked    = likedIds.has(song.id)
            const isSelected = selectedId === song.id

            return (
              <tr
                key={song.id}
                className={`song-table__row${isPlaying ? ' song-table__row--playing' : ''}${isSelected ? ' song-table__row--selected' : ''}`}
                onClick={() => setSelectedId(prev => prev === song.id ? null : song.id)}
              >
                <td className="song-table__col-play" onClick={e => e.stopPropagation()}>
                  <PlayPauseButton
                    isPlaying={isPlaying}
                    onToggle={() => togglePlay(song.id)}
                  />
                </td>

                <td className="song-table__col-song">
                  <div className="song-table__song-info">
                    <span className="song-table__song-name">{song.songName}</span>
                    <span className="song-table__artist-name">{song.artistName}</span>
                  </div>
                </td>

                <td className="song-table__col-genre">{song.genre}</td>

                <td className="song-table__col-liked" onClick={e => e.stopPropagation()}>
                  <HeartButton
                    liked={isLiked}
                    onToggle={() => toggleLike(song.id)}
                  />
                </td>

                <td className="song-table__col-duration">
                  {formatDuration(song.durationSeconds)}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </>
  )
}

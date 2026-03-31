import { useState, useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import type { SongDto } from '../../core/auth/contracts'
import PlayPauseButton from './PlayPauseButton'
import HeartButton from './HeartButton'
import SongDrawer from './SongDrawer'
import LoginPromptModal from './LoginPromptModal'
import { usePlayerStore } from '../../core/store/playerStore'

interface SongTableProps {
  readonly songs:                       readonly SongDto[]
  readonly onSongDeleted?:              (id: number) => void
  readonly onSongRemovedFromPlaylist?:  (id: number) => void
  readonly onSongUpdated?:             (song: SongDto) => void
  readonly readOnly?:                  boolean
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

export default function SongTable({ songs, onSongDeleted, onSongRemovedFromPlaylist, onSongUpdated, readOnly }: SongTableProps) {
  const [selectedId,     setSelectedId]     = useState<number | null>(null)
  const [loginPromptOpen, setLoginPromptOpen] = useState(false)
  const location = useLocation()

  const { currentSong, isPlaying, likedIds, sourceRoute, playSong, setIsPlaying, toggleLike } = usePlayerStore()

  // This table owns the current context only when the source matches this page
  const isActiveSource = sourceRoute === location.pathname

  function togglePlay(song: SongDto) {
    if (isActiveSource && currentSong?.id === song.id) {
      // Same song, same source context — just pause/resume
      setIsPlaying(!isPlaying)
    } else {
      // Different song OR different source — lock context to this table
      playSong(song, [...songs], location.pathname)
    }
  }

  // Scroll to song when navigated here from the audio container song name click
  useEffect(() => {
    const scrollId = (location.state as { scrollToSongId?: number } | null)?.scrollToSongId
    if (!scrollId) return
    const row = document.querySelector<HTMLElement>(`[data-song-id="${scrollId}"]`)
    row?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, [location.state])

  const selectedSong = songs.find(s => s.id === selectedId) ?? null

  return (
    <>
      {loginPromptOpen && <LoginPromptModal onClose={() => setLoginPromptOpen(false)} />}

      {selectedSong && (
        <SongDrawer
          song={selectedSong}
          onClose={() => setSelectedId(null)}
          onDelete={onSongDeleted}
          onRemovedFromPlaylist={onSongRemovedFromPlaylist}
          onSongUpdated={onSongUpdated}
          readOnly={readOnly}
        />
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
            const isCurrentSong   = isActiveSource && currentSong?.id === song.id
            const isButtonPlaying = isCurrentSong && isPlaying
            const isLiked         = likedIds.has(song.id)
            const isSelected      = selectedId === song.id

            return (
              <tr
                key={song.id}
                data-song-id={song.id}
                className={[
                  'song-table__row',
                  isCurrentSong ? 'song-table__row--playing' : '',
                  isSelected    ? 'song-table__row--selected' : '',
                ].filter(Boolean).join(' ')}
                onClick={() => setSelectedId(prev => prev === song.id ? null : song.id)}
              >
                <td className="song-table__col-play" onClick={e => e.stopPropagation()}>
                  <PlayPauseButton
                    isPlaying={isButtonPlaying}
                    onToggle={() => togglePlay(song)}
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
                    onToggle={readOnly ? () => setLoginPromptOpen(true) : () => toggleLike(song.id)}
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

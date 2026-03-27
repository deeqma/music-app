import { createPortal } from 'react-dom'
import type { SongDto } from '../mock/types'
import Icon from './Icon'
import musicRaw from '../assets/music.svg?raw'
import xRaw     from '../assets/x.svg?raw'

interface SongDrawerProps {
  readonly song:    SongDto
  readonly onClose: () => void
}

export default function SongDrawer({ song, onClose }: SongDrawerProps) {
  const bodyEl = document.querySelector('.main-content__body')
  if (!bodyEl) return null

  return createPortal(
    <>
      <div
        className="song-drawer__backdrop"
        role="button"
        tabIndex={-1}
        aria-label="Close drawer"
        onClick={onClose}
        onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') onClose() }}
      />

      <div className="song-drawer">
        <button className="song-drawer__close" onClick={onClose} aria-label="Close">
          <Icon src={xRaw} size={16} color="secondary" alt="close" />
        </button>

        <div className="song-drawer__cover">
          <Icon src={musicRaw} size={72} color="accent" alt="song" />
        </div>

        <div className="song-drawer__info">
          <span className="song-drawer__song-name">{song.songName}</span>
          <span className="song-drawer__artist">{song.artistName}</span>
        </div>

        <div className="song-drawer__meta">
          <div className="song-drawer__meta-row">
            <span className="song-drawer__meta-label">Song ID</span>
            <span className="song-drawer__meta-value">{song.id}</span>
          </div>
          <div className="song-drawer__meta-row">
            <span className="song-drawer__meta-label">Album</span>
            <span className="song-drawer__meta-value">{song.album}</span>
          </div>
          <div className="song-drawer__meta-row">
            <span className="song-drawer__meta-label">Release Year</span>
            <span className="song-drawer__meta-value">{song.releaseYear}</span>
          </div>
        </div>

        <div className="song-drawer__actions">
          <button className="song-drawer__btn song-drawer__btn--edit">Edit</button>
          <button className="song-drawer__btn song-drawer__btn--delete">Delete</button>
        </div>
      </div>
    </>,
    bodyEl
  )
}

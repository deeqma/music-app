import { useState, useRef, useEffect } from 'react'
import { createPortal } from 'react-dom'
import { playlists } from '../mock/playlistData'
import Icon from './Icon'
import xRaw     from '../assets/x.svg?raw'
import heartRaw from '../assets/heart.svg?raw'

interface ImportModalProps {
  onClose: () => void
}

export default function ImportModal({ onClose }: ImportModalProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [selectedFile,   setSelectedFile]   = useState<File | null>(null)
  const [songName,       setSongName]       = useState('')
  const [artistName,     setArtistName]     = useState('')
  const [genre,          setGenre]          = useState('')
  const [releaseYear,    setReleaseYear]    = useState('')
  const [album,          setAlbum]          = useState('')
  const [playlistChoice, setPlaylistChoice] = useState('')
  const [customPlaylist, setCustomPlaylist] = useState('')
  const [liked,          setLiked]          = useState(false)

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    globalThis.addEventListener('keydown', onKeyDown)
    return () => globalThis.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (file?.name.endsWith('.mp3')) setSelectedFile(file)
  }

  return createPortal(
    <div className="import-overlay">
      <div className="import-modal">

        <div className="import-modal__header">
          <span className="import-modal__title">Import Song</span>
          <button className="import-modal__close" onClick={onClose} aria-label="Close">
            <Icon src={xRaw} size={16} color="secondary" alt="close" />
          </button>
        </div>

        <div className="import-modal__body">

          <div className="import-modal__file-row">
            <input
              ref={fileInputRef}
              type="file"
              accept=".mp3"
              style={{ display: 'none' }}
              onChange={handleFileChange}
            />
            <button
              className="import-modal__browse-btn"
              onClick={() => fileInputRef.current?.click()}
            >
              Browse MP3
            </button>
            <span className="import-modal__file-name">
              {selectedFile ? selectedFile.name : 'No file selected'}
            </span>
          </div>

          <div className="import-modal__row">
            <input
              className="import-modal__input"
              value={songName}
              onChange={e => setSongName(e.target.value)}
              placeholder="Song name"
            />
            <input
              className="import-modal__input"
              value={artistName}
              onChange={e => setArtistName(e.target.value)}
              placeholder="Artist"
            />
          </div>

          <div className="import-modal__row">
            <input
              className="import-modal__input"
              value={genre}
              onChange={e => setGenre(e.target.value)}
              placeholder="Genre"
            />
            <input
              className="import-modal__input import-modal__input--year"
              type="number"
              value={releaseYear}
              onChange={e => setReleaseYear(e.target.value)}
              placeholder="Year"
              min={1900}
              max={2026}
            />
          </div>

          <input
            className="import-modal__input"
            value={album}
            onChange={e => setAlbum(e.target.value)}
            placeholder="Album"
          />

          <select
            className="import-modal__select"
            value={playlistChoice}
            onChange={e => setPlaylistChoice(e.target.value)}
          >
            <option value="">Add to playlist</option>
            {playlists.map(p => (
              <option key={p.playlistId} value={p.slug}>{p.playlistName}</option>
            ))}
            <option value="custom">Custom playlist</option>
          </select>

          {playlistChoice === 'custom' && (
            <input
              className="import-modal__input"
              value={customPlaylist}
              onChange={e => setCustomPlaylist(e.target.value)}
              placeholder="New playlist name"
            />
          )}

          <button
            className={`import-modal__liked-btn${liked ? ' import-modal__liked-btn--active' : ''}`}
            onClick={() => setLiked(l => !l)}
          >
            <Icon src={heartRaw} size={15} color={liked ? 'accent' : 'secondary'} alt="heart" />
            <span>{liked ? 'Liked' : 'Mark as liked'}</span>
          </button>

        </div>

        <div className="import-modal__footer">
          <button className="import-modal__import-btn">Import</button>
        </div>

      </div>
    </div>,
    document.body
  )
}

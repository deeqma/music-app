import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import Icon from './Icon'
import xRaw from '../assets/x.svg?raw'
import { songApi } from '../auth/songApi'
import type { SongDto } from '../auth/contracts'

interface EditSongModalProps {
  song:     SongDto
  onSaved:  (updated: SongDto) => void
  onClose:  () => void
}

export default function EditSongModal({ song, onSaved, onClose }: EditSongModalProps) {
  const [songName,    setSongName]    = useState(song.songName)
  const [artistName,  setArtistName]  = useState(song.artistName)
  const [releaseYear, setReleaseYear] = useState(String(song.releaseYear))
  const [album,       setAlbum]       = useState(song.album ?? '')
  const [saving,      setSaving]      = useState(false)
  const [error,       setError]       = useState('')

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    globalThis.addEventListener('keydown', onKeyDown)
    return () => globalThis.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    if (!songName.trim() || !artistName.trim() || !releaseYear) return
    setSaving(true)
    setError('')
    try {
      const updated = await songApi.update(song.id, {
        songName:    songName.trim(),
        artistName:  artistName.trim(),
        releaseYear: Number(releaseYear),
        album:       album.trim() || undefined,
        genre:       song.genre ?? undefined, // preserve existing genre
      })
      onSaved(updated)
    } catch {
      setError('Failed to save changes')
    } finally {
      setSaving(false)
    }
  }

  return createPortal(
    <div
      className="import-overlay"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="import-modal cpl-modal">

        <div className="import-modal__header">
          <span className="import-modal__title">Edit Song</span>
          <button className="import-modal__close" onClick={onClose} aria-label="Close">
            <Icon src={xRaw} size={16} color="secondary" alt="close" />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="import-modal__body">

            <input
              className="import-modal__input"
              value={songName}
              onChange={e => setSongName(e.target.value)}
              placeholder="Song name"
              required
              autoFocus
            />

            <input
              className="import-modal__input"
              value={artistName}
              onChange={e => setArtistName(e.target.value)}
              placeholder="Artist"
              required
            />

            <div className="import-modal__row">
              <input
                className="import-modal__input"
                value={album}
                onChange={e => setAlbum(e.target.value)}
                placeholder="Album"
              />
              <input
                className="import-modal__input import-modal__input--year"
                type="number"
                value={releaseYear}
                onChange={e => setReleaseYear(e.target.value)}
                placeholder="Year"
                min={1900}
                max={2100}
                required
              />
            </div>

            {error && <p className="cpl-modal__error">{error}</p>}

          </div>

          <div className="import-modal__footer cpl-modal__footer--row">
            <button
              type="button"
              className="import-modal__import-btn cpl-modal__cancel-btn"
              onClick={onClose}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="import-modal__import-btn"
              disabled={saving || !songName.trim() || !artistName.trim() || !releaseYear}
            >
              {saving ? 'Saving…' : 'Confirm'}
            </button>
          </div>
        </form>

      </div>
    </div>,
    document.body
  )
}

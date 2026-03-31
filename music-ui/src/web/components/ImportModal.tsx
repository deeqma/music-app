import { useState, useRef, useEffect } from 'react'
import { createPortal } from 'react-dom'
import Icon from './Icon'
import xRaw from '../assets/x.svg?raw'
import { songApi } from '../../core/auth/songApi'

interface ImportModalProps {
  onClose: () => void
}

export default function ImportModal({ onClose }: ImportModalProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [songName,     setSongName]     = useState('')
  const [artistName,   setArtistName]   = useState('')
  const [genre,        setGenre]        = useState('')
  const [releaseYear,  setReleaseYear]  = useState('')
  const [album,        setAlbum]        = useState('')
  const [submitting,   setSubmitting]   = useState(false)
  const [error,        setError]        = useState('')
  const [success,      setSuccess]      = useState(false)

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    globalThis.addEventListener('keydown', onKeyDown)
    return () => globalThis.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return
    if (!file.name.toLowerCase().endsWith('.mp3')) {
      setError('Only MP3 files are supported.')
      return
    }
    setError('')
    setSelectedFile(file)
    // Auto-fill song name from filename if empty
    if (!songName) {
      setSongName(file.name.replace(/\.mp3$/i, ''))
    }
  }

  const canSubmit = selectedFile && songName.trim() && artistName.trim() && releaseYear.trim()

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    if (!canSubmit || submitting || !selectedFile) return
    setSubmitting(true)
    setError('')
    try {
      await songApi.upload({
        file:        selectedFile,
        songName:    songName.trim(),
        artistName:  artistName.trim(),
        releaseYear: Number(releaseYear),
        album:       album.trim() || undefined,
        genre:       genre.trim() || undefined,
      })
      setSuccess(true)
      setTimeout(onClose, 1000)
    } catch (err) {
      const e = err as { message?: string }
      setError(e.message ?? 'Upload failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return createPortal(
    <dialog
      className="import-overlay"
      open
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
      onKeyDown={e => { if (e.key === 'Escape') onClose() }}
    >
      <div className="import-modal">

        <div className="import-modal__header">
          <span className="import-modal__title">Import Song</span>
          <button className="import-modal__close" onClick={onClose} aria-label="Close">
            <Icon src={xRaw} size={16} color="secondary" alt="close" />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="import-modal__body">

            <div className="import-modal__file-row">
              <input
                ref={fileInputRef}
                type="file"
                accept=".mp3,audio/mpeg"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              <button
                type="button"
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
                placeholder="Song name *"
                required
              />
              <input
                className="import-modal__input"
                value={artistName}
                onChange={e => setArtistName(e.target.value)}
                placeholder="Artist *"
                required
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
                placeholder="Year *"
                min={1900}
                max={2099}
                required
              />
            </div>

            <input
              className="import-modal__input"
              value={album}
              onChange={e => setAlbum(e.target.value)}
              placeholder="Album (optional)"
            />

            {error   && <p className="cpl-modal__error">{error}</p>}
            {success && <p className="cpl-modal__error" style={{ color: 'var(--color-accent)' }}>Uploaded successfully!</p>}

          </div>

          <div className="import-modal__footer">
            <button
              type="submit"
              className="import-modal__import-btn"
              disabled={!canSubmit || submitting}
            >
              {submitting ? 'Uploading…' : 'Import'}
            </button>
          </div>
        </form>

      </div>
    </dialog>,
    document.body
  )
}

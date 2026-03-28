import { useState, useEffect } from 'react'
import { createPortal } from 'react-dom'
import Icon from './Icon'
import xRaw     from '../assets/x.svg?raw'
import lockRaw  from '../assets/lock.svg?raw'
import publicRaw from '../assets/public.svg?raw'
import { playlistApi } from '../auth/playlistApi'
import { usePlaylistsStore } from '../store/playlistsStore'

interface CreatePlaylistModalProps {
  onClose: () => void
}

export default function CreatePlaylistModal({ onClose }: CreatePlaylistModalProps) {
  const [name,        setName]        = useState('')
  const [description, setDescription] = useState('')
  const [isPrivate,   setIsPrivate]   = useState(true)
  const [submitting,  setSubmitting]  = useState(false)
  const [error,       setError]       = useState('')

  const { addPlaylist } = usePlaylistsStore()

  useEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    globalThis.addEventListener('keydown', onKeyDown)
    return () => globalThis.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    if (!name.trim()) return
    setSubmitting(true)
    setError('')
    try {
      const created = await playlistApi.create({
        playlistName: name.trim(),
        description:  description.trim() || undefined,
        visibility:   isPrivate ? 'PRIVATE' : 'PUBLIC',
      })
      addPlaylist(created)
      onClose()
    } catch {
      setError('Failed to create playlist. Check the name and try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return createPortal(
    <div
      className="import-overlay"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="import-modal cpl-modal">

        <div className="import-modal__header">
          <span className="import-modal__title">New Playlist</span>
          <button className="import-modal__close" onClick={onClose} aria-label="Close">
            <Icon src={xRaw} size={16} color="secondary" alt="close" />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="import-modal__body">

            <input
              className="import-modal__input"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Playlist name"
              maxLength={50}
              required
              autoFocus
            />

            <textarea
              className="import-modal__input cpl-modal__textarea"
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Description (optional)"
              rows={3}
            />

            <button
              type="button"
              className={`import-modal__liked-btn${!isPrivate ? ' import-modal__liked-btn--active' : ''}`}
              onClick={() => setIsPrivate(p => !p)}
            >
              <Icon
                src={isPrivate ? lockRaw : publicRaw}
                size={15}
                color={isPrivate ? 'secondary' : 'accent'}
                alt={isPrivate ? 'private' : 'public'}
              />
              <span>{isPrivate ? 'Private' : 'Public'}</span>
            </button>

            {error && <p className="cpl-modal__error">{error}</p>}

          </div>

          <div className="import-modal__footer">
            <button
              type="submit"
              className="import-modal__import-btn"
              disabled={submitting || !name.trim()}
            >
              {submitting ? 'Creating…' : 'Create Playlist'}
            </button>
          </div>
        </form>

      </div>
    </div>,
    document.body
  )
}

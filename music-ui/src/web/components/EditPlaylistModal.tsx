import { useState, useEffect, useRef } from 'react'
import { createPortal } from 'react-dom'
import Icon from './Icon'
import xRaw      from '../assets/x.svg?raw'
import lockRaw   from '../assets/lock.svg?raw'
import publicRaw from '../assets/public.svg?raw'
import { playlistApi } from '../../core/auth/playlistApi'
import type { PlaylistDetailsDto } from '../../core/auth/contracts'

interface EditPlaylistModalProps {
  playlistId:         number
  initialName:        string
  initialDescription: string
  initialVisibility:  'PUBLIC' | 'PRIVATE'
  onSaved:            (updated: PlaylistDetailsDto) => void
  onClose:            () => void
}

export default function EditPlaylistModal({
  playlistId,
  initialName,
  initialDescription,
  initialVisibility,
  onSaved,
  onClose,
}: EditPlaylistModalProps) {
  const [name,        setName]        = useState(initialName)
  const [description, setDescription] = useState(initialDescription)
  const [isPrivate,   setIsPrivate]   = useState(initialVisibility === 'PRIVATE')
  const [saving,      setSaving]      = useState(false)
  const [error,       setError]       = useState('')
  const errorTimeout = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    if (!error) return
    if (errorTimeout.current) clearTimeout(errorTimeout.current)
    errorTimeout.current = setTimeout(() => setError(''), 3000)
  }, [error])

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
    setSaving(true)
    setError('')
    try {
      const wantedVisibility = isPrivate ? 'PRIVATE' : 'PUBLIC'

      let result = await playlistApi.update(playlistId, {
        playlistName: name.trim(),
        description:  description.trim() || undefined,
        visibility:   wantedVisibility,
      })

      // If the backend didn't honour the visibility change, use the dedicated endpoint
      if (result.visibility !== wantedVisibility) {
        result = await playlistApi.toggleVisibility(playlistId, isPrivate)
      }

      onSaved(result)
    } catch (err) {
      const e = err as { message?: string }
      setError(e.message ?? 'Failed to save changes')
    } finally {
      setSaving(false)
    }
  }

  return createPortal(
    <dialog
      className="import-overlay"
      open
    >
      <button className="import-overlay__backdrop" onClick={onClose} aria-label="Close modal" />
      <div className="import-modal cpl-modal">

        <div className="import-modal__header">
          <span className="import-modal__title">Edit Playlist</span>
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
              className={`import-modal__liked-btn${isPrivate ? '' : ' import-modal__liked-btn--active'}`}
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
              disabled={saving || !name.trim()}
            >
              {saving ? 'Saving…' : 'Confirm'}
            </button>
          </div>
        </form>

      </div>
    </dialog>,
    document.body
  )
}

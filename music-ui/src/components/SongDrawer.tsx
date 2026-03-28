import { useRef, useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { useLocation } from 'react-router-dom'
import type { SongDto } from '../auth/contracts'
import { songApi } from '../auth/songApi'
import Icon from './Icon'
import musicRaw from '../assets/music.svg?raw'
import xRaw     from '../assets/x.svg?raw'
import { usePlaylistsStore } from '../store/playlistsStore'
import PlaylistDropdown from './PlaylistDropdown'

interface SongDrawerProps {
  readonly song:      SongDto
  readonly onClose:   () => void
  readonly onDelete?: (id: number) => void
}

export default function SongDrawer({ song, onClose, onDelete }: SongDrawerProps) {
  const drawerRef   = useRef<HTMLDivElement>(null)
  const addTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const [addedMsg,    setAddedMsg]    = useState('')
  const [deleting,    setDeleting]    = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const { pathname } = useLocation()
  const isPlaylistPage = pathname.startsWith('/playlist/')
  const currentSlug    = isPlaylistPage ? pathname.replace('/playlist/', '') : null

  const { playlists, addSongToPlaylist, removeSongFromPlaylist } = usePlaylistsStore()
  const currentPlaylist = playlists.find(p => p.slug === currentSlug) ?? null

  const availablePlaylists = playlists.filter(p => !p.songDtos.some(s => s.id === song.id))

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose()
    }
    function handleMouseDown(e: MouseEvent) {
      if (drawerRef.current?.contains(e.target as Node)) return
      if ((e.target as Element).closest?.('.song-table__row')) return
      onClose()
    }
    const timer = setTimeout(() => {
      document.addEventListener('keydown',   handleKeyDown)
      document.addEventListener('mousedown', handleMouseDown)
    }, 0)
    return () => {
      clearTimeout(timer)
      document.removeEventListener('keydown',   handleKeyDown)
      document.removeEventListener('mousedown', handleMouseDown)
    }
  }, [onClose])

  function handleAddToPlaylist(playlistId: number) {
    const pl = playlists.find(p => p.playlistId === playlistId)
    if (!pl) return
    addSongToPlaylist(playlistId, song)
    const msg = `Added to ${pl.playlistName}`
    setAddedMsg(msg)
    if (addTimerRef.current) clearTimeout(addTimerRef.current)
    addTimerRef.current = setTimeout(() => setAddedMsg(''), 2000)
  }

  function handleRemoveFromPlaylist() {
    if (!currentPlaylist) return
    removeSongFromPlaylist(currentPlaylist.playlistId, song.id)
    onClose()
  }

  async function handleDelete() {
    setDeleting(true)
    setDeleteError('')
    try {
      await songApi.delete(song.id)
      onDelete?.(song.id)
      onClose()
    } catch {
      setDeleteError('Delete failed')
      setDeleting(false)
    }
  }

  const mountEl = document.querySelector('.main-content__body-wrapper')
  if (!mountEl) return null

  return createPortal(
    <div ref={drawerRef} className="song-drawer">
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
          <span className="song-drawer__meta-value">{song.album ?? '—'}</span>
        </div>
        <div className="song-drawer__meta-row">
          <span className="song-drawer__meta-label">Release Year</span>
          <span className="song-drawer__meta-value">{song.releaseYear}</span>
        </div>
      </div>

      {/* Add to playlist */}
      <div className="song-drawer__section">
        <span className="song-drawer__section-label">Add to playlist</span>
        {availablePlaylists.length > 0 ? (
          <PlaylistDropdown playlists={availablePlaylists} onSelect={handleAddToPlaylist} />
        ) : (
          <span className="song-drawer__section-note">Song is in all playlists</span>
        )}
        {addedMsg && <span className="song-drawer__added-msg">{addedMsg}</span>}
      </div>

      {/* Remove from playlist — only on playlist pages */}
      {isPlaylistPage && currentPlaylist && (
        <button
          className="song-drawer__btn song-drawer__btn--remove"
          onClick={handleRemoveFromPlaylist}
        >
          Remove from {currentPlaylist.playlistName}
        </button>
      )}

      <div className="song-drawer__actions">
        <button className="song-drawer__btn song-drawer__btn--edit">Edit</button>
        <button
          className="song-drawer__btn song-drawer__btn--delete"
          onClick={handleDelete}
          disabled={deleting}
        >
          {deleting ? 'Deleting…' : 'Delete'}
        </button>
      </div>
      {deleteError && <p className="song-drawer__error">{deleteError}</p>}
    </div>,
    mountEl
  )
}

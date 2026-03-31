import { useRef, useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { useLocation } from 'react-router-dom'
import type { SongDto, PlaylistSummaryDto } from '../../core/auth/contracts'
import { songApi } from '../../core/auth/songApi'
import Icon from './Icon'
import musicRaw from '../assets/music.svg?raw'
import xRaw     from '../assets/x.svg?raw'
import { playlistApi } from '../../core/auth/playlistApi'
import { usePlaylistsStore } from '../../core/store/playlistsStore'
import PlaylistDropdown from './PlaylistDropdown'
import EditSongModal from './EditSongModal'
import { isAdmin } from '../../core/auth/authToken'

interface SongDrawerProps {
  readonly song:                   SongDto
  readonly onClose:                () => void
  readonly onDelete?:              (id: number) => void
  readonly onRemovedFromPlaylist?: (id: number) => void
  readonly onSongUpdated?:         (song: SongDto) => void
  readonly readOnly?:              boolean
}

export default function SongDrawer({ song, onClose, onDelete, onRemovedFromPlaylist, onSongUpdated, readOnly }: SongDrawerProps) {
  const drawerRef   = useRef<HTMLDivElement>(null)
  const addTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const [addedMsg,    setAddedMsg]    = useState('')
  const [deleting,    setDeleting]    = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [removing,    setRemoving]    = useState(false)
  const [editOpen,    setEditOpen]    = useState(false)

  // Fresh playlist list fetched on drawer open
  const [allPlaylists,     setAllPlaylists]     = useState<PlaylistSummaryDto[]>([])
  const [playlistsLoading, setPlaylistsLoading] = useState(true)

  const { pathname } = useLocation()
  const isPlaylistPage = pathname.startsWith('/playlist/')
  const currentSlug    = isPlaylistPage ? pathname.replace('/playlist/', '') : null

  // Still need slug→id lookup for remove; use store summaries (already loaded)
  const { playlists: storePlaylists } = usePlaylistsStore()
  const currentPlaylist = storePlaylists.find(p => p.slug === currentSlug) ?? null

  // Fetch all playlists fresh when drawer opens
  useEffect(() => {
    playlistApi.getAll()
      .then(list => {
        // Exclude the playlist this song is already being viewed in
        setAllPlaylists(list.filter(p => p.owner && p.playlistId !== currentPlaylist?.playlistId))
      })
      .catch(() => setAllPlaylists([]))
      .finally(() => setPlaylistsLoading(false))
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

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

  async function handleAddToPlaylist(playlistId: number) {
    const pl = allPlaylists.find(p => p.playlistId === playlistId)
    if (!pl) return
    try {
      await playlistApi.addSong(playlistId, song.id)
      setAddedMsg(`Added to ${pl.playlistName}`)
    } catch {
      setAddedMsg('Failed to add')
    }
    if (addTimerRef.current) clearTimeout(addTimerRef.current)
    addTimerRef.current = setTimeout(() => setAddedMsg(''), 2000)
  }

  async function handleRemoveFromPlaylist() {
    if (!currentPlaylist || removing) return
    setRemoving(true)
    try {
      await playlistApi.removeSong(currentPlaylist.playlistId, song.id)
      onRemovedFromPlaylist?.(song.id)
      onClose()
    } catch {
      setRemoving(false)
    }
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

  const mountEl = document.querySelector('.main-content__body-wrapper') ?? document.body

  return (
    <>
      {editOpen && (
        <EditSongModal
          song={song}
          onSaved={updated => { onSongUpdated?.(updated); setEditOpen(false) }}
          onClose={() => setEditOpen(false)}
        />
      )}
      {createPortal(
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

      {/* Add to playlist — hidden in read-only mode */}
      {!readOnly && (
        <div className="song-drawer__section">
          <span className="song-drawer__section-label">Add to playlist</span>
          {playlistsLoading && (
            <span className="song-drawer__section-note">Loading…</span>
          )}
          {!playlistsLoading && allPlaylists.length > 0 && (
            <PlaylistDropdown playlists={allPlaylists} onSelect={handleAddToPlaylist} />
          )}
          {!playlistsLoading && allPlaylists.length === 0 && (
            <span className="song-drawer__section-note">No playlists available</span>
          )}
          {addedMsg && <span className="song-drawer__added-msg">{addedMsg}</span>}
        </div>
      )}

      {/* Remove from playlist — only on playlist pages, not in read-only mode */}
      {!readOnly && isPlaylistPage && currentPlaylist && (
        <button
          className="song-drawer__btn song-drawer__btn--remove"
          onClick={handleRemoveFromPlaylist}
          disabled={removing}
        >
          {removing ? 'Removing…' : `Remove from ${currentPlaylist.playlistName}`}
        </button>
      )}

      {!readOnly && isAdmin() && (
        <div className="song-drawer__actions">
          <button
            className="song-drawer__btn song-drawer__btn--edit"
            onClick={() => setEditOpen(true)}
          >
            Edit
          </button>
          <button
            className="song-drawer__btn song-drawer__btn--delete"
            onClick={handleDelete}
            disabled={deleting}
          >
            {deleting ? 'Deleting…' : 'Delete'}
          </button>
        </div>
      )}

      {deleteError && <p className="song-drawer__error">{deleteError}</p>}
    </div>,
    mountEl
  )}
    </>
  )
}

import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import type { SongDto, PlaylistDetailsDto } from '../../core/auth/contracts'
import { playlistApi } from '../../core/auth/playlistApi'
import { usePlaylistsStore } from '../../core/store/playlistsStore'
import { usePlayerStore } from '../../core/store/playerStore'
import SongTable from '../components/SongTable'
import FilterPanel from '../components/FilterPanel'
import EditPlaylistModal from '../components/EditPlaylistModal'
import Icon from '../components/Icon'
import musicRaw  from '../assets/music.svg?raw'
import searchRaw from '../assets/search.svg?raw'
import copyRaw   from '../assets/copy.svg?raw'
import lockRaw   from '../assets/lock.svg?raw'
import publicRaw from '../assets/public.svg?raw'
import filterRaw from '../assets/filter.svg?raw'
import { DEFAULT_FILTER, isFilterActive } from '../../core/lib/filterUtils'
import type { FilterState } from '../../core/lib/filterUtils'

const PAGE_SIZE = 15

function formatTotalDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

export default function PlaylistPage() {
  const { name } = useParams<{ name: string }>()

  const navigate = useNavigate()

  const { playlists, removePlaylist } = usePlaylistsStore()
  const summary = playlists.find(p => p.slug === name) ?? null

  const [details,   setDetails]   = useState<PlaylistDetailsDto | null>(null)
  const [editOpen,  setEditOpen]  = useState(false)
  const [deleting,  setDeleting]  = useState(false)

  const [songs,   setSongs]   = useState<SongDto[]>([])
  const [page,    setPage]    = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [loading, setLoading] = useState(false)

  const [searchQuery,    setSearchQuery]    = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')

  const [filterOpen,      setFilterOpen]      = useState(false)
  const [filter,          setFilter]          = useState<FilterState>(DEFAULT_FILTER)
  const [debouncedFilter, setDebouncedFilter] = useState<FilterState>(DEFAULT_FILTER)

  const [copied,      setCopied]      = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const copyTimeout       = useRef<ReturnType<typeof setTimeout> | null>(null)
  const deleteErrorTimeout = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    if (!deleteError) return
    if (deleteErrorTimeout.current) clearTimeout(deleteErrorTimeout.current)
    deleteErrorTimeout.current = setTimeout(() => setDeleteError(''), 3000)
  }, [deleteError])

  const loadingRef  = useRef(false)
  const pageRef     = useRef(0)
  const hasMoreRef  = useRef(true)
  const queryRef    = useRef('')
  const filterRef   = useRef<FilterState>(DEFAULT_FILTER)
  const summaryRef  = useRef(summary)
  const sentinelRef = useRef<HTMLDivElement>(null)

  summaryRef.current = summary

  const mergeLikedSongs = usePlayerStore(s => s.mergeLikedSongs)

  pageRef.current    = page
  hasMoreRef.current = hasMore

  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(searchQuery), 300)
    return () => clearTimeout(t)
  }, [searchQuery])

  useEffect(() => {
    const t = setTimeout(() => setDebouncedFilter(filter), 300)
    return () => clearTimeout(t)
  }, [filter])

  queryRef.current  = debouncedQuery
  filterRef.current = debouncedFilter

  async function ensureShareToken(playlistId: number, result: PlaylistDetailsDto) {
    if (result.visibility === 'PRIVATE' && !result.shareToken) {
      try {
        const withToken = await playlistApi.generateShareToken(playlistId)
        setDetails(withToken)
      } catch {
        setDetails(result)
      }
    } else {
      setDetails(result)
    }
  }

  async function loadPage(p: number, q: string, f: FilterState, reset = false) {
    const s = summaryRef.current
    if (!s || loadingRef.current) return
    loadingRef.current = true
    setLoading(true)
    try {
      let data: SongDto[]
      if (q) {
        data = await playlistApi.searchSongs(s.playlistId, q, undefined, p, PAGE_SIZE)
      } else {
        const result = await playlistApi.getById(s.playlistId, { ...f, page: p, pageSize: PAGE_SIZE })
        data = result.songDtos
        if (reset) {
          await ensureShareToken(s.playlistId, result)
        }
      }
      mergeLikedSongs(data)
      if (data.length < PAGE_SIZE) setHasMore(false)
      setSongs(prev => reset ? data : [...prev, ...data])
      setPage(p + 1)
    } catch {
      // keep current state on error
    } finally {
      loadingRef.current = false
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!summary) return
    setSongs([])
    setDetails(null)
    setPage(0)
    setHasMore(true)
    setSearchQuery('')
    setFilter(DEFAULT_FILTER)
    setDebouncedQuery('')
    setDebouncedFilter(DEFAULT_FILTER)
    setDeleteError('')
    loadPage(0, '', DEFAULT_FILTER, true)
  }, [summary?.playlistId]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!summary) return
    setSongs([])
    setPage(0)
    setHasMore(true)
    loadPage(0, debouncedQuery, debouncedFilter, true)
  }, [debouncedQuery, debouncedFilter]) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const obs = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && hasMoreRef.current && !loadingRef.current) {
        loadPage(pageRef.current, queryRef.current, filterRef.current)
      }
    }, { rootMargin: '200px' })
    obs.observe(el)
    return () => obs.disconnect()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  async function handleSaved(updated: PlaylistDetailsDto) {
    setDetails(updated)
    setEditOpen(false)
    // Auto-generate share token if just switched to private with no token yet
    if (updated.visibility === 'PRIVATE' && !updated.shareToken && summary) {
      try {
        const withToken = await playlistApi.generateShareToken(summary.playlistId)
        setDetails(withToken)
      } catch { /* share token generation is best-effort */ }
    }
  }

  function handleCopy() {
    if (!details?.shareToken) return
    const shareUrl = `${globalThis.location.origin}/share/${details.shareToken}`
    navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    if (copyTimeout.current) clearTimeout(copyTimeout.current)
    copyTimeout.current = setTimeout(() => setCopied(false), 2000)
  }

  async function handleDelete() {
    if (!summary || deleting) return
    setDeleting(true)
    setDeleteError('')
    try {
      await playlistApi.delete(summary.playlistId)
      removePlaylist(summary.playlistId)
      navigate('/explore')
    } catch (err) {
      const e = err as { message?: string }
      setDeleteError(e.message ?? 'Failed to delete playlist')
      setDeleting(false)
    }
  }

  function handleSongDeleted(id: number) {
    setSongs(prev => prev.filter(s => s.id !== id))
  }

  function handleSongRemovedFromPlaylist(songId: number) {
    setSongs(prev => prev.filter(s => s.id !== songId))
  }

  function handleSongUpdated(song: SongDto) {
    setSongs(prev => prev.map(s => s.id === song.id ? song : s))
  }

  if (!summary) return <p className="playlist-page__not-found">Playlist not found.</p>

  const isPrivate     = details?.visibility === 'PRIVATE'
  const totalDuration = details?.totalDurationSeconds ?? 0
  const shareUrl      = `${globalThis.location.origin}/share/${details?.shareToken}`
  const filterActive  = isFilterActive(filter)

  return (
    <div className="playlist-page">

      {editOpen && details && (
        <EditPlaylistModal
          playlistId={summary.playlistId}
          initialName={details.playlistName}
          initialDescription={details.description ?? ''}
          initialVisibility={details.visibility}
          onSaved={handleSaved}
          onClose={() => setEditOpen(false)}
        />
      )}

      <div className="playlist-hero">
        <div className="playlist-hero__cover">
          <Icon src={musicRaw} size={80} color="accent" alt="playlist cover" />
        </div>

        <div className="playlist-hero__info">
          <div className="playlist-hero__label-row">
            <span className="playlist-hero__label">Playlist</span>
            {/* Display-only — edit visibility through the Edit modal */}
            <span className="playlist-hero__visibility-badge" title={isPrivate ? 'Private' : 'Public'}>
              <Icon
                src={isPrivate ? lockRaw : publicRaw}
                size={14}
                color={isPrivate ? 'accent' : 'secondary'}
                alt={isPrivate ? 'private' : 'public'}
              />
            </span>
          </div>

          <h1 className="playlist-hero__name">{details?.playlistName ?? summary.playlistName}</h1>
          {details?.description && (
            <p className="playlist-hero__description">{details.description}</p>
          )}
          <div className="playlist-hero__stats">
            <span>{details?.totalSongs ?? 0} songs</span>
            <span className="playlist-hero__dot">·</span>
            <span>{formatTotalDuration(totalDuration)}</span>
          </div>

          {summary.owner && (
            <div className="playlist-hero__actions">
              <button
                className="playlist-hero__btn playlist-hero__btn--edit"
                onClick={() => setEditOpen(true)}
                disabled={!details}
              >
                Edit
              </button>
              <button
                className="playlist-hero__btn playlist-hero__btn--delete"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting…' : 'Delete'}
              </button>
            </div>
          )}
          {deleteError && <p className="playlist-hero__error">{deleteError}</p>}

          {isPrivate && details?.shareToken && (
            <div className="playlist-share-bar">
              <span className="playlist-share-bar__url">{shareUrl}</span>
              <button
                className="playlist-share-bar__copy-btn"
                onClick={handleCopy}
                disabled={copied}
                aria-label="Copy link"
              >
                {copied
                  ? <span className="playlist-share-bar__copied">Copied</span>
                  : <Icon src={copyRaw} size={15} color="accent" alt="copy" />
                }
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Toolbar: search + filter */}
      <div className="playlist-toolbar">
        <div className="playlist-toolbar__search">
          <Icon src={searchRaw} size={16} color="accent" alt="search" />
          <input
            type="text"
            className="playlist-toolbar__search-input"
            placeholder="Search by song name or artist…"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="song-twf__bar playlist-toolbar__filter-bar">
          <button
            className={`song-twf__filter-btn${filterOpen ? ' song-twf__filter-btn--open' : ''}`}
            onClick={() => setFilterOpen(o => !o)}
            aria-label="Toggle filter"
          >
            <Icon src={filterRaw} size={14} color="accent" alt="filter" />
            <span>Filter</span>
          </button>

          {filterActive && (
            <button
              className="song-twf__clear-btn"
              onClick={() => setFilter(DEFAULT_FILTER)}
            >
              Clear
            </button>
          )}

          <span className="song-twf__count">{songs.length} songs</span>
        </div>
      </div>

      {filterOpen && (
        <FilterPanel filter={filter} onChange={setFilter} />
      )}

      <SongTable
        songs={songs}
        onSongDeleted={handleSongDeleted}
        onSongRemovedFromPlaylist={handleSongRemovedFromPlaylist}
        onSongUpdated={handleSongUpdated}
      />
      <div ref={sentinelRef} style={{ height: 1 }} />
      {loading && <p className="songs-status">Loading…</p>}
      {!hasMore && songs.length > 0 && (
        <p className="songs-status songs-status--end">All songs loaded</p>
      )}
    </div>
  )
}

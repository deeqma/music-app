import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import type { SongDto, PlaylistDetailsDto } from '../auth/contracts'
import { playlistApi } from '../auth/playlistApi'
import { usePlayerStore } from '../store/playerStore'
import { getToken } from '../auth/authToken'
import SongTable from '../components/SongTable'
import FilterPanel from '../components/FilterPanel'
import Icon from '../components/Icon'
import musicRaw  from '../assets/music.svg?raw'
import searchRaw from '../assets/search.svg?raw'
import filterRaw from '../assets/filter.svg?raw'
import lockRaw   from '../assets/lock.svg?raw'
import publicRaw from '../assets/public.svg?raw'
import { DEFAULT_FILTER, isFilterActive } from '../lib/filterUtils'
import type { FilterState } from '../lib/filterUtils'

const PAGE_SIZE = 15

function formatTotalDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

export default function SharePage() {
  const { shareToken } = useParams<{ shareToken: string }>()
  const isAuthenticated = !!getToken()

  const [details,  setDetails]  = useState<PlaylistDetailsDto | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [songs,    setSongs]    = useState<SongDto[]>([])
  const [page,     setPage]     = useState(0)
  const [hasMore,  setHasMore]  = useState(true)
  const [loading,  setLoading]  = useState(false)

  const [searchQuery,    setSearchQuery]    = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')

  const [filterOpen,      setFilterOpen]      = useState(false)
  const [filter,          setFilter]          = useState<FilterState>(DEFAULT_FILTER)
  const [debouncedFilter, setDebouncedFilter] = useState<FilterState>(DEFAULT_FILTER)

  const loadingRef  = useRef(false)
  const pageRef     = useRef(0)
  const hasMoreRef  = useRef(true)
  const queryRef    = useRef('')
  const filterRef   = useRef<FilterState>(DEFAULT_FILTER)
  const detailsRef  = useRef<PlaylistDetailsDto | null>(null)
  const sentinelRef = useRef<HTMLDivElement>(null)

  const mergeLikedSongs = usePlayerStore(s => s.mergeLikedSongs)

  pageRef.current    = page
  hasMoreRef.current = hasMore
  queryRef.current   = debouncedQuery
  filterRef.current  = debouncedFilter
  detailsRef.current = details

  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(searchQuery), 300)
    return () => clearTimeout(t)
  }, [searchQuery])

  useEffect(() => {
    const t = setTimeout(() => setDebouncedFilter(filter), 300)
    return () => clearTimeout(t)
  }, [filter])

  async function loadPage(p: number, q: string, f: FilterState, reset = false) {
    if (!shareToken || loadingRef.current) return
    loadingRef.current = true
    setLoading(true)
    try {
      let data: SongDto[]
      if (q) {
        const d = detailsRef.current
        if (!d) return
        data = await playlistApi.searchSongs(d.playlistId, q, shareToken, p, PAGE_SIZE)
      } else {
        const result = await playlistApi.getByShareToken(shareToken, { ...f, page: p, pageSize: PAGE_SIZE })
        data = result.songDtos
        if (reset) setDetails(result)
      }
      mergeLikedSongs(data)
      if (data.length < PAGE_SIZE) setHasMore(false)
      setSongs(prev => reset ? data : [...prev, ...data])
      setPage(p + 1)
    } catch {
      // Only mark as not found when the initial playlist fetch fails
      if (reset && !detailsRef.current) setNotFound(true)
      // Search/filter failures silently return empty — don't wipe the page
    } finally {
      loadingRef.current = false
      setLoading(false)
    }
  }

  // Single effect: fires on mount + whenever query or filter changes
  useEffect(() => {
    setSongs([])
    setPage(0)
    setHasMore(true)
    loadPage(0, debouncedQuery, debouncedFilter, true)
  }, [debouncedQuery, debouncedFilter]) // eslint-disable-line react-hooks/exhaustive-deps

  // Infinite scroll
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

  if (notFound) {
    return <p className="share-page__not-found">Playlist not found or the owner deleted it.</p>
  }

  if (!details && loading) {
    return <p className="songs-status">Loading…</p>
  }

  const isPrivate     = details?.visibility === 'PRIVATE'
  const totalDuration = details?.totalDurationSeconds ?? 0
  const filterActive  = isFilterActive(filter)

  return (
    <div className="playlist-page share-page">

      <div className="playlist-hero">
        <div className="playlist-hero__cover">
          <Icon src={musicRaw} size={80} color="accent" alt="playlist cover" />
        </div>

        <div className="playlist-hero__info">
          <div className="playlist-hero__label-row">
            <span className="playlist-hero__label">Playlist</span>
            <span className="playlist-hero__visibility-badge" title={isPrivate ? 'Private' : 'Public'}>
              <Icon
                src={isPrivate ? lockRaw : publicRaw}
                size={14}
                color={isPrivate ? 'accent' : 'secondary'}
                alt={isPrivate ? 'private' : 'public'}
              />
            </span>
          </div>

          <h1 className="playlist-hero__name">{details?.playlistName ?? '—'}</h1>
          {details?.description && (
            <p className="playlist-hero__description">{details.description}</p>
          )}
          <div className="playlist-hero__stats">
            <span>{details?.totalSongs ?? 0} songs</span>
            <span className="playlist-hero__dot">·</span>
            <span>{formatTotalDuration(totalDuration)}</span>
          </div>
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

      <SongTable songs={songs} readOnly={!isAuthenticated} />
      <div ref={sentinelRef} style={{ height: 1 }} />
      {loading && <p className="songs-status">Loading…</p>}
      {!hasMore && songs.length > 0 && (
        <p className="songs-status songs-status--end">All songs loaded</p>
      )}
    </div>
  )
}

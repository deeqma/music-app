import { useState, useEffect, useRef } from 'react'
import { useOutletContext, useLocation } from 'react-router-dom'
import type { SongDto } from '../../core/auth/contracts'
import { songApi } from '../../core/auth/songApi'
import { usePlayerStore } from '../../core/store/playerStore'
import SongTable from '../components/SongTable'
import FilterPanel from '../components/FilterPanel'
import Icon from '../components/Icon'
import filterRaw from '../assets/filter.svg?raw'
import { DEFAULT_FILTER, isFilterActive } from '../../core/lib/filterUtils'
import type { FilterState } from '../../core/lib/filterUtils'

const PAGE_SIZE = 15

export default function LikedSongsPage() {
  const { searchQuery } = useOutletContext<{ searchQuery: string }>()
  const location = useLocation()
  const scrollToSongIdRef = useRef<number | null>(
    (location.state as { scrollToSongId?: number } | null)?.scrollToSongId ?? null
  )

  const [allLoaded, setAllLoaded] = useState<SongDto[]>([])
  const [page,      setPage]      = useState(0)
  const [hasMore,   setHasMore]   = useState(true)
  const [loading,   setLoading]   = useState(false)

  const [debouncedQuery, setDebouncedQuery] = useState(searchQuery)

  const [filterOpen,      setFilterOpen]      = useState(false)
  const [filter,          setFilter]          = useState<FilterState>(DEFAULT_FILTER)
  const [debouncedFilter, setDebouncedFilter] = useState<FilterState>(DEFAULT_FILTER)

  const loadingRef  = useRef(false)
  const pageRef     = useRef(0)
  const hasMoreRef  = useRef(true)
  const queryRef    = useRef('')
  const filterRef   = useRef<FilterState>(DEFAULT_FILTER)
  const sentinelRef = useRef<HTMLDivElement>(null)

  const likedIds        = usePlayerStore(s => s.likedIds)
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

  // Only show songs that are still liked — unlike removes them instantly
  const songs = allLoaded.filter(s => likedIds.has(s.id))

  async function loadPage(p: number, q: string, f: FilterState, reset = false) {
    if (loadingRef.current) return
    loadingRef.current = true
    setLoading(true)
    try {
      let data: SongDto[]
      if (q) {
        data = await songApi.searchLiked(q, p, PAGE_SIZE)
      } else {
        data = await songApi.getLiked({ ...f, page: p, pageSize: PAGE_SIZE })
      }
      mergeLikedSongs(data)
      if (data.length < PAGE_SIZE) { hasMoreRef.current = false; setHasMore(false) }
      setAllLoaded(prev => reset ? data : [...prev, ...data])
      setPage(p + 1)
    } catch {
      // keep current state on error
    } finally {
      loadingRef.current = false
      setLoading(false)
    }
  }

  useEffect(() => {
    setAllLoaded([])
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

  useEffect(() => {
    const id = scrollToSongIdRef.current
    if (!id) return
    if (songs.some(s => s.id === id)) {
      requestAnimationFrame(() => {
        document.querySelector<HTMLElement>(`[data-song-id="${id}"]`)
          ?.scrollIntoView({ behavior: 'smooth', block: 'center' })
      })
      scrollToSongIdRef.current = null
    } else if (hasMoreRef.current && !loadingRef.current) {
      loadPage(pageRef.current, queryRef.current, filterRef.current)
    } else {
      scrollToSongIdRef.current = null
    }
  }, [songs]) // eslint-disable-line react-hooks/exhaustive-deps

  const active = isFilterActive(filter)

  return (
    <>
      <div className="song-twf__bar">
        <button
          className={`song-twf__filter-btn${filterOpen ? ' song-twf__filter-btn--open' : ''}`}
          onClick={() => setFilterOpen(o => !o)}
          aria-label="Toggle filter"
        >
          <Icon src={filterRaw} size={14} color="accent" alt="filter" />
          <span>Filter</span>
        </button>

        {active && (
          <button
            className="song-twf__clear-btn"
            onClick={() => setFilter(DEFAULT_FILTER)}
          >
            Clear
          </button>
        )}

        <span className="song-twf__count">{songs.length} songs</span>
      </div>

      {filterOpen && (
        <FilterPanel filter={filter} onChange={setFilter} />
      )}

      <SongTable
        songs={songs}
        onSongUpdated={song => setAllLoaded(prev => prev.map(s => s.id === song.id ? song : s))}
      />
      <div ref={sentinelRef} style={{ height: 1 }} />
      {loading && <p className="songs-status">Loading…</p>}
      {!hasMore && songs.length > 0 && (
        <p className="songs-status songs-status--end">All liked songs loaded</p>
      )}
    </>
  )
}

import { useState, useEffect, useRef } from 'react'
import { useOutletContext } from 'react-router-dom'
import type { SongDto } from '../auth/contracts'
import { songApi } from '../auth/songApi'
import { usePlayerStore } from '../store/playerStore'
import SongTable from '../components/SongTable'
import FilterPanel from '../components/FilterPanel'
import Icon from '../components/Icon'
import filterRaw from '../assets/filter.svg?raw'
import { DEFAULT_FILTER, isFilterActive } from '../lib/filterUtils'
import type { FilterState } from '../lib/filterUtils'

const PAGE_SIZE = 15

export default function ExplorePage() {
  const { searchQuery } = useOutletContext<{ searchQuery: string }>()

  const [songs,   setSongs]   = useState<SongDto[]>([])
  const [page,    setPage]    = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [loading, setLoading] = useState(false)

  const [filterOpen,     setFilterOpen]     = useState(false)
  const [filter,         setFilter]         = useState<FilterState>(DEFAULT_FILTER)
  const [debouncedFilter, setDebouncedFilter] = useState<FilterState>(DEFAULT_FILTER)

  // Refs for IntersectionObserver callback — avoids stale closures
  const loadingRef  = useRef(false)
  const pageRef     = useRef(0)
  const hasMoreRef  = useRef(true)
  const queryRef    = useRef('')
  const filterRef   = useRef<FilterState>(DEFAULT_FILTER)
  const sentinelRef = useRef<HTMLDivElement>(null)

  const mergeLikedSongs = usePlayerStore(s => s.mergeLikedSongs)

  // Keep refs in sync every render
  pageRef.current    = page
  hasMoreRef.current = hasMore

  // Debounce search query — 300ms
  const [debouncedQuery, setDebouncedQuery] = useState(searchQuery)
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(searchQuery), 300)
    return () => clearTimeout(t)
  }, [searchQuery])

  // Debounce filter changes — 300ms (covers text typing and year slider drag)
  useEffect(() => {
    const t = setTimeout(() => setDebouncedFilter(filter), 300)
    return () => clearTimeout(t)
  }, [filter])

  queryRef.current  = debouncedQuery
  filterRef.current = debouncedFilter

  async function loadPage(p: number, q: string, f: FilterState, reset = false) {
    if (loadingRef.current) return
    loadingRef.current = true
    setLoading(true)
    try {
      const data = q
        ? await songApi.search(q, p, PAGE_SIZE)
        : await songApi.getAll({ ...f, page: p, pageSize: PAGE_SIZE })
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

  // Reset and reload from page 0 when search or filter changes
  useEffect(() => {
    setSongs([])
    setPage(0)
    setHasMore(true)
    loadPage(0, debouncedQuery, debouncedFilter, true)
  }, [debouncedQuery, debouncedFilter]) // eslint-disable-line react-hooks/exhaustive-deps

  // IntersectionObserver — set up once, reads fresh values from refs
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

  function handleSongDeleted(id: number) {
    setSongs(prev => prev.filter(s => s.id !== id))
  }

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

      <SongTable songs={songs} onSongDeleted={handleSongDeleted} />
      <div ref={sentinelRef} style={{ height: 1 }} />
      {loading && <p className="songs-status">Loading…</p>}
      {!hasMore && songs.length > 0 && (
        <p className="songs-status songs-status--end">All songs loaded</p>
      )}
    </>
  )
}

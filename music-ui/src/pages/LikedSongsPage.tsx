import { useState, useEffect, useRef } from 'react'
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

export default function LikedSongsPage() {
  const [allLoaded, setAllLoaded] = useState<SongDto[]>([])
  const [page,      setPage]      = useState(0)
  const [hasMore,   setHasMore]   = useState(true)
  const [loading,   setLoading]   = useState(false)

  const [filterOpen,      setFilterOpen]      = useState(false)
  const [filter,          setFilter]          = useState<FilterState>(DEFAULT_FILTER)
  const [debouncedFilter, setDebouncedFilter] = useState<FilterState>(DEFAULT_FILTER)

  const loadingRef  = useRef(false)
  const pageRef     = useRef(0)
  const hasMoreRef  = useRef(true)
  const filterRef   = useRef<FilterState>(DEFAULT_FILTER)
  const sentinelRef = useRef<HTMLDivElement>(null)

  const likedIds        = usePlayerStore(s => s.likedIds)
  const mergeLikedSongs = usePlayerStore(s => s.mergeLikedSongs)

  pageRef.current    = page
  hasMoreRef.current = hasMore

  // Debounce filter changes — 300ms
  useEffect(() => {
    const t = setTimeout(() => setDebouncedFilter(filter), 300)
    return () => clearTimeout(t)
  }, [filter])

  filterRef.current = debouncedFilter

  // Only show songs that are still liked — unlike removes them instantly
  const songs = allLoaded.filter(s => likedIds.has(s.id))

  async function loadPage(p: number, f: FilterState, reset = false) {
    if (loadingRef.current) return
    loadingRef.current = true
    setLoading(true)
    try {
      const data = await songApi.getLiked({ ...f, page: p, pageSize: PAGE_SIZE })
      mergeLikedSongs(data)
      if (data.length < PAGE_SIZE) setHasMore(false)
      setAllLoaded(prev => reset ? data : [...prev, ...data])
      setPage(p + 1)
    } catch {
      // keep current state on error
    } finally {
      loadingRef.current = false
      setLoading(false)
    }
  }

  // Reset and reload from page 0 when filter changes
  useEffect(() => {
    setAllLoaded([])
    setPage(0)
    setHasMore(true)
    loadPage(0, debouncedFilter, true)
  }, [debouncedFilter]) // eslint-disable-line react-hooks/exhaustive-deps

  // IntersectionObserver — set up once
  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const obs = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && hasMoreRef.current && !loadingRef.current) {
        loadPage(pageRef.current, filterRef.current)
      }
    }, { rootMargin: '200px' })
    obs.observe(el)
    return () => obs.disconnect()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

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

      <SongTable songs={songs} />
      <div ref={sentinelRef} style={{ height: 1 }} />
      {loading && <p className="songs-status">Loading…</p>}
      {!hasMore && songs.length > 0 && (
        <p className="songs-status songs-status--end">All liked songs loaded</p>
      )}
    </>
  )
}

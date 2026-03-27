import { useState } from 'react'
import type { SongDto } from '../mock/types'
import SongTable from './SongTable'
import FilterPanel from './FilterPanel'
import { DEFAULT_FILTER, isFilterActive } from '../lib/filterUtils'
import type { FilterState } from '../lib/filterUtils'
import Icon from './Icon'
import filterRaw from '../assets/filter.svg?raw'

interface Props {
  readonly songs: readonly SongDto[]
}

export default function SongTableWithFilter({ songs }: Props) {
  const [filterOpen, setFilterOpen] = useState(false)
  const [filter,     setFilter]     = useState<FilterState>(DEFAULT_FILTER)

  const active = isFilterActive(filter)

  const filtered = songs.filter(s => {
    if (filter.genre      && !s.genre.toLowerCase().includes(filter.genre.toLowerCase()))           return false
    if (filter.artistName && !s.artistName.toLowerCase().includes(filter.artistName.toLowerCase())) return false
    if (filter.album      && !s.album.toLowerCase().includes(filter.album.toLowerCase()))           return false
    if (s.releaseYear < filter.yearFrom || s.releaseYear > filter.yearTo)                           return false
    return true
  })

  return (
    <div className="song-twf">
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

        <span className="song-twf__count">
          {filtered.length} of {songs.length} songs
        </span>
      </div>

      {filterOpen && (
        <FilterPanel filter={filter} onChange={setFilter} />
      )}

      <SongTable songs={filtered} />
    </div>
  )
}

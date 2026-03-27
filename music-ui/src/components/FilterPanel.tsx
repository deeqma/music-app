import type { FilterState } from '../lib/filterUtils'

interface FilterPanelProps {
  readonly filter:   FilterState
  readonly onChange: (f: FilterState) => void
}

const MIN_YEAR = 1900
const MAX_YEAR = 2026

export default function FilterPanel({ filter, onChange }: FilterPanelProps) {
  function set<K extends keyof FilterState>(key: K, value: FilterState[K]) {
    onChange({ ...filter, [key]: value })
  }

  const fromPct = ((filter.yearFrom - MIN_YEAR) / (MAX_YEAR - MIN_YEAR)) * 100
  const toPct   = ((filter.yearTo   - MIN_YEAR) / (MAX_YEAR - MIN_YEAR)) * 100

  return (
    <div className="filter-panel">
      <div className="filter-panel__fields">
        <div className="filter-panel__field">
          <label htmlFor="filter-genre" className="filter-panel__label">Genre</label>
          <input
            id="filter-genre"
            className="filter-panel__input"
            value={filter.genre}
            onChange={e => set('genre', e.target.value)}
            placeholder="e.g. Rock"
          />
        </div>
        <div className="filter-panel__field">
          <label htmlFor="filter-artist" className="filter-panel__label">Artist</label>
          <input
            id="filter-artist"
            className="filter-panel__input"
            value={filter.artistName}
            onChange={e => set('artistName', e.target.value)}
            placeholder="e.g. AC/DC"
          />
        </div>
        <div className="filter-panel__field">
          <label htmlFor="filter-album" className="filter-panel__label">Album</label>
          <input
            id="filter-album"
            className="filter-panel__input"
            value={filter.album}
            onChange={e => set('album', e.target.value)}
            placeholder="e.g. Back in Black"
          />
        </div>
      </div>

      <div className="filter-panel__year">
        <div className="filter-panel__year-header">
          <span className="filter-panel__label">Year</span>
          <span className="filter-panel__year-values">
            {filter.yearFrom} &ndash; {filter.yearTo}
          </span>
        </div>

        <div className="filter-panel__slider-wrap">
          <div className="filter-panel__track" />
          <div
            className="filter-panel__track-fill"
            style={{ left: `${fromPct}%`, width: `${toPct - fromPct}%` }}
          />
          <input
            type="range"
            className="filter-panel__range"
            min={MIN_YEAR}
            max={MAX_YEAR}
            value={filter.yearFrom}
            style={{ zIndex: filter.yearFrom > MAX_YEAR - 10 ? 5 : 3 }}
            onChange={e => set('yearFrom', Math.min(Number(e.target.value), filter.yearTo - 1))}
          />
          <input
            type="range"
            className="filter-panel__range"
            min={MIN_YEAR}
            max={MAX_YEAR}
            value={filter.yearTo}
            style={{ zIndex: 4 }}
            onChange={e => set('yearTo', Math.max(Number(e.target.value), filter.yearFrom + 1))}
          />
        </div>
      </div>
    </div>
  )
}

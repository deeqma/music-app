export interface FilterState {
  genre:      string
  artistName: string
  album:      string
  yearFrom:   number
  yearTo:     number
}

export const DEFAULT_FILTER: FilterState = {
  genre: '', artistName: '', album: '', yearFrom: 1900, yearTo: 2026,
}

export function isFilterActive(f: FilterState): boolean {
  return !!(f.genre || f.artistName || f.album || f.yearFrom > 1900 || f.yearTo < 2026)
}

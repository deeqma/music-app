import type React from 'react'
import type { PlaylistSummaryDto } from '../../core/auth/contracts'

interface PlaylistDropdownProps {
  readonly playlists: readonly PlaylistSummaryDto[]
  readonly onSelect:  (playlistId: number) => void
}

export default function PlaylistDropdown({ playlists, onSelect }: PlaylistDropdownProps) {
  function handleChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const id = Number(e.target.value)
    if (id) onSelect(id)
    e.target.value = ''
  }

  return (
    <select
      className="pl-dropdown__select"
      defaultValue=""
      onChange={handleChange}
      aria-label="Select playlist"
    >
      <option value="" disabled>Select playlist…</option>
      {playlists.map(p => (
        <option key={p.playlistId} value={p.playlistId}>
          {p.playlistName}
        </option>
      ))}
    </select>
  )
}

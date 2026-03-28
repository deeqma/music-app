import { useRef, useEffect, useState } from 'react'
import type { PlaylistSummaryDto } from '../auth/contracts'

interface PlaylistDropdownProps {
  readonly playlists: readonly PlaylistSummaryDto[]
  readonly onSelect:  (playlistId: number) => void
}

export default function PlaylistDropdown({ playlists, onSelect }: PlaylistDropdownProps) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    function handleMouseDown(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleMouseDown)
    return () => document.removeEventListener('mousedown', handleMouseDown)
  }, [open])

  return (
    <div ref={ref} className="pl-dropdown">
      <button
        className={`pl-dropdown__trigger${open ? ' pl-dropdown__trigger--open' : ''}`}
        onClick={() => setOpen(o => !o)}
        aria-expanded={open}
      >
        <span>Select playlist…</span>
        <span className="pl-dropdown__chevron" aria-hidden="true">▾</span>
      </button>

      {open && (
        <div className="pl-dropdown__list" role="listbox">
          {playlists.map(p => (
            <button
              key={p.playlistId}
              className="pl-dropdown__item"
              role="option"
              aria-selected={false}
              onClick={() => { onSelect(p.playlistId); setOpen(false) }}
            >
              {p.playlistName}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

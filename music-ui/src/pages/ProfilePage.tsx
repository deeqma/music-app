import { useState, useRef } from 'react'
import Icon from '../components/Icon'
import profileRaw from '../assets/profile-circle.svg?raw'
import copyRaw    from '../assets/copy.svg?raw'
import exportRaw  from '../assets/export.svg?raw'
import logoutRaw  from '../assets/logout.svg?raw'
import { mockUser } from '../mock/userData'
import { songs }    from '../mock/songData'
import { playlists } from '../mock/playlistData'

function formatStockholmTime(iso: string): string {
  return new Date(iso).toLocaleString('en-SE', {
    timeZone:     'Europe/Stockholm',
    year:         'numeric',
    month:        'short',
    day:          'numeric',
    hour:         '2-digit',
    minute:       '2-digit',
    timeZoneName: 'short',
  })
}

function handleExport() {
  const data = {
    exportedAt: new Date().toISOString(),
    songs: songs.map(s => ({
      id:          s.id,
      songName:    s.songName,
      artistName:  s.artistName,
      album:       s.album,
      genre:       s.genre,
      releaseYear: s.releaseYear,
      liked:       s.liked,
      playlists:   playlists
        .filter(p => p.songDtos.some(sd => sd.id === s.id))
        .map(p => p.playlistName),
    })),
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = 'my-songs.json'
  a.click()
  URL.revokeObjectURL(url)
}

export default function ProfilePage() {
  const [copied, setCopied] = useState(false)
  const copyTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  function copyUUID() {
    navigator.clipboard.writeText(mockUser.userId)
    setCopied(true)
    if (copyTimerRef.current) clearTimeout(copyTimerRef.current)
    copyTimerRef.current = setTimeout(() => setCopied(false), 2000)
  }

  const playlistPct = Math.round((mockUser.totalPlaylists / mockUser.maxPlaylists) * 100)

  return (
    <div className="profile">

      {/* Hero */}
      <div className="profile__hero">
        <div className="profile__avatar">
          <Icon src={profileRaw} size={64} color="secondary" alt="profile" />
        </div>

        <div className="profile__identity">
          <button className="profile__uuid" onClick={copyUUID} title="Click to copy">
            <span className="profile__uuid-text">{mockUser.userId}</span>
            <Icon src={copyRaw} size={14} color={copied ? 'accent' : 'secondary'} alt="copy" />
            {copied && <span className="profile__uuid-copied">Copied!</span>}
          </button>
          <h1 className="profile__username">{mockUser.username}</h1>
        </div>
      </div>

      <div className="profile__body">

        {/* Stats */}
        <div className="profile__card">
          <div className="profile__stat">
            <span className="profile__stat-label">Playlists</span>
            <span className="profile__stat-value">
              <span className="profile__stat-num">{mockUser.totalPlaylists}</span>
              <span className="profile__stat-max">/ {mockUser.maxPlaylists}</span>
            </span>
          </div>
          <div className="profile__progress-track">
            <div className="profile__progress-fill" style={{ width: `${playlistPct}%` }} />
          </div>

          <div className="profile__stat profile__stat--mt">
            <span className="profile__stat-label">Liked Songs</span>
            <span className="profile__stat-num">{mockUser.totalLikedSongs}</span>
          </div>
        </div>

        {/* Export */}
        <div className="profile__card profile__card--row">
          <div className="profile__card-icon">
            <Icon src={exportRaw} size={20} color="accent" alt="export" />
          </div>
          <div className="profile__card-text">
            <span className="profile__card-title">Export Library</span>
            <span className="profile__card-sub">Download all songs with playlist names as JSON</span>
          </div>
          <button className="profile__export-btn" onClick={handleExport}>
            Export JSON
          </button>
        </div>

        {/* Session */}
        <div className="profile__card profile__card--row profile__card--muted">
          <div className="profile__card-text">
            <span className="profile__card-title">Session expires</span>
            <span className="profile__card-sub profile__card-sub--mono">
              {formatStockholmTime(mockUser.sessionExpiresAt)}
            </span>
          </div>
        </div>

        {/* Logout */}
        <button className="profile__logout-btn">
          <Icon src={logoutRaw} size={18} color="secondary" alt="logout" />
          <span>Log out</span>
        </button>

        {/* Danger zone */}
        <div className="profile__danger-zone">
          <span className="profile__danger-title">Danger Zone</span>
          <button className="profile__delete-btn">Delete Account</button>
        </div>

      </div>
    </div>
  )
}

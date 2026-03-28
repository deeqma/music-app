import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import Icon from '../components/Icon'
import profileRaw from '../assets/profile-circle.svg?raw'
import copyRaw    from '../assets/copy.svg?raw'
import logoutRaw  from '../assets/logout.svg?raw'
import { clearToken, getTokenPayload } from '../auth/authToken'

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

export default function ProfilePage() {
  const navigate = useNavigate()
  const [copied, setCopied] = useState(false)
  const copyTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const user = getTokenPayload()

  function handleLogout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  function copyUUID() {
    navigator.clipboard.writeText(user?.userId ?? '')
    setCopied(true)
    if (copyTimerRef.current) clearTimeout(copyTimerRef.current)
    copyTimerRef.current = setTimeout(() => setCopied(false), 2000)
  }

  const playlistPct = 0

  return (
    <div className="profile">

      {/* Hero */}
      <div className="profile__hero">
        <div className="profile__avatar">
          <Icon src={profileRaw} size={64} color="secondary" alt="profile" />
        </div>

        <div className="profile__identity">
          <button className="profile__uuid" onClick={copyUUID} title="Click to copy">
            <span className="profile__uuid-text">{user?.userId ?? '—'}</span>
            <Icon src={copyRaw} size={14} color={copied ? 'accent' : 'secondary'} alt="copy" />
            {copied && <span className="profile__uuid-copied">Copied!</span>}
          </button>
          <h1 className="profile__username">{user?.sub ?? '—'}</h1>
        </div>
      </div>

      <div className="profile__body">

        {/* Stats */}
        <div className="profile__card">
          <div className="profile__stat">
            <span className="profile__stat-label">Playlists</span>
            <span className="profile__stat-value">
              <span className="profile__stat-num">—</span>
              <span className="profile__stat-max"></span>
            </span>
          </div>
          <div className="profile__progress-track">
            <div className="profile__progress-fill" style={{ width: `${playlistPct}%` }} />
          </div>

          <div className="profile__stat profile__stat--mt">
            <span className="profile__stat-label">Liked Songs</span>
            <span className="profile__stat-num">—</span>
          </div>
        </div>


        {/* Session */}
        <div className="profile__card profile__card--row profile__card--muted">
          <div className="profile__card-text">
            <span className="profile__card-title">Session expires</span>
            <span className="profile__card-sub profile__card-sub--mono">
              {user ? formatStockholmTime(new Date(user.exp * 1000).toISOString()) : '—'}
            </span>
          </div>
        </div>

        {/* Logout */}
        <button className="profile__logout-btn" onClick={handleLogout}>
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

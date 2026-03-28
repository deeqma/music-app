import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Icon from '../components/Icon'
import profileRaw from '../assets/profile-circle.svg?raw'
import copyRaw    from '../assets/copy.svg?raw'
import logoutRaw  from '../assets/logout.svg?raw'
import { clearToken, getTokenPayload } from '../auth/authToken'
import { userApi } from '../auth/userApi'
import type { UserProfileDto } from '../auth/contracts'

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
  const [copied,       setCopied]       = useState(false)
  const [profile,      setProfile]      = useState<UserProfileDto | null>(null)
  const [deleting,     setDeleting]     = useState(false)
  const [deleteError,  setDeleteError]  = useState('')
  const copyTimerRef   = useRef<ReturnType<typeof setTimeout> | null>(null)
  const deleteErrTimer = useRef<ReturnType<typeof setTimeout> | null>(null)

  const jwtPayload = getTokenPayload()
  const role       = jwtPayload?.role ?? '—'
  const expIso     = jwtPayload ? new Date(jwtPayload.exp * 1000).toISOString() : null

  useEffect(() => {
    userApi.getProfile()
      .then(setProfile)
      .catch((err: { errorType?: string; status?: number }) => {
        if (err.errorType === 'NOT_FOUND' || err.status === 404) {
          clearToken()
          navigate('/login', { replace: true })
        }
      })
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!deleteError) return
    if (deleteErrTimer.current) clearTimeout(deleteErrTimer.current)
    deleteErrTimer.current = setTimeout(() => setDeleteError(''), 3000)
  }, [deleteError])

  function handleLogout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  function copyUUID() {
    navigator.clipboard.writeText(profile?.userId ?? '')
    setCopied(true)
    if (copyTimerRef.current) clearTimeout(copyTimerRef.current)
    copyTimerRef.current = setTimeout(() => setCopied(false), 2000)
  }

  async function handleDeleteAccount() {
    if (deleting) return
    setDeleting(true)
    setDeleteError('')
    try {
      await userApi.deleteAccount()
      clearToken()
      navigate('/login', { replace: true })
    } catch (err) {
      const e = err as { message?: string }
      setDeleteError(e.message ?? 'Failed to delete account')
      setDeleting(false)
    }
  }

  const totalPlaylists  = profile?.totalPlaylists  ?? 0
  const playlistLimit   = profile?.playlistLimit   ?? 0
  const totalLikedSongs = profile?.totalLikedSongs ?? 0
  const playlistPct     = playlistLimit > 0 ? Math.min((totalPlaylists / playlistLimit) * 100, 100) : 0

  return (
    <div className="profile">

      {/* Hero */}
      <div className="profile__hero">
        <div className="profile__avatar">
          <Icon src={profileRaw} size={64} color="secondary" alt="profile" />
        </div>

        <div className="profile__identity">
          <button className="profile__uuid" onClick={copyUUID} title="Click to copy">
            <span className="profile__uuid-text">{profile?.userId ?? '—'}</span>
            <Icon src={copyRaw} size={14} color={copied ? 'accent' : 'secondary'} alt="copy" />
            {copied && <span className="profile__uuid-copied">Copied!</span>}
          </button>
          <h1 className="profile__username">{profile?.username ?? '—'}</h1>
        </div>
      </div>

      <div className="profile__body">

        {/* Stats */}
        <div className="profile__card">
          <div className="profile__stat">
            <span className="profile__stat-label">Playlists</span>
            <span className="profile__stat-value">
              <span className="profile__stat-num">{totalPlaylists}</span>
              <span className="profile__stat-max">/ {playlistLimit}</span>
            </span>
          </div>
          <div className="profile__progress-track">
            <div className="profile__progress-fill" style={{ width: `${playlistPct}%` }} />
          </div>

          <div className="profile__stat profile__stat--mt">
            <span className="profile__stat-label">Liked Songs</span>
            <span className="profile__stat-num">{totalLikedSongs}</span>
          </div>
        </div>

        {/* Role */}
        <div className="profile__card profile__card--row profile__card--muted">
          <div className="profile__card-text">
            <span className="profile__card-title">Role</span>
            <span className="profile__card-sub">{role}</span>
          </div>
        </div>

        {/* Session */}
        <div className="profile__card profile__card--row profile__card--muted">
          <div className="profile__card-text">
            <span className="profile__card-title">Session expires</span>
            <span className="profile__card-sub profile__card-sub--mono">
              {expIso ? formatStockholmTime(expIso) : '—'}
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
          <button
            className="profile__delete-btn"
            onClick={handleDeleteAccount}
            disabled={deleting}
          >
            {deleting ? 'Deleting…' : 'Delete Account'}
          </button>
          {deleteError && <p className="profile__danger-error">{deleteError}</p>}
        </div>

      </div>
    </div>
  )
}

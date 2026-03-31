import { useState, useEffect } from 'react'
import { NavLink } from 'react-router-dom'
import Icon from '../components/Icon'
import ImportModal from '../components/ImportModal'
import CreatePlaylistModal from '../components/CreatePlaylistModal'
import radioRaw       from '../assets/radio.svg?raw'
import exploreRaw     from '../assets/explore.svg?raw'
import heartRaw       from '../assets/heart.svg?raw'
import importRaw      from '../assets/import.svg?raw'
import playlistRaw    from '../assets/playlist.svg?raw'
import profileRaw     from '../assets/profile-circle.svg?raw'
import expandDownRaw  from '../assets/expand-circle-down-rounded.svg?raw'
import expandUpRaw    from '../assets/expand-circle-up-rounded.svg?raw'
import musicRaw       from '../assets/music.svg?raw'
import plusRaw        from '../assets/plus.svg?raw'
import { playlistApi } from '../../core/auth/playlistApi'
import { usePlaylistsStore } from '../../core/store/playlistsStore'
import { isAdmin } from '../../core/auth/authToken'

function navClass({ isActive }: { isActive: boolean }) {
  return `sidebar__nav-item${isActive ? ' sidebar__nav-item--active' : ''}`
}

function playlistClass({ isActive }: { isActive: boolean }) {
  return `sidebar__playlist-item${isActive ? ' sidebar__playlist-item--active' : ''}`
}

export default function Sidebar() {
  const [playlistsOpen, setPlaylistsOpen] = useState(true)
  const [importOpen,    setImportOpen]    = useState(false)
  const [createOpen,    setCreateOpen]    = useState(false)
  const [open,          setOpen]          = useState(() => window.innerWidth >= 1100)
  const [onlyMine,      setOnlyMine]      = useState(() => localStorage.getItem('playlist-only-mine') === 'true')

  const { playlists, setPlaylists } = usePlaylistsStore()

  function toggleOnlyMine() {
    setOnlyMine(prev => {
      const next = !prev
      localStorage.setItem('playlist-only-mine', String(next))
      return next
    })
  }

  const visiblePlaylists = onlyMine ? playlists.filter(p => p.owner) : playlists

  useEffect(() => {
    document.documentElement.dataset.sidebar = open ? 'open' : 'collapsed'
  }, [open])

  // Load playlists from API on mount
  useEffect(() => {
    playlistApi.getAll().then(setPlaylists).catch(() => {})
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <aside className="sidebar">
      {isAdmin() && importOpen && <ImportModal onClose={() => setImportOpen(false)} />}
      {createOpen  && <CreatePlaylistModal  onClose={() => setCreateOpen(false)}  />}

      {/* Toggle row — always visible */}
      <div className="sidebar__header">
        <span className="sidebar__title">Music App</span>
        <button
          className="sidebar__toggle"
          onClick={() => setOpen(o => !o)}
          aria-label={open ? 'Collapse sidebar' : 'Expand sidebar'}
        >
          <Icon src={radioRaw} size={20} color="accent" />
        </button>
      </div>

      {/* Everything below is hidden when collapsed */}
      <div className="sidebar__content">
        <hr className="sidebar__divider" />

        <nav className="sidebar__nav">
          <NavLink to="/explore" className={navClass}>
            <Icon src={exploreRaw} size={18} color="accent" alt="explore" />
            <span>Explore</span>
          </NavLink>

          <NavLink to="/liked-songs" className={navClass}>
            <Icon src={heartRaw} size={18} color="accent" alt="liked songs" />
            <span>Liked Songs</span>
          </NavLink>

          {isAdmin() && (
            <button className="sidebar__nav-item" onClick={() => setImportOpen(true)}>
              <Icon src={importRaw} size={18} color="accent" alt="import" />
              <span>Import</span>
            </button>
          )}
        </nav>

        <hr className="sidebar__divider" />

        <div className="sidebar__playlists">
          <div className="sidebar__playlists-header-row">
            <button
              className="sidebar__playlists-header"
              onClick={() => setPlaylistsOpen(o => !o)}
            >
              <Icon src={playlistRaw} size={16} color="accent" alt="playlists" />
              <span>PLAYLISTS</span>
              <Icon
                src={playlistsOpen ? expandUpRaw : expandDownRaw}
                size={16}
                color="accent"
                alt={playlistsOpen ? 'collapse' : 'expand'}
              />
            </button>
            <button
              className="sidebar__create-playlist-btn"
              onClick={() => setCreateOpen(true)}
              aria-label="Create playlist"
              title="Create playlist"
            >
              <Icon src={plusRaw} size={14} color="accent" alt="create playlist" />
            </button>
          </div>

          <label className="sidebar__only-mine">
            <input
              type="checkbox"
              className="sidebar__only-mine-check"
              checked={onlyMine}
              onChange={toggleOnlyMine}
            />
            <span>Only my playlists</span>
          </label>

          {playlistsOpen && (
            <ul className="sidebar__playlist-list">
              {visiblePlaylists.map(pl => (
                <li key={pl.playlistId}>
                  <NavLink
                    to={`/playlist/${pl.slug}`}
                    className={playlistClass}
                  >
                    <Icon src={musicRaw} size={14} color="accent" alt="playlist" />
                    {pl.playlistName}
                  </NavLink>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="sidebar__bottom">
          <hr className="sidebar__divider" />
          <NavLink to="/profile" className={navClass}>
            <Icon src={profileRaw} size={20} color="accent" alt="profile" />
            <span>Profile</span>
          </NavLink>
        </div>
      </div>
    </aside>
  )
}

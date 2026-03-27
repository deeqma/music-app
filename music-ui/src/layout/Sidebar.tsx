import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import Icon from '../components/Icon'
import ImportModal from '../components/ImportModal'
import radioRaw       from '../assets/radio.svg?raw'
import exploreRaw     from '../assets/explore.svg?raw'
import heartRaw       from '../assets/heart.svg?raw'
import importRaw      from '../assets/import.svg?raw'
import playlistRaw    from '../assets/playlist.svg?raw'
import profileRaw     from '../assets/profile-circle.svg?raw'
import expandDownRaw  from '../assets/expand-circle-down-rounded.svg?raw'
import expandUpRaw    from '../assets/expand-circle-up-rounded.svg?raw'
import musicRaw       from '../assets/music.svg?raw'
import { playlists }  from '../mock/playlistData'

function navClass({ isActive }: { isActive: boolean }) {
  return `sidebar__nav-item${isActive ? ' sidebar__nav-item--active' : ''}`
}

function playlistClass({ isActive }: { isActive: boolean }) {
  return `sidebar__playlist-item${isActive ? ' sidebar__playlist-item--active' : ''}`
}

export default function Sidebar() {
  const [playlistsOpen, setPlaylistsOpen] = useState(true)
  const [importOpen,    setImportOpen]    = useState(false)

  return (
    <aside className="sidebar">
      {importOpen && <ImportModal onClose={() => setImportOpen(false)} />}
      <div className="sidebar__header">
        <span className="sidebar__title">Music App</span>
        <Icon src={radioRaw} size={20} color="accent" alt="radio" />
      </div>

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

        <button className="sidebar__nav-item" onClick={() => setImportOpen(true)}>
          <Icon src={importRaw} size={18} color="accent" alt="import" />
          <span>Import</span>
        </button>
      </nav>

      <hr className="sidebar__divider" />

      <div className="sidebar__playlists">
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

        {playlistsOpen && (
          <ul className="sidebar__playlist-list">
            {playlists.map(pl => (
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
    </aside>
  )
}

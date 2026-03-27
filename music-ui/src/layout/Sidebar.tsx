import { useState } from 'react'
import Icon from '../components/Icon'
import radioRaw from '../assets/radio.svg?raw'
import exploreRaw from '../assets/explore.svg?raw'
import heartRaw from '../assets/heart.svg?raw'
import playlistRaw from '../assets/playlist.svg?raw'
import profileRaw from '../assets/profile-circle.svg?raw'
import expandDownRaw from '../assets/expand-circle-down-rounded.svg?raw'
import expandUpRaw from '../assets/expand-circle-up-rounded.svg?raw'
import musicRaw from '../assets/music.svg?raw'

export type Page = 'explore' | 'liked-songs' | 'profile' | `playlist:${string}`

const MOCK_PLAYLISTS = [
  { id: '1', name: 'Rock' },
  { id: '2', name: 'Blues' },
]

interface SidebarProps {
  page: Page
  onNavigate: (page: Page) => void
}

export default function Sidebar({ page, onNavigate }: SidebarProps) {
  const [playlistsOpen, setPlaylistsOpen] = useState(true)

  return (
    <aside className="sidebar">
      <div className="sidebar__header">
        <span className="sidebar__title">Music App</span>
        <Icon src={radioRaw} size={20} color="accent" alt="radio" />
      </div>

      <hr className="sidebar__divider" />

      <nav className="sidebar__nav">
        <button
          className={`sidebar__nav-item${page === 'explore' ? ' sidebar__nav-item--active' : ''}`}
          onClick={() => onNavigate('explore')}
        >
          <Icon src={exploreRaw} size={18} color="accent" alt="explore" />
          <span>Explore</span>
        </button>

        <button
          className={`sidebar__nav-item${page === 'liked-songs' ? ' sidebar__nav-item--active' : ''}`}
          onClick={() => onNavigate('liked-songs')}
        >
          <Icon src={heartRaw} size={18} color="accent" alt="liked songs" />
          <span>Liked Songs</span>
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
            {MOCK_PLAYLISTS.map(pl => {
              const plPage: Page = `playlist:${pl.name}`
              const isActive = page === plPage
              return (
                <li
                  key={pl.id}
                  className={`sidebar__playlist-item${isActive ? ' sidebar__playlist-item--active' : ''}`}
                  onClick={() => onNavigate(plPage)}
                >
                  <Icon src={musicRaw} size={14} color="accent" alt="playlist" />
                  {pl.name}
                </li>
              )
            })}
          </ul>
        )}
      </div>

      <div className="sidebar__bottom">
        <hr className="sidebar__divider" />
        <button
          className={`sidebar__nav-item${page === 'profile' ? ' sidebar__nav-item--active' : ''}`}
          onClick={() => onNavigate('profile')}
        >
          <Icon src={profileRaw} size={20} color="accent" alt="profile" />
          <span>Profile</span>
        </button>
      </div>
    </aside>
  )
}

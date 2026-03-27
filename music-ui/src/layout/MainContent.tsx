import { Outlet, useLocation } from 'react-router-dom'
import Icon from '../components/Icon'
import searchRaw from '../assets/search.svg?raw'

function usePageLabel(): string {
  const { pathname } = useLocation()

  if (pathname === '/explore')     return 'Explore'
  if (pathname === '/liked-songs') return 'Liked Songs'
  if (pathname === '/profile')     return 'Profile'
  return ''
}

export default function MainContent() {
  const label      = usePageLabel()
  const { pathname } = useLocation()
  const isPlaylist  = pathname.startsWith('/playlist/') || pathname === '/profile'

  return (
    <main className="main-content">
      {!isPlaylist && (
        <>
          <div className="main-content__topbar">
            <span className="main-content__page-label">{label}</span>

            <div className="main-content__search">
              <Icon src={searchRaw} size={18} color="accent" alt="search" />
              <input
                type="text"
                className="main-content__search-input"
                placeholder="Search..."
              />
            </div>

          </div>

          <hr className="main-content__divider" />
        </>
      )}

      <div className="main-content__body">
        <Outlet />
      </div>
    </main>
  )
}

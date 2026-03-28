import { useState, useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import Icon from '../components/Icon'
import searchRaw from '../assets/search.svg?raw'
import AudioContainer from '../AudioContainer'

function usePageLabel(): string {
  const { pathname } = useLocation()
  if (pathname === '/explore')     return 'Explore'
  if (pathname === '/liked-songs') return 'Liked Songs'
  if (pathname === '/profile')     return 'Profile'
  return ''
}

export default function MainContent() {
  const label     = usePageLabel()
  const { pathname } = useLocation()
  const isPlaylist  = pathname.startsWith('/playlist/') || pathname === '/profile'

  const [searchQuery, setSearchQuery] = useState('')

  // Clear search when navigating to a different page
  useEffect(() => {
    setSearchQuery('')
  }, [pathname])

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
                placeholder="Search by song name or artist…"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          <hr className="main-content__divider" />
        </>
      )}

      <div className="main-content__body-wrapper">
        <div className="main-content__body">
          <Outlet context={{ searchQuery }} />
        </div>
      </div>

      <AudioContainer />
    </main>
  )
}

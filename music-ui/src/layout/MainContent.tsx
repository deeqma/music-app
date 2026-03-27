import Icon from '../components/Icon'
import searchRaw from '../assets/search.svg?raw'
import filterRaw from '../assets/filter.svg?raw'
import type { Page } from './Sidebar'

function getPageLabel(page: Page): string {
  if (page === 'explore')      return 'Explore'
  if (page === 'liked-songs')  return 'Liked Songs'
  if (page === 'profile')      return 'Profile'
  if (page.startsWith('playlist:')) return page.slice('playlist:'.length)
  return page
}

interface MainContentProps {
  page: Page
}

export default function MainContent({ page }: MainContentProps) {
  return (
    <main className="main-content">
      <div className="main-content__topbar">
        <span className="main-content__page-label">{getPageLabel(page)}</span>

        <div className="main-content__search">
          <Icon src={searchRaw} size={18} color="accent" alt="search" />
          <input
            type="text"
            className="main-content__search-input"
            placeholder="Search..."
          />
        </div>

        <button className="main-content__filter-btn" aria-label="Filter">
          <Icon src={filterRaw} size={18} color="accent" alt="filter" />
        </button>
      </div>

      <hr className="main-content__divider" />

      <div className="main-content__body" />
    </main>
  )
}

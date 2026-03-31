import { useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { useTheme } from '../../core/hooks/useTheme'
import { getToken } from '../../core/auth/authToken'
import Icon from '../components/Icon'
import darkModeRaw  from '../assets/dark-mode.svg?raw'
import lightModeRaw from '../assets/light-mode.svg?raw'
import AudioContainer from '../AudioContainer'
import Sidebar from './Sidebar'
import TopBar from './TopBar'
import MainContent from './MainContent'

const THEMES = [
  { value: 'storm'  as const, label: 'Storm'  },
  { value: 'forest' as const, label: 'Forest' },
  { value: 'ember'  as const, label: 'Ember'  },
]

export default function ShareLayout() {
  const navigate = useNavigate()
  const { theme, mode, setTheme, toggleMode } = useTheme()
  const [themeMenuOpen, setThemeMenuOpen] = useState(false)

  // Authenticated users see the full app layout with sidebar/navbar
  if (getToken()) {
    return (
      <div className="app">
        <Sidebar />
        <TopBar theme={theme} mode={mode} setTheme={setTheme} toggleMode={toggleMode} />
        <MainContent />
      </div>
    )
  }

  // Unauthenticated: minimal share layout
  return (
    <div className="share-app">

      <header className="share-topbar">
        <span className="share-topbar__brand">Music App</span>

        <div className="share-topbar__actions">

          <div className="topbar__theme-selector">
            <button
              className="topbar__theme-btn"
              onClick={() => setThemeMenuOpen(o => !o)}
            >
              Theme
            </button>
            {themeMenuOpen && (
              <ul className="topbar__theme-menu">
                {THEMES.map(t => (
                  <li key={t.value}>
                    <button
                      className={`topbar__theme-option${theme === t.value ? ' topbar__theme-option--active' : ''}`}
                      onClick={() => { setTheme(t.value); setThemeMenuOpen(false) }}
                    >
                      {t.label}
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <button
            className="topbar__toggle-btn"
            onClick={toggleMode}
            aria-label={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            <Icon
              src={mode === 'dark' ? lightModeRaw : darkModeRaw}
              size={22}
              color="accent"
              alt={mode === 'dark' ? 'light mode' : 'dark mode'}
            />
          </button>

          <button
            className="share-topbar__login-btn"
            onClick={() => navigate('/login')}
          >
            Log in
          </button>

        </div>
      </header>

      <div className="share-content">
        <div className="share-content__body">
          <Outlet />
        </div>
      </div>

      <div className="share-audio-wrapper">
        <AudioContainer />
      </div>
    </div>
  )
}

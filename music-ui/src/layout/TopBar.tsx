import { useState } from 'react'
import Icon from '../components/Icon'
import darkModeRaw from '../assets/dark-mode.svg?raw'
import lightModeRaw from '../assets/light-mode.svg?raw'
import type { ThemeName, ThemeMode } from '../hooks/useTheme'

const THEMES: { value: ThemeName; label: string }[] = [
  { value: 'storm', label: 'Storm' },
  { value: 'forest', label: 'Forest' },
  { value: 'ember', label: 'Ember' },
]

interface TopBarProps {
  theme: ThemeName
  mode: ThemeMode
  setTheme: (t: ThemeName) => void
  toggleMode: () => void
}

export default function TopBar({ theme, mode, setTheme, toggleMode }: TopBarProps) {
  const [themeMenuOpen, setThemeMenuOpen] = useState(false)

  const handleThemeSelect = (t: ThemeName) => {
    setTheme(t)
    setThemeMenuOpen(false)
  }

  return (
    <header className="topbar">
      <div className="topbar__actions">
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
                    onClick={() => handleThemeSelect(t.value)}
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
      </div>
    </header>
  )
}

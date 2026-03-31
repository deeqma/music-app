import { useState } from 'react'

export type ThemeName = 'ember' | 'forest' | 'storm'
export type ThemeMode = 'dark' | 'light'

const THEME_KEY = 'music-app-theme'
const MODE_KEY  = 'music-app-mode'

function applyTheme(theme: ThemeName, mode: ThemeMode) {
  document.documentElement.dataset.theme = `${theme}-${mode}`
}

export function useTheme() {
  const [theme, setTheme] = useState<ThemeName>(
    () => (localStorage.getItem(THEME_KEY) as ThemeName) ?? 'ember'
  )

  const [mode, setMode] = useState<ThemeMode>(
    () => (localStorage.getItem(MODE_KEY) as ThemeMode) ?? 'dark'
  )

  function updateTheme(t: ThemeName) {
    localStorage.setItem(THEME_KEY, t)
    applyTheme(t, mode)
    setTheme(t)
  }

  const toggleMode = () => {
    const next: ThemeMode = mode === 'dark' ? 'light' : 'dark'
    localStorage.setItem(MODE_KEY, next)
    applyTheme(theme, next)
    setMode(next)
  }

  return { theme, mode, setTheme: updateTheme, toggleMode }
}

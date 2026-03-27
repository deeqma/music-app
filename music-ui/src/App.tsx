import { useState } from 'react'
import { useTheme } from './hooks/useTheme'
import Sidebar, { type Page } from './layout/Sidebar'
import TopBar from './layout/TopBar'
import MainContent from './layout/MainContent'

export default function App() {
  const { theme, mode, setTheme, toggleMode } = useTheme()
  const [page, setPage] = useState<Page>('explore')

  return (
    <div className="app">
      <Sidebar page={page} onNavigate={setPage} />
      <TopBar theme={theme} mode={mode} setTheme={setTheme} toggleMode={toggleMode} />
      <MainContent page={page} />
    </div>
  )
}

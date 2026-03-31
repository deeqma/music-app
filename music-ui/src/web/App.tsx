import { useTheme } from '../core/hooks/useTheme'
import Sidebar from './layout/Sidebar'
import TopBar from './layout/TopBar'
import MainContent from './layout/MainContent'

export default function App() {
  const { theme, mode, setTheme, toggleMode } = useTheme()

  return (
    <div className="app">
      <Sidebar />
      <TopBar theme={theme} mode={mode} setTheme={setTheme} toggleMode={toggleMode} />
      <MainContent />
    </div>
  )
}

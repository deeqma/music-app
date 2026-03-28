import { Outlet } from 'react-router-dom'
import TopBar from './TopBar'
import { useTheme } from '../hooks/useTheme'

export default function AuthLayout() {
  const { theme, mode, setTheme, toggleMode } = useTheme()

  return (
    <div className="auth-layout">
      <TopBar theme={theme} mode={mode} setTheme={setTheme} toggleMode={toggleMode} />
      <main className="auth-layout__body">
        <Outlet />
      </main>
    </div>
  )
}

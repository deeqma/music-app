import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import { router } from './routes'
import './index.css'

// Apply saved theme before React renders to prevent flash of unstyled content
const savedTheme = localStorage.getItem('music-app-theme') ?? 'ember'
const savedMode  = localStorage.getItem('music-app-mode')  ?? 'dark'
document.documentElement.dataset.theme = `${savedTheme}-${savedMode}`

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)

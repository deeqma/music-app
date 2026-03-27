import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// Apply saved theme before React renders to prevent flash
const savedTheme = localStorage.getItem('music-app-theme') ?? 'ember'
const savedMode  = localStorage.getItem('music-app-mode')  ?? 'dark'
document.documentElement.setAttribute('data-theme', `${savedTheme}-${savedMode}`)

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)

import { createBrowserRouter, Navigate } from 'react-router-dom'
import App from './App'
import AuthLayout from './layout/AuthLayout'
import RequireAuth from './components/RequireAuth'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ExplorePage from './pages/ExplorePage'
import LikedSongsPage from './pages/LikedSongsPage'
import ProfilePage from './pages/ProfilePage'
import PlaylistPage from './pages/PlaylistPage'

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/login" replace /> },

  {
    element: <AuthLayout />,
    children: [
      { path: '/login',    element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },

  {
    element: <RequireAuth />,
    children: [
      {
        element: <App />,
        children: [
          { path: '/explore',        element: <ExplorePage /> },
          { path: '/liked-songs',    element: <LikedSongsPage /> },
          { path: '/profile',        element: <ProfilePage /> },
          { path: '/playlist/:name', element: <PlaylistPage /> },
        ],
      },
    ],
  },
])

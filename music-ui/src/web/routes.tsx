import { createBrowserRouter, Navigate } from 'react-router-dom'
import App from './App'
import AuthLayout from './layout/AuthLayout'
import ShareLayout from './layout/ShareLayout'
import RequireAuth from './components/RequireAuth'
import { getToken } from '../core/auth/authToken'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ExplorePage from './pages/ExplorePage'
import LikedSongsPage from './pages/LikedSongsPage'
import ProfilePage from './pages/ProfilePage'
import PlaylistPage from './pages/PlaylistPage'
import SharePage from './pages/SharePage'

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to={getToken() ? '/explore' : '/login'} replace /> },

  {
    element: <AuthLayout />,
    children: [
      { path: '/login',    element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },

  {
    element: <ShareLayout />,
    children: [
      { path: '/share/:shareToken', element: <SharePage /> },
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

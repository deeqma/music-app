import { createBrowserRouter, Navigate } from 'react-router-dom'
import App from './App'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ExplorePage from './pages/ExplorePage'
import LikedSongsPage from './pages/LikedSongsPage'
import ProfilePage from './pages/ProfilePage'
import PlaylistPage from './pages/PlaylistPage'


export const router = createBrowserRouter([
  { path: '/',         element: <LandingPage /> },
  { path: '/login',    element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },

  {
    element: <App />,
    children: [
      { index: true,              element: <Navigate to="/explore" replace /> },
      { path: '/explore',         element: <ExplorePage /> },
      { path: '/liked-songs',     element: <LikedSongsPage /> },
      { path: '/profile',         element: <ProfilePage /> },
      { path: '/playlist/:name',  element: <PlaylistPage /> },
    ],
  },
])

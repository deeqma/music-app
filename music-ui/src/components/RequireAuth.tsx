import { Navigate, Outlet } from 'react-router-dom'
import { getToken } from '../auth/authToken'

export default function RequireAuth() {
  if (!getToken()) return <Navigate to="/login" replace />
  return <Outlet />
}

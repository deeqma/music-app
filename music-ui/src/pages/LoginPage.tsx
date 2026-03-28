import { useState } from 'react'
import { Link, useNavigate, Navigate } from 'react-router-dom'
import { authApi } from '../auth/authApi'
import { saveToken, getToken } from '../auth/authToken'
import type { ErrorResponse } from '../auth/contracts'

export default function LoginPage() {
  const navigate = useNavigate()
  if (getToken()) return <Navigate to="/explore" replace />
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error,    setError]    = useState('')
  const [loading,  setLoading]  = useState(false)

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { accessToken } = await authApi.login({ username, password })
      if (!accessToken) {
        setError('No access token received. Please try again.')
        return
      }
      saveToken(accessToken)
      navigate('/explore', { replace: true })
    } catch (err) {
      const e = err as ErrorResponse
      setError(e.message ?? 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-page__card">
        <h1 className="auth-page__title">Music App</h1>
        <p className="auth-page__subtitle">Sign in to your account</p>

        <form className="auth-page__form" onSubmit={handleSubmit}>
          <div className="auth-page__field">
            <label className="auth-page__label" htmlFor="login-username">Username</label>
            <input
              id="login-username"
              className="auth-page__input"
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="Enter username"
              autoComplete="username"
              required
            />
          </div>

          <div className="auth-page__field">
            <label className="auth-page__label" htmlFor="login-password">Password</label>
            <input
              id="login-password"
              className="auth-page__input"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Enter password"
              autoComplete="current-password"
              required
            />
          </div>

          {error && <p className="auth-page__error">{error}</p>}

          <button className="auth-page__submit" type="submit" disabled={loading}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="auth-page__toggle">
          No account?{' '}
          <Link className="auth-page__link" to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}

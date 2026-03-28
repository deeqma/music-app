import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../auth/authApi'
import { clearToken } from '../auth/authToken'
import type { ErrorResponse, UserRole } from '../auth/contracts'

const ROLES: UserRole[] = ['USER', 'MODERATOR', 'ADMIN']

export default function RegisterPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [role,     setRole]     = useState<UserRole>('USER')
  const [error,    setError]    = useState('')
  const [loading,  setLoading]  = useState(false)

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.register({ username, password, role })
      // Clear any existing session — user must log in fresh
      clearToken()
      navigate('/login', { replace: true })
    } catch (err) {
      const e = err as ErrorResponse
      setError(e.message ?? 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-page__card">
        <h1 className="auth-page__title">Music App</h1>
        <p className="auth-page__subtitle">Create your account</p>

        <form className="auth-page__form" onSubmit={handleSubmit}>
          <div className="auth-page__field">
            <label className="auth-page__label" htmlFor="reg-username">Username</label>
            <input
              id="reg-username"
              className="auth-page__input"
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="Choose a username"
              autoComplete="username"
              required
            />
          </div>

          <div className="auth-page__field">
            <label className="auth-page__label" htmlFor="reg-password">Password</label>
            <input
              id="reg-password"
              className="auth-page__input"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Choose a password"
              autoComplete="new-password"
              required
            />
          </div>

          <div className="auth-page__field">
            <label className="auth-page__label">Role</label>
            <div className="auth-page__role-group">
              {ROLES.map(r => (
                <button
                  key={r}
                  type="button"
                  className={`auth-page__role-btn${role === r ? ' auth-page__role-btn--active' : ''}`}
                  onClick={() => setRole(r)}
                >
                  {r.charAt(0) + r.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          {error && <p className="auth-page__error">{error}</p>}

          <button className="auth-page__submit" type="submit" disabled={loading}>
            {loading ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="auth-page__toggle">
          Already have an account?{' '}
          <Link className="auth-page__link" to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}

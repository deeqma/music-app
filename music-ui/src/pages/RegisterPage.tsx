import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../auth/authApi'
import { saveToken } from '../auth/authToken'
import type { ErrorResponse } from '../auth/contracts'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error,    setError]    = useState('')
  const [loading,  setLoading]  = useState(false)

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.register({ username, password })
      const { accessToken } = await authApi.login({ username, password })
      saveToken(accessToken)
      navigate('/explore', { replace: true })
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

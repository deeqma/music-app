import { Link } from 'react-router-dom'

export default function LoginPage() {
  return (
    <div className="auth-page">
      <h1>Login</h1>
      <p>
        No account? <Link to="/register">Register</Link>
      </p>
    </div>
  )
}

import { Link } from 'react-router-dom'

export default function RegisterPage() {
  return (
    <div className="auth-page">
      <h1>Register</h1>
      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  )
}

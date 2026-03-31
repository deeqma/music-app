import { Link } from 'react-router-dom'

export default function LandingPage() {
  return (
    <div className="landing">
      <h1>Music App</h1>
      <div className="landing__actions">
        <Link to="/login">Login</Link>
        <Link to="/register">Register</Link>
        <Link to="/explore">Explore</Link>
      </div>
    </div>
  )
}

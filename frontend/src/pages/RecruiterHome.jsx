import { useAuth } from "../auth/AuthContext";

export default function RecruiterHome() {
  const { user, logout } = useAuth();
  return (
    <div className="card">
      <h1>Recruiter dashboard</h1>
      <p>Welcome, {user.name}.</p>
      <p>Job posting and candidate ranking are coming next.</p>
      <button onClick={logout}>Log out</button>
    </div>
  );
}

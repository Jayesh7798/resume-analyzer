import { useAuth } from "../auth/AuthContext";

export default function CandidateHome() {
  const { user, logout } = useAuth();
  return (
    <div className="card">
      <h1>Candidate dashboard</h1>
      <p>Welcome, {user.name}.</p>
      <p>Resume upload, jobs and recommendations are coming next.</p>
      <button onClick={logout}>Log out</button>
    </div>
  );
}

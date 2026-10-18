import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function CandidateLayout() {
  const { user, logout } = useAuth();

  return (
    <>
      <nav className="nav">
        <strong>Resume Analyzer</strong>
        <NavLink to="/candidate/resumes">My resumes</NavLink>
        <NavLink to="/candidate/jobs">Jobs</NavLink>
        <NavLink to="/candidate/recommended">Recommended</NavLink>
        <span className="spacer" />
        <span>{user.name}</span>
        <button className="secondary" onClick={logout}>
          Log out
        </button>
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}

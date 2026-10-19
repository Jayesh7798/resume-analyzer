import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function RecruiterLayout() {
  const { user, logout } = useAuth();

  return (
    <>
      <nav className="nav">
        <strong>Resume Analyzer</strong>
        <NavLink to="/recruiter/jobs">My jobs</NavLink>
        <NavLink to="/recruiter/post">Post a job</NavLink>
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

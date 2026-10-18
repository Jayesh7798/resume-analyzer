import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Login from "./pages/Login";
import Register from "./pages/Register";
import CandidateHome from "./pages/CandidateHome";
import RecruiterHome from "./pages/RecruiterHome";

function Home() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === "CANDIDATE") return <Navigate to="/candidate" replace />;
  if (user.role === "RECRUITER") return <Navigate to="/recruiter" replace />;
  return <p>Admin dashboard coming later.</p>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/candidate"
        element={
          <ProtectedRoute role="CANDIDATE">
            <CandidateHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/recruiter"
        element={
          <ProtectedRoute role="RECRUITER">
            <RecruiterHome />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

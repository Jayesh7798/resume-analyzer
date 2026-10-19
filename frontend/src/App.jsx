import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import CandidateLayout from "./components/CandidateLayout";
import RecruiterLayout from "./components/RecruiterLayout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ResumesPage from "./pages/ResumesPage";
import JobsPage from "./pages/JobsPage";
import RecommendedPage from "./pages/RecommendedPage";
import MyJobsPage from "./pages/MyJobsPage";
import PostJobPage from "./pages/PostJobPage";
import CandidatesPage from "./pages/CandidatesPage";

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
            <CandidateLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="resumes" replace />} />
        <Route path="resumes" element={<ResumesPage />} />
        <Route path="jobs" element={<JobsPage />} />
        <Route path="recommended" element={<RecommendedPage />} />
      </Route>

      <Route
        path="/recruiter"
        element={
          <ProtectedRoute role="RECRUITER">
            <RecruiterLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="jobs" replace />} />
        <Route path="jobs" element={<MyJobsPage />} />
        <Route path="jobs/:jobId/candidates" element={<CandidatesPage />} />
        <Route path="post" element={<PostJobPage />} />
      </Route>
    </Routes>
  );
}

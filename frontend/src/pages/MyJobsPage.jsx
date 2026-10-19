import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";

export default function MyJobsPage() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const { data } = await api.get("/jobs/mine");
        setJobs(data);
      } catch {
        setError("Could not load your jobs.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <p>Loading...</p>;

  return (
    <>
      {error && <p className="error">{error}</p>}

      {jobs.length === 0 ? (
        <div className="panel">
          <h2>My jobs</h2>
          <p>
            You haven't posted any jobs yet.{" "}
            <Link to="/recruiter/post">Post your first job</Link>.
          </p>
        </div>
      ) : (
        jobs.map((job) => (
          <div className="panel" key={job.id}>
            <h3>
              {job.title} <small className="muted">at {job.company}</small>
            </h3>
            <p className="muted">
              {job.location || "Location not specified"} · minimum experience{" "}
              {job.minExperience} yrs · posted{" "}
              {new Date(job.createdAt).toLocaleDateString()}
            </p>
            <div className="chips">
              {job.skills.map((s) => (
                <span className="chip" key={s.name}>
                  {s.name}
                </span>
              ))}
            </div>
            <Link
              className="button-link"
              to={`/recruiter/jobs/${job.id}/candidates`}
            >
              View candidates
            </Link>
          </div>
        ))
      )}
    </>
  );
}

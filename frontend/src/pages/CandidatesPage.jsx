import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../api/client";
import ScoreResult from "../components/ScoreResult";

export default function CandidatesPage() {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [open, setOpen] = useState({}); // applicationId -> details expanded

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const [jobRes, candidatesRes] = await Promise.all([
          api.get(`/jobs/${jobId}`),
          api.get(`/jobs/${jobId}/candidates`),
        ]);
        setJob(jobRes.data);
        setCandidates(candidatesRes.data);
      } catch (err) {
        if (err.response?.status === 404) {
          setError("Job not found, or it belongs to another recruiter.");
        } else {
          setError("Could not load candidates.");
        }
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [jobId]);

  function toggle(id) {
    setOpen((prev) => ({ ...prev, [id]: !prev[id] }));
  }

  if (loading) return <p>Loading...</p>;

  if (error) {
    return (
      <div className="panel">
        <p className="error">{error}</p>
        <Link to="/recruiter/jobs">Back to my jobs</Link>
      </div>
    );
  }

  return (
    <>
      <div className="panel">
        <Link to="/recruiter/jobs">&larr; Back to my jobs</Link>
        <h2>
          {job.title} <small className="muted">at {job.company}</small>
        </h2>
        <strong>Required skills</strong>
        <div className="chips">
          {job.skills.map((s) => (
            <span className="chip" key={s.name}>
              {s.name}
            </span>
          ))}
        </div>
        <p className="muted">
          Scores are decision support, not hiring decisions. Open each
          candidate's details to see why they ranked where they did, and review
          the resume yourself before deciding.
        </p>
      </div>

      {candidates.length === 0 ? (
        <div className="panel">
          <p>No applicants yet.</p>
        </div>
      ) : (
        candidates.map((c) => (
          <div className="panel" key={c.applicationId}>
            <div className="rec-head">
              <div className="rank">#{c.rank}</div>
              <div className="rec-title">
                <h3>{c.candidateName}</h3>
                <p className="muted">
                  {c.candidateEmail} · applied{" "}
                  {new Date(c.appliedAt).toLocaleDateString()} ·{" "}
                  {c.matchedSkills.length} of{" "}
                  {c.matchedSkills.length + c.missingSkills.length} required
                  skills matched
                </p>
              </div>
              <div className="score-pill">{c.overallScore}%</div>
            </div>

            <div className="row">
              <button
                className="secondary"
                onClick={() => toggle(c.applicationId)}
              >
                {open[c.applicationId] ? "Hide details" : "Show details"}
              </button>
            </div>

            {open[c.applicationId] && <ScoreResult result={c} />}
          </div>
        ))
      )}
    </>
  );
}

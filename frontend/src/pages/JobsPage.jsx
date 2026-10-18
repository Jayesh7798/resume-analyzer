import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";
import ScoreResult from "../components/ScoreResult";

export default function JobsPage() {
  const [jobs, setJobs] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [resumeId, setResumeId] = useState("");
  const [keyword, setKeyword] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [results, setResults] = useState({}); // jobId -> analysis
  const [status, setStatus] = useState({}); // jobId -> { type, text }
  const [busy, setBusy] = useState({}); // jobId -> true while a request runs

  async function loadJobs(searchText) {
    setError("");
    try {
      const { data } = await api.get("/jobs", {
        params: searchText ? { keyword: searchText } : {},
      });
      setJobs(data);
    } catch {
      setError("Could not load jobs.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    async function init() {
      try {
        const { data } = await api.get("/resumes/my");
        const analyzed = data.filter((r) => r.status === "ANALYZED");
        setResumes(analyzed);
        if (analyzed.length > 0) setResumeId(String(analyzed[0].id));
      } catch {
        setError("Could not load your resumes.");
      }
      await loadJobs("");
    }
    init();
  }, []);

  function handleSearch(e) {
    e.preventDefault();
    setLoading(true);
    loadJobs(keyword.trim());
  }

  function setJobStatus(jobId, type, text) {
    setStatus((prev) => ({ ...prev, [jobId]: { type, text } }));
  }

  function setJobBusy(jobId, value) {
    setBusy((prev) => ({ ...prev, [jobId]: value }));
  }

  function describeError(err) {
    const code = err.response?.status;
    if (code === 503)
      return "The matching service is unavailable. Try again shortly.";
    if (code === 409)
      return "That resume was not analyzed successfully. Upload it again.";
    if (code === 404) return "Job or resume not found.";
    if (!err.response)
      return "Cannot reach the server. Is the backend running?";
    return "Something went wrong. Please try again.";
  }

  async function checkMatch(jobId) {
    setJobBusy(jobId, true);
    setJobStatus(jobId, "", "");
    try {
      const { data } = await api.post(`/jobs/${jobId}/match`, {
        resumeId: Number(resumeId),
      });
      setResults((prev) => ({ ...prev, [jobId]: data }));
    } catch (err) {
      setJobStatus(jobId, "error", describeError(err));
    } finally {
      setJobBusy(jobId, false);
    }
  }

  async function apply(jobId) {
    setJobBusy(jobId, true);
    setJobStatus(jobId, "", "");
    try {
      const { data } = await api.post(`/jobs/${jobId}/apply`, {
        resumeId: Number(resumeId),
      });
      setResults((prev) => ({ ...prev, [jobId]: data.analysis }));
      setJobStatus(jobId, "success", "Application submitted.");
    } catch (err) {
      if (
        err.response?.status === 409 &&
        err.response?.data?.message === undefined
      ) {
        // 409 here means this candidate already applied to this job
        setJobStatus(jobId, "error", "You have already applied to this job.");
      } else {
        setJobStatus(jobId, "error", describeError(err));
      }
    } finally {
      setJobBusy(jobId, false);
    }
  }

  const noResume = resumes.length === 0;

  return (
    <>
      <div className="panel">
        <h2>Find jobs</h2>

        {noResume ? (
          <p className="error">
            You need an analyzed resume to check matches or apply.{" "}
            <Link to="/candidate/resumes">Upload one first</Link>.
          </p>
        ) : (
          <div className="row">
            <label htmlFor="resume">Use resume:</label>
            <select
              id="resume"
              value={resumeId}
              onChange={(e) => setResumeId(e.target.value)}
            >
              {resumes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.fileName}
                </option>
              ))}
            </select>
          </div>
        )}

        <form className="search" onSubmit={handleSearch}>
          <input
            placeholder="Search by title, company or skill..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit">Search</button>
        </form>
        {error && <p className="error">{error}</p>}
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : jobs.length === 0 ? (
        <div className="panel">
          <p>No jobs found.</p>
        </div>
      ) : (
        jobs.map((job) => (
          <div className="panel" key={job.id}>
            <h3>
              {job.title} <small className="muted">at {job.company}</small>
            </h3>
            <p className="muted">
              {job.location || "Location not specified"} · minimum experience{" "}
              {job.minExperience} yrs
            </p>
            <p>{job.description}</p>

            <div className="chips">
              {job.skills.map((s) => (
                <span className="chip" key={s.name}>
                  {s.name}
                </span>
              ))}
            </div>

            <div className="row">
              <button
                disabled={noResume || busy[job.id]}
                onClick={() => checkMatch(job.id)}
              >
                {busy[job.id] ? "Working..." : "Check my match"}
              </button>
              <button
                className="secondary"
                disabled={noResume || busy[job.id]}
                onClick={() => apply(job.id)}
              >
                Apply
              </button>
            </div>

            {status[job.id]?.text && (
              <p
                className={
                  status[job.id].type === "success" ? "success" : "error"
                }
              >
                {status[job.id].text}
              </p>
            )}

            {results[job.id] && <ScoreResult result={results[job.id]} />}
          </div>
        ))
      )}
    </>
  );
}

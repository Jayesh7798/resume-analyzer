import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/client";

export default function PostJobPage() {
  const [title, setTitle] = useState("");
  const [company, setCompany] = useState("");
  const [description, setDescription] = useState("");
  const [location, setLocation] = useState("");
  const [minExperience, setMinExperience] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [created, setCreated] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setCreated(null);
    setLoading(true);
    try {
      const { data } = await api.post("/jobs", {
        title,
        company,
        description,
        location,
        minExperience: Number(minExperience),
      });
      setCreated(data);
      setTitle("");
      setCompany("");
      setDescription("");
      setLocation("");
      setMinExperience(0);
    } catch (err) {
      const code = err.response?.status;
      if (code === 400) {
        setError("Please fill in the title, company and description.");
      } else if (code === 503) {
        setError(
          "The skill extraction service is unavailable. Make sure the AI service is running, then try again.",
        );
      } else if (!err.response) {
        setError("Cannot reach the server. Is the backend running?");
      } else {
        setError("Something went wrong. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      {created && (
        <div className="panel">
          <h2 className="success">Job posted: {created.title}</h2>
          <p>The system detected these required skills in your description:</p>
          {created.skills.length === 0 ? (
            <p className="error">
              No known skills were detected. Candidates will all score 0% on
              skills for this job. Try naming specific technologies in the
              description.
            </p>
          ) : (
            <div className="chips">
              {created.skills.map((s) => (
                <span className="chip" key={s.name}>
                  {s.name}
                </span>
              ))}
            </div>
          )}
          <Link to="/recruiter/jobs">View my jobs</Link>
        </div>
      )}

      <div className="panel">
        <h2>Post a job</h2>
        <form onSubmit={handleSubmit}>
          <label>Job title</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />

          <label>Company</label>
          <input
            value={company}
            onChange={(e) => setCompany(e.target.value)}
            required
          />

          <label>Description (mention the technologies you need)</label>
          <textarea
            rows={6}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />

          <label>Location</label>
          <input
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />

          <label>Minimum experience (years)</label>
          <input
            type="number"
            min="0"
            value={minExperience}
            onChange={(e) => setMinExperience(e.target.value)}
          />

          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? "Posting..." : "Post job"}
          </button>
        </form>
      </div>
    </>
  );
}

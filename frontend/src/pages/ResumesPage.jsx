import { useEffect, useState } from "react";
import api from "../api/client";

export default function ResumesPage() {
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const [detail, setDetail] = useState(null);

  async function loadResumes() {
    try {
      const { data } = await api.get("/resumes/my");
      setResumes(data);
    } catch {
      setError("Could not load your resumes.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadResumes();
  }, []);

  async function handleUpload(e) {
    const file = e.target.files[0];
    e.target.value = "";
    if (!file) return;

    setError("");
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      await api.post("/resumes/upload", formData);
      await loadResumes();
    } catch (err) {
      if (err.response?.status === 413) {
        setError("File is too large (max 5 MB).");
      } else if (err.response?.status === 400) {
        setError("Only PDF or DOCX files are allowed.");
      } else {
        setError("Upload failed. Please try again.");
      }
    } finally {
      setUploading(false);
    }
  }

  async function viewDetail(id) {
    setError("");
    try {
      const { data } = await api.get(`/resumes/${id}`);
      setDetail(data);
    } catch {
      setError("Could not load resume details.");
    }
  }

  return (
    <>
      <div className="panel">
        <h2>Upload a resume</h2>
        <p>
          PDF or DOCX, up to 5 MB. The system extracts the text and finds your
          skills.
        </p>
        <input
          type="file"
          accept=".pdf,.docx"
          onChange={handleUpload}
          disabled={uploading}
        />
        {uploading && <p>Uploading and analyzing...</p>}
        {error && <p className="error">{error}</p>}
      </div>

      <div className="panel">
        <h2>My resumes</h2>
        {loading ? (
          <p>Loading...</p>
        ) : resumes.length === 0 ? (
          <p>No resumes yet. Upload one above.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>File</th>
                <th>Uploaded</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {resumes.map((r) => (
                <tr key={r.id}>
                  <td>{r.fileName}</td>
                  <td>{new Date(r.uploadedAt).toLocaleString()}</td>
                  <td>
                    <span className={`badge ${r.status}`}>{r.status}</span>
                  </td>
                  <td>
                    {r.status === "ANALYZED" && (
                      <button
                        className="secondary"
                        onClick={() => viewDetail(r.id)}
                      >
                        View
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {resumes.some((r) => r.status === "FAILED") && (
          <p className="error">
            A FAILED resume could not be analyzed. Make sure the AI service is
            running, then upload it again.
          </p>
        )}
      </div>

      {detail && (
        <div className="panel">
          <h2>{detail.fileName}</h2>
          <h3>Skills found ({detail.skills.length})</h3>
          {detail.skills.length === 0 ? (
            <p>No known skills were detected in this resume.</p>
          ) : (
            <div className="chips">
              {detail.skills.map((s) => (
                <span
                  className="chip"
                  key={s.name}
                  title={`${s.category}, mentioned ${s.mentions}x`}
                >
                  {s.name}
                </span>
              ))}
            </div>
          )}
          <details>
            <summary>Extracted text</summary>
            <pre className="text">{detail.extractedText}</pre>
          </details>
          <button className="secondary" onClick={() => setDetail(null)}>
            Close
          </button>
        </div>
      )}
    </>
  );
}

export default function ScoreResult({ result }) {
  return (
    <div className="result">
      <div className="scores">
        <div>
          <span className="big">{result.overallScore}%</span>
          <small>Overall</small>
        </div>
        <div>
          <span>{result.skillScore}%</span>
          <small>Skills (50%)</small>
        </div>
        <div>
          <span>{result.semanticScore}%</span>
          <small>Semantic (30%)</small>
        </div>
        <div>
          <span>{result.fitScore}%</span>
          <small>Experience/education (20%)</small>
        </div>
      </div>

      <p>{result.explanation}</p>

      <strong>Matched skills</strong>
      <div className="chips">
        {result.matchedSkills.length === 0 ? (
          <span className="muted">None</span>
        ) : (
          result.matchedSkills.map((s) => (
            <span className="chip good" key={s}>
              {s}
            </span>
          ))
        )}
      </div>

      <strong>Missing skills</strong>
      <div className="chips">
        {result.missingSkills.length === 0 ? (
          <span className="muted">
            None. You have every skill this job lists.
          </span>
        ) : (
          result.missingSkills.map((s) => (
            <span className="chip bad" key={s}>
              {s}
            </span>
          ))
        )}
      </div>
    </div>
  );
}

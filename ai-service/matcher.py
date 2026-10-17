from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# interim weights; will be replaced by the full 3-part formula later
SKILL_WEIGHT = 0.70
TEXT_WEIGHT = 0.30


def text_similarity(resume_text: str, job_text: str) -> float:
    """TF-IDF cosine similarity, returned as 0-100."""
    if not resume_text.strip() or not job_text.strip():
        return 0.0
    vectorizer = TfidfVectorizer(stop_words="english")
    try:
        tfidf = vectorizer.fit_transform([resume_text, job_text])
    except ValueError:  # e.g. text contains only stop words
        return 0.0
    return float(cosine_similarity(tfidf[0:1], tfidf[1:2])[0][0]) * 100


def skill_match(resume_skills: list[str], job_skills: list[str]):
    """Returns (score 0-100, matched, missing). job_skills order is kept (most important first)."""
    have = {s.lower() for s in resume_skills}
    matched = [s for s in job_skills if s.lower() in have]
    missing = [s for s in job_skills if s.lower() not in have]
    score = 100.0 * len(matched) / len(job_skills) if job_skills else 0.0
    return score, matched, missing


def build_explanation(skill_score, text_score, matched, missing, job_skill_count) -> str:
    if job_skill_count == 0:
        return "No known skills were detected in the job description, so the skill score is 0."
    parts = [f"Matched {len(matched)} of {job_skill_count} skills required by the job ({skill_score:.1f}%)."]
    if missing:
        parts.append("Missing skills: " + ", ".join(missing) + ".")
    parts.append(f"Text similarity between resume and job description is {text_score:.1f}%.")
    return " ".join(parts)


def compute_match(resume_text: str, job_text: str,
                  resume_skills: list[str], job_skills: list[str]) -> dict:
    skill_score, matched, missing = skill_match(resume_skills, job_skills)
    text_score = text_similarity(resume_text, job_text)
    overall = SKILL_WEIGHT * skill_score + TEXT_WEIGHT * text_score
    return {
        "skill_score": round(skill_score, 1),
        "text_score": round(text_score, 1),
        "overall_score": round(overall, 1),
        "matched_skills": matched,
        "missing_skills": missing,
        "explanation": build_explanation(skill_score, text_score, matched, missing, len(job_skills)),
    }
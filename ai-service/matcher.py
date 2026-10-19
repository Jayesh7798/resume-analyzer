from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

from fit import compute_fit
from semantic import embed_resume, semantic_similarity

# the hybrid formula from the project document
SKILL_WEIGHT = 0.50
SEMANTIC_WEIGHT = 0.30
FIT_WEIGHT = 0.20


def keyword_similarity(resume_text: str, job_text: str) -> float:
    """TF-IDF cosine similarity (keyword baseline), returned as 0-100."""
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


def build_explanation(skill_score, semantic_score, fit_score, matched, missing,
                      job_skill_count, fit_notes) -> str:
    parts = []
    if job_skill_count == 0:
        parts.append("No known skills were detected in the job description, so the skill score is 0.")
    else:
        parts.append(f"Matched {len(matched)} of {job_skill_count} skills required by the job ({skill_score:.1f}%).")
        if missing:
            parts.append("Missing skills: " + ", ".join(missing) + ".")
    parts.append(f"Semantic similarity between resume and job description is {semantic_score:.1f}%.")
    fit_text = f"Experience/education fit is {fit_score:.0f}%."
    if fit_notes:
        fit_text += " " + " ".join(fit_notes)
    parts.append(fit_text)
    return " ".join(parts)


def compute_match(resume_text: str, job_text: str,
                  resume_skills: list[str], job_skills: list[str],
                  min_experience: int = 0, resume_embeddings=None) -> dict:
    skill_score, matched, missing = skill_match(resume_skills, job_skills)

    if resume_embeddings is None:
        resume_embeddings = embed_resume(resume_text)
    semantic_score = semantic_similarity(resume_embeddings, job_text)

    fit_score, fit_notes = compute_fit(resume_text, job_text, min_experience)
    keyword_score = keyword_similarity(resume_text, job_text)  # kept for comparison only

    overall = (SKILL_WEIGHT * skill_score
               + SEMANTIC_WEIGHT * semantic_score
               + FIT_WEIGHT * fit_score)

    return {
        "skill_score": round(skill_score, 1),
        "semantic_score": round(semantic_score, 1),
        "fit_score": round(fit_score, 1),
        "keyword_score": round(keyword_score, 1),  # not used in the overall score
        "overall_score": round(overall, 1),
        "matched_skills": matched,
        "missing_skills": missing,
        "explanation": build_explanation(skill_score, semantic_score, fit_score,
                                         matched, missing, len(job_skills), fit_notes),
    }
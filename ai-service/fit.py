import re

LEVEL_NAMES = {1: "diploma", 2: "bachelor's", 3: "master's", 4: "PhD"}

# (level, pattern); matched case-insensitively
_DEGREES_ANY_CASE = [
    (4, r"\bph\.?\s?d\b|\bdoctorate\b"),
    (3, r"\bm\.?\s?tech\b|\bmca\b|\bm\.?\s?sc\b|\bmba\b|\bmasters?\s+degree\b|\bmaster[’']?s?\s+(?:of|degree|in)\b"),
    (2, r"\bb\.?\s?tech\b|\bbca\b|\bb\.?\s?sc\b|\bbachelor[’']?s?\b"),
    (1, r"\bdiploma\b"),
]
# short abbreviations that are only safe when written in capitals, e.g. "B.E." (not the word "be")
_DEGREES_CASE_SENSITIVE = [
    (3, r"\bM\.E\b\.?"),
    (2, r"\bB\.E\b\.?"),
]

_COMPILED_ANY = [(lvl, re.compile(p, re.IGNORECASE)) for lvl, p in _DEGREES_ANY_CASE]
_COMPILED_CASE = [(lvl, re.compile(p)) for lvl, p in _DEGREES_CASE_SENSITIVE]

_EXP_BEFORE = re.compile(
    r"(\d+(?:\.\d+)?)\s*\+?\s*(?:years?|yrs?)\b[^.\n]{0,40}?experience", re.IGNORECASE)
_EXP_AFTER = re.compile(
    r"experience[^.\n]{0,40}?(\d+(?:\.\d+)?)\s*\+?\s*(?:years?|yrs?)\b", re.IGNORECASE)


def degree_levels(text: str) -> set[int]:
    found = set()
    for level, pattern in _COMPILED_ANY + _COMPILED_CASE:
        if pattern.search(text):
            found.add(level)
    return found


def years_of_experience(text: str) -> float:
    values = [float(m.group(1)) for m in _EXP_BEFORE.finditer(text)]
    values += [float(m.group(1)) for m in _EXP_AFTER.finditer(text)]
    values = [v for v in values if v <= 40]
    return max(values) if values else 0.0


def experience_fit(resume_text: str, min_experience: int):
    """Returns (score 0-100, note or None)."""
    if min_experience <= 0:
        return 100.0, None
    have = years_of_experience(resume_text)
    score = min(1.0, have / min_experience) * 100
    if have >= min_experience:
        return score, None
    found = f"{have:g} years" if have > 0 else "none"
    return score, f"The job asks for {min_experience} years of experience; found: {found}."


def education_fit(resume_text: str, job_text: str):
    """Returns (score 0-100, note or None)."""
    required_levels = degree_levels(job_text)
    if not required_levels:
        return 100.0, None
    required = min(required_levels)  # "B.Tech or M.Tech" means a bachelor's is enough
    have = max(degree_levels(resume_text), default=0)
    if have >= required:
        return 100.0, None
    return 0.0, f"The job asks for a {LEVEL_NAMES[required]} degree, which was not found in the resume."


def compute_fit(resume_text: str, job_text: str, min_experience: int):
    """Returns (fit score 0-100, list of notes)."""
    exp_score, exp_note = experience_fit(resume_text, min_experience)
    edu_score, edu_note = education_fit(resume_text, job_text)
    notes = [n for n in (exp_note, edu_note) if n]
    return (exp_score + edu_score) / 2, notes
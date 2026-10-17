from fastapi import FastAPI, File, HTTPException, UploadFile
from pydantic import BaseModel

from extractor import extract_docx_text, extract_pdf_text
from matcher import compute_match
from skills import extract_skills

app = FastAPI(title="Resume AI Service")


class TextRequest(BaseModel):
    text: str


class MatchRequest(BaseModel):
    resume_text: str
    job_text: str
    resume_skills: list[str]
    job_skills: list[str]


class RankJob(BaseModel):
    id: int
    text: str
    skills: list[str]


class RankRequest(BaseModel):
    resume_text: str
    resume_skills: list[str]
    jobs: list[RankJob]


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/analyze-resume")
async def analyze_resume(file: UploadFile = File(...)):
    name = (file.filename or "").lower()
    if not (name.endswith(".pdf") or name.endswith(".docx")):
        raise HTTPException(status_code=400, detail="Only PDF or DOCX files are supported")

    data = await file.read()
    if not data:
        raise HTTPException(status_code=400, detail="File is empty")

    try:
        if name.endswith(".pdf"):
            text = extract_pdf_text(data)
        else:
            text = extract_docx_text(data)
    except Exception:
        raise HTTPException(status_code=422, detail="Could not read the file")

    if not text:
        raise HTTPException(
            status_code=422,
            detail="No text found. The file may be a scanned image.",
        )

    return {
        "extracted_text": text,
        "char_count": len(text),
        "skills": extract_skills(text),
    }


@app.post("/extract-skills")
def extract_skills_from_text(req: TextRequest):
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Text is empty")
    return {"skills": extract_skills(req.text)}


@app.post("/match")
def match(req: MatchRequest):
    return compute_match(req.resume_text, req.job_text, req.resume_skills, req.job_skills)


@app.post("/rank")
def rank(req: RankRequest):
    results = []
    for job in req.jobs:
        scores = compute_match(req.resume_text, job.text, req.resume_skills, job.skills)
        results.append({"job_id": job.id, **scores})
    results.sort(key=lambda r: r["overall_score"], reverse=True)
    return {"results": results}
# AI Resume Analyzer & Job Matching System

Candidates upload a resume (PDF/DOCX); the system extracts skills, scores it against job postings
with an explainable match score, shows missing skills, and recommends jobs. Recruiters post jobs and
see applicants ranked by fit.

## Architecture
Spring Boot REST API (auth, jobs, resumes, applications) <-> Python FastAPI service (text extraction,
spaCy skill extraction, TF-IDF matching) <-> MySQL

## Tech stack
Java 21, Spring Boot 4, Spring Security + JWT, Spring Data JPA, MySQL, Python, FastAPI, spaCy,
scikit-learn, PyMuPDF, python-docx

## Run locally
1. Create the database: `CREATE DATABASE resume_analyzer;`
2. AI service:
```
   cd ai-service
   python -m venv venv
   venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn main:app --reload --port 8000
```
3. Backend: set environment variables `DB_PASSWORD` and `JWT_SECRET`, then run
   `ResumeAnalyzerApplication` (or `./mvnw spring-boot:run`).

## Status
Backend and matching engine complete; React frontend, semantic similarity and Docker in progress.

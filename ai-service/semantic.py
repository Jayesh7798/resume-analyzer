from sentence_transformers import SentenceTransformer, util

MODEL_NAME = "all-MiniLM-L6-v2"
CHUNK_WORDS = 120  # the model only reads ~256 word-pieces, so long resumes are split up
TOP_K = 3  # average the best 3 chunks against the job

# loaded once when the service starts
_model = SentenceTransformer(MODEL_NAME)


def split_into_chunks(text: str, size: int = CHUNK_WORDS) -> list[str]:
    words = text.split()
    return [" ".join(words[i:i + size]) for i in range(0, len(words), size)]


def embed_resume(text: str):
    """Embed each chunk of the resume once. Returns None if there is no text."""
    chunks = split_into_chunks(text)
    if not chunks:
        return None
    return _model.encode(chunks, convert_to_tensor=True, normalize_embeddings=True)


def semantic_similarity(resume_embeddings, job_text: str) -> float:
    """Similarity between the resume chunks and the job description, as 0-100."""
    if resume_embeddings is None or not job_text.strip():
        return 0.0
    job_embedding = _model.encode(job_text, convert_to_tensor=True, normalize_embeddings=True)
    sims = util.cos_sim(job_embedding, resume_embeddings)[0]  # one value per resume chunk
    top = sims.topk(min(TOP_K, sims.shape[0])).values
    return max(0.0, float(top.mean().item())) * 100
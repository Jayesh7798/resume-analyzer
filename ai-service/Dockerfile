FROM python:3.13-slim

WORKDIR /app
ENV PYTHONUNBUFFERED=1 PIP_NO_CACHE_DIR=1

# CPU-only PyTorch: the default Linux build pulls in gigabytes of GPU libraries
RUN pip install torch --index-url https://download.pytorch.org/whl/cpu

COPY requirements.docker.txt .
RUN pip install -r requirements.docker.txt

# download the embedding model now, so containers start without internet access
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"

COPY . .

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
FROM python:3.11-slim

WORKDIR /app

COPY categorization-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# pre-download model into image so container starts without network access
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')"

COPY categorization-service/ .
COPY shared/categories.json /shared/categories.json

ENV CATEGORIES_FILE=/shared/categories.json

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

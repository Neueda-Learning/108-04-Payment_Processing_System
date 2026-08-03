FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY data/ data/
COPY src/ src/

# Precompute FAQ embeddings NOW, at build time — not at container
# startup. This means the model weights and embeddings are already
# baked into the image; no network call needed when the container
# actually starts.
RUN cd /app && python src/precompute_embeddings.py

WORKDIR /app/src
EXPOSE 8000

CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8000"]
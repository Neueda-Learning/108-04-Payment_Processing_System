"""
Retrieval layer. Loads the FAQ data + precomputed embeddings ONCE at
service startup (module import time), then answers each request by
embedding only the incoming query and comparing against the cached
FAQ embeddings.

This never calls an external API and never re-embeds the FAQ corpus
per request, so it's fast and has no external failure mode.
"""
import json
import os
import numpy as np
from sentence_transformers import SentenceTransformer, util

# Resolve paths relative to this file's location (src/), not the
# current working directory — so it works regardless of where you
# run `uvicorn` or `python` from.
_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FAQ_PATH = os.path.join(_BASE_DIR, "data", "faqs.json")
EMBEDDINGS_PATH = os.path.join(_BASE_DIR, "data", "faq_embeddings.npy")

# Below this similarity score, we don't trust the match — better to
# say "I don't know" than force a bad retrieval into the prompt.
MIN_SIMILARITY_THRESHOLD = 0.35

_model = SentenceTransformer("all-MiniLM-L6-v2")

with open(FAQ_PATH, "r") as f:
    _faqs = json.load(f)

_corpus_embeddings = np.load(EMBEDDINGS_PATH)


def retrieve(query: str, top_k: int = 3):
    """
    Returns a list of matching FAQ entries, or an empty list if
    nothing scores above the similarity threshold.
    """
    query_embedding = _model.encode(query, convert_to_numpy=True)
    scores = util.cos_sim(query_embedding, _corpus_embeddings)[0]

    top_k = min(top_k, len(_faqs))
    top_results = scores.topk(top_k)

    matches = []
    for score, idx in zip(top_results.values, top_results.indices):
        if float(score) >= MIN_SIMILARITY_THRESHOLD:
            matches.append(_faqs[int(idx)])

    return matches
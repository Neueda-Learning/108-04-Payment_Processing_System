"""
Run this ONCE at Docker build time (not at request time).
Loads the FAQ data, encodes all questions with MiniLM, and saves the
embeddings to disk so the running container only needs to embed the
user's query, never the whole FAQ corpus.

Usage:
    python src/precompute_embeddings.py
"""
import json
import os
import numpy as np
from sentence_transformers import SentenceTransformer

# Resolve paths relative to this file's location (src/), not the
# current working directory — so it works regardless of where you
# run this script from.
_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FAQ_PATH = os.path.join(_BASE_DIR, "data", "faqs.json")
EMBEDDINGS_OUTPUT_PATH = os.path.join(_BASE_DIR, "data", "faq_embeddings.npy")


def main():
    print("Loading FAQ data...")
    with open(FAQ_PATH, "r") as f:
        faqs = json.load(f)

    print(f"Loading model 'all-MiniLM-L6-v2'...")
    model = SentenceTransformer("all-MiniLM-L6-v2")

    print(f"Encoding {len(faqs)} FAQ questions...")
    questions = [faq["question"] for faq in faqs]
    embeddings = model.encode(questions, convert_to_numpy=True)

    np.save(EMBEDDINGS_OUTPUT_PATH, embeddings)
    print(f"Saved embeddings with shape {embeddings.shape} to {EMBEDDINGS_OUTPUT_PATH}")


if __name__ == "__main__":
    main()
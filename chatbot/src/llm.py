"""
Handles the call to Gemini Flash for answer generation, with:
  - a request timeout (don't hang forever on a slow/stuck request)
  - retries with backoff (handle transient failures/network blips)
  - a clear exception if all retries are exhausted, so the caller
    (server.py) can fall back to the raw FAQ answer instead of
    returning a generic error to the user
"""
import os
import time
import requests

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent"

REQUEST_TIMEOUT_SECONDS = 30
MAX_RETRIES = 3
BACKOFF_BASE_SECONDS = 1  # 1s, 2s, 4s between retries


class LLMUnavailableError(Exception):
    """Raised when the LLM call fails after all retries are exhausted."""
    pass


def ask_gemini(prompt: str) -> str:
    if not GEMINI_API_KEY:
        raise LLMUnavailableError("GEMINI_API_KEY is not set")

    last_error = None

    for attempt in range(MAX_RETRIES):
        try:
            response = requests.post(
                GEMINI_URL,
                params={"key": GEMINI_API_KEY},
                json={"contents": [{"parts": [{"text": prompt}]}]},
                timeout=REQUEST_TIMEOUT_SECONDS,
            )
            response.raise_for_status()
            data = response.json()
            return data["candidates"][0]["content"]["parts"][0]["text"].strip()

        except (requests.exceptions.RequestException, KeyError, IndexError) as e:
            last_error = e
            print(f"[DEBUG] Gemini call attempt {attempt + 1} failed: {e}")
            if hasattr(e, "response") and e.response is not None:
                print(f"[DEBUG] Response status: {e.response.status_code}")
                print(f"[DEBUG] Response body: {e.response.text}")
            if attempt < MAX_RETRIES - 1:
                sleep_time = BACKOFF_BASE_SECONDS * (2 ** attempt)
                time.sleep(sleep_time)

    raise LLMUnavailableError(
        f"Gemini call failed after {MAX_RETRIES} attempts: {last_error}"
    )
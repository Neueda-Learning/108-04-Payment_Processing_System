"""
Thin HTTP client for the Spring Boot backend's analytics endpoints
(GET /stats/dashboard, GET /stats/payments). Kept separate from
retrieval.py because it hits a live service (payment data) instead of
the static FAQ corpus.
"""
import os
import requests

BACKEND_API_URL = os.environ.get("BACKEND_API_URL", "http://localhost:8080")
REQUEST_TIMEOUT_SECONDS = 5


class StatsUnavailableError(Exception):
    """Raised when the backend stats endpoints can't be reached or return an error."""
    pass


def get_dashboard_stats(from_date: str | None = None, to_date: str | None = None) -> dict:
    """
    Calls GET /stats/dashboard on the backend. Dates, if given, must be
    ISO strings (YYYY-MM-DD). With no dates, the backend defaults to the
    last 30 days.
    """
    params = {}
    if from_date:
        params["from"] = from_date
    if to_date:
        params["to"] = to_date

    try:
        response = requests.get(
            f"{BACKEND_API_URL}/stats/dashboard",
            params=params,
            timeout=REQUEST_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        raise StatsUnavailableError(f"Could not fetch dashboard stats: {e}") from e


def get_payment_stats() -> dict:
    """Calls GET /stats/payments on the backend (simple aggregate totals)."""
    try:
        response = requests.get(
            f"{BACKEND_API_URL}/stats/payments",
            timeout=REQUEST_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        raise StatsUnavailableError(f"Could not fetch payment stats: {e}") from e
